// Ejecutar únicamente de forma explícita: crea dos cuentas temporales en el proyecto real.
// Las contraseñas se generan en memoria y no se registran. El informe contiene solo IDs para limpieza.
import assert from "node:assert/strict";
import { randomBytes } from "node:crypto";
import { readFileSync, writeFileSync } from "node:fs";
import { initializeApp, deleteApp } from "firebase/app";
import { getAuth, createUserWithEmailAndPassword, updateProfile, signOut, signInWithEmailAndPassword, deleteUser } from "firebase/auth";
import {
  getFirestore, doc, getDocFromServer, getDocsFromServer, collection, query, where,
  runTransaction, writeBatch, updateDoc, setDoc, onSnapshot, serverTimestamp, terminate
} from "firebase/firestore";

assert.equal(process.env.FIRESTORE_EMULATOR_HOST, undefined, "La prueba real no debe usar un emulador.");
const settings = JSON.parse(readFileSync(new URL("../app/google-services.json", import.meta.url), "utf8"));
const projectId = settings.project_info.project_id;
assert.equal(projectId, "healthcontrol-d0948", "Proyecto inesperado.");
const client = settings.client.find(c => c.client_info.android_client_info?.package_name === "com.icm2630.proyecto");
assert.ok(client, "No se encontró la configuración Android.");
const config = {
  apiKey: client.api_key[0].current_key, projectId,
  appId: client.client_info.mobilesdk_app_id,
  authDomain: projectId + ".firebaseapp.com"
};
const identificador = Date.now().toString(36);
const reporte = { projectId, completed: false, checks: [], createdDocumentPaths: [], accountsDeleted: false };
const cuentas = [];
const apps = [];
const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const ref = (database, path) => doc(database, path);
function codigo() {
  return [...randomBytes(6)].map(n => alphabet[n % alphabet.length]).join("");
}
const comprobarDenegado = async operacion => {
  try { await operacion(); }
  catch (e) { assert.equal(e.code, "permission-denied"); return; }
  assert.fail("Se concedió un acceso que debía estar denegado.");
};
async function cuenta(sufijo) {
  const app = initializeApp(config, "qa-healthcontrol-" + identificador + "-" + sufijo);
  apps.push(app);
  const auth = getAuth(app);
  const firestore = getFirestore(app);
  const email = "qa-healthcontrol-" + identificador + "-" + sufijo + "@example.test";
  const password = randomBytes(24).toString("base64url");
  const user = (await createUserWithEmailAndPassword(auth, email, password)).user;
  const nombre = "QA HealthControl " + sufijo.toUpperCase();
  const cuenta = { app, auth, firestore, email, password, uid: user.uid, nombre };
  cuentas.push(cuenta);
  await updateProfile(user, { displayName: nombre });
  for (let i = 0; i < 5; i++) {
    const candidato = codigo();
    const resultado = await runTransaction(firestore, async tx => {
      const codeRef = ref(firestore, "codigos/" + candidato);
      if ((await tx.get(codeRef)).exists()) return false;
      tx.set(ref(firestore, "usuarios/" + user.uid), {
        nombreCompleto: nombre, correo: email, codigoVinculacion: candidato,
        configurado: true, creadoEn: serverTimestamp(), actualizadoEn: serverTimestamp(),
        compartirUbicacion: false
      });
      tx.set(codeRef, { uid: user.uid, nombre });
      tx.set(ref(firestore, "perfilesCompartidos/" + user.uid), {
        nombreCompleto: nombre, codigoVinculacion: candidato,
        configurado: true, actualizadoEn: serverTimestamp(), condiciones: [], alergias: ""
      });
      return true;
    });
    if (resultado) {
      cuenta.codigo = candidato;
      reporte.createdDocumentPaths.push("usuarios/" + user.uid, "codigos/" + candidato,
        "perfilesCompartidos/" + user.uid);
      return cuenta;
    }
  }
  throw new Error("No se pudo reservar un código de prueba.");
}
function esperarSnapshot(q, predicate) {
  return new Promise((resolve, reject) => {
    let unsubscribe = () => {};
    const timeout = setTimeout(() => { unsubscribe(); reject(new Error("No llegó la actualización en tiempo real.")); }, 20000);
    unsubscribe = onSnapshot(q, snapshot => {
      if (predicate(snapshot)) { clearTimeout(timeout); unsubscribe(); resolve(); }
    }, e => { clearTimeout(timeout); unsubscribe(); reject(e); });
  });
}
try {
  const a = await cuenta("a");
  const b = await cuenta("b");
  reporte.accounts = [{ uid: a.uid, codigo: a.codigo }, { uid: b.uid, codigo: b.codigo }];
  reporte.checks.push("dos cuentas reales registradas y códigos persistidos");
  await comprobarDenegado(() => getDocFromServer(ref(a.firestore, "perfilesCompartidos/" + b.uid)));
  const indice = await getDocFromServer(ref(a.firestore, "codigos/" + b.codigo));
  assert.equal(indice.data().uid, b.uid);
  reporte.checks.push("código real resuelto sin acceso previo a salud");

  const id = a.uid + "_" + b.uid;
  const path = "invitaciones/" + id;
  const recibidas = query(collection(b.firestore, "invitaciones"), where("destinatarioUid", "==", b.uid));
  const vistoEnB = esperarSnapshot(recibidas, s => s.docs.some(d => d.id === id && d.data().estado === "PENDIENTE"));
  await runTransaction(a.firestore, async tx => {
    const own = ref(a.firestore, path);
    const inversa = ref(a.firestore, "invitaciones/" + b.uid + "_" + a.uid);
    assert.equal((await tx.get(own)).exists(), false);
    assert.equal((await tx.get(inversa)).exists(), false);
    const sender = await tx.get(ref(a.firestore, "usuarios/" + a.uid));
    const receiver = await tx.get(ref(a.firestore, "codigos/" + b.codigo));
    tx.set(own, {
      emisorUid: a.uid, destinatarioUid: b.uid,
      nombreEmisor: sender.data().nombreCompleto, nombreDestinatario: receiver.data().nombre,
      codigoDestino: b.codigo, estado: "PENDIENTE",
      creadaEn: serverTimestamp(), actualizadaEn: serverTimestamp()
    });
  });
  reporte.createdDocumentPaths.push(path);
  await vistoEnB;
  reporte.checks.push("solicitud recibida por la otra cuenta en tiempo real");
  await comprobarDenegado(() => updateDoc(ref(a.firestore, path), { estado: "ACEPTADA", actualizadaEn: serverTimestamp() }));
  const vistoEnA = esperarSnapshot(ref(a.firestore, path), s => s.data()?.estado === "ACEPTADA");
  await updateDoc(ref(b.firestore, path), { estado: "ACEPTADA", actualizadaEn: serverTimestamp() });
  await vistoEnA;
  await getDocFromServer(ref(a.firestore, "perfilesCompartidos/" + b.uid));
  await getDocFromServer(ref(b.firestore, "perfilesCompartidos/" + a.uid));
  await comprobarDenegado(() => getDocFromServer(ref(a.firestore, "usuarios/" + b.uid)));
  reporte.checks.push("aceptación mutua, salud consultable y cuenta privada protegida");

  const citaPath = "usuarios/" + b.uid + "/citas/qa-invitacion";
  await setDoc(ref(b.firestore, citaPath), {
    id: "qa-invitacion", personaId: null, tipo: "MEDICINA_GENERAL",
    motivo: "Registro temporal para verificar sincronización", fechaMillis: Date.now(),
    hora: 9, minuto: 0, modalidad: "PRESENCIAL"
  });
  reporte.createdDocumentPaths.push(citaPath);
  assert.equal((await getDocFromServer(ref(a.firestore, citaPath))).data().id, "qa-invitacion");
  reporte.checks.push("cita real de la otra cuenta consultada con el vínculo aceptado");

  await signOut(a.auth);
  await signInWithEmailAndPassword(a.auth, a.email, a.password);
  const persistida = await getDocsFromServer(query(collection(a.firestore, "invitaciones"), where("emisorUid", "==", a.uid)));
  assert.equal(persistida.docs.find(d => d.id === id).data().estado, "ACEPTADA");
  reporte.checks.push("vínculo recuperado después de cerrar e iniciar sesión");
  await updateDoc(ref(a.firestore, path), { estado: "REVOCADA", actualizadaEn: serverTimestamp() });
  await comprobarDenegado(() => getDocFromServer(ref(a.firestore, "perfilesCompartidos/" + b.uid)));
  await comprobarDenegado(() => getDocFromServer(ref(a.firestore, citaPath)));
  reporte.checks.push("desvinculación retira acceso al perfil y los registros");
  reporte.completed = true;
} catch (e) {
  reporte.error = { code: e.code ?? "test-failed", message: e.message };
  process.exitCode = 1;
} finally {
  const deleted = await Promise.allSettled(cuentas.map(async c => {
    if (c.auth.currentUser) await deleteUser(c.auth.currentUser);
  }));
  reporte.accountsDeleted = deleted.every(r => r.status === "fulfilled");
  if (!reporte.accountsDeleted) process.exitCode = 1;
  await Promise.allSettled(cuentas.map(c => terminate(c.firestore)));
  await Promise.allSettled(apps.map(deleteApp));
  writeFileSync(new URL("./production-test-result.json", import.meta.url), JSON.stringify(reporte, null, 2) + "\n");
  console.log(JSON.stringify(reporte, null, 2));
}
