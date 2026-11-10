import { before, beforeEach, after, test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import {
  collection, doc, getDoc, getDocs, setDoc, updateDoc, deleteDoc, writeBatch,
  query, where, serverTimestamp, Timestamp, GeoPoint, runTransaction, deleteField
} from "firebase/firestore";

const projectId = "demo-healthcontrol";
const [host, port] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8180").split(":");
let env;
const nombres = { alice: "Ana Prueba", bob: "Bruno Prueba", carol: "Carla Prueba" };
const codigos = { alice: "AAAAAA", bob: "BBBBBB", carol: "CCCCCC" };
const contexts = new Map();
const db = uid => {
  if (!contexts.has(uid)) contexts.set(uid, env.authenticatedContext(uid).firestore());
  return contexts.get(uid);
};
const ref = (database, path) => doc(database, path);
const perfil = (nombre, codigo) => ({
  nombreCompleto: nombre, codigoVinculacion: codigo, configurado: true,
  fechaNacimientoMillis: Date.UTC(1990, 0, 1), genero: "SIN_ESPECIFICAR",
  tipoSangre: "O_POSITIVO", alergias: "", condiciones: [],
  condicionRelevante: "", actualizadoEn: serverTimestamp()
});
const privada = (nombre, codigo) => ({
  ...perfil(nombre, codigo), correo: "privado@example.test",
  contacto: "privado@example.test", contactoEmergencia: "Contacto privado",
  telefonoEmergencia: "3000000000", permisoUbicacion: false,
  notificacionesActivas: false, compartirUbicacion: false, fotoUri: null,
  creadoEn: serverTimestamp()
});

async function crearCuenta(uid, nombre, codigo) {
  const database = db(uid);
  const batch = writeBatch(database);
  batch.set(ref(database, "usuarios/" + uid), privada(nombre, codigo));
  batch.set(ref(database, "codigos/" + codigo), { uid, nombre });
  batch.set(ref(database, "perfilesCompartidos/" + uid), perfil(nombre, codigo));
  await batch.commit();
}
const solicitud = (emisor, destino, extra = {}) => ({
  emisorUid: emisor, destinatarioUid: destino,
  nombreEmisor: nombres[emisor], nombreDestinatario: nombres[destino],
  codigoDestino: codigos[destino], estado: "PENDIENTE",
  creadaEn: serverTimestamp(), actualizadaEn: serverTimestamp(), ...extra
});
const solicitudRef = (database, a = "alice", b = "bob") => ref(database, "invitaciones/" + a + "_" + b);
const enviar = (a = "alice", b = "bob") => setDoc(solicitudRef(db(a), a, b), solicitud(a, b));
const responder = (estado, uid = "bob") => updateDoc(solicitudRef(db(uid)), { estado, actualizadaEn: serverTimestamp() });
async function aceptar() { await enviar(); await responder("ACEPTADA"); }

const cita = (id = "cita-prueba") => ({
  id, personaId: null, tipo: "MEDICINA_GENERAL", motivo: "Prueba de sincronización",
  fechaMillis: Date.UTC(2026, 10, 10), hora: 9, minuto: 0, modalidad: "PRESENCIAL",
  especialidad: "", institucion: "Centro de prueba", direccion: "", enlaceVirtual: "",
  nombreMedico: "", notas: "", nombreSoporte: null
});
const medicamento = (id = "med-prueba") => ({
  id, personaId: null, nombre: "Registro de prueba", forma: "PASTILLA",
  dosis: "1", unidad: "unidad", cantidadPorToma: "1", horarios: ["08:00"],
  fechaInicioMillis: Date.UTC(2026, 10, 1), fechaFinMillis: null,
  tratamientoPermanente: true, indicaciones: "", nombreOrdenMedica: null
});

before(async () => {
  env = await initializeTestEnvironment({
    projectId, firestore: { host, port: Number(port), rules: readFileSync(new URL("../firestore.rules", import.meta.url), "utf8") }
  });
});
beforeEach(async () => {
  contexts.clear();
  await env.clearFirestore();
  for (const uid of Object.keys(nombres)) await crearCuenta(uid, nombres[uid], codigos[uid]);
});
after(async () => { await env?.cleanup(); });

test("registro atómico real: reserva código, perfil privado y salud compartida", async () => {
  await assertSucceeds(crearCuenta("dave", "Diego Prueba", "DDDDDD"));
  assert.equal((await getDoc(ref(db("dave"), "usuarios/dave"))).data().codigoVinculacion, "DDDDDD");
  await assertFails(getDoc(ref(db("alice"), "usuarios/dave")));
});

test("guardar cambios de perfil actualiza cuenta, directorio y salud en un solo lote", async () => {
  const database = db("alice");
  const batch = writeBatch(database);
  batch.set(ref(database, "usuarios/alice"), {
    nombreCompleto: "Ana Actualizada", alergias: "Dato actualizado", actualizadoEn: serverTimestamp()
  }, { merge: true });
  batch.set(ref(database, "codigos/AAAAAA"), { uid: "alice", nombre: "Ana Actualizada" });
  batch.set(ref(database, "perfilesCompartidos/alice"),
    { ...perfil("Ana Actualizada", "AAAAAA"), alergias: "Dato actualizado" });
  await assertSucceeds(batch.commit());
  assert.equal((await getDoc(ref(database, "usuarios/alice"))).data().correo, "privado@example.test");
  assert.equal((await getDoc(ref(db("bob"), "codigos/AAAAAA"))).data().nombre, "Ana Actualizada");
});

test("actualiza el directorio anterior sin borrar el perfil ni cambiar el código", async () => {
  await env.withSecurityRulesDisabled(async context => {
    const admin = context.firestore();
    await setDoc(ref(admin, "codigos/AAAAAA"), { uid: "alice" });
  });
  const database = db("alice");
  await assertSucceeds(runTransaction(database, async tx => {
    const usuario = await tx.get(ref(database, "usuarios/alice"));
    const indice = await tx.get(ref(database, "codigos/AAAAAA"));
    assert.equal(indice.data().uid, "alice");
    const nombre = usuario.data().nombreCompleto;
    tx.set(ref(database, "codigos/AAAAAA"), { uid: "alice", nombre });
    tx.set(ref(database, "perfilesCompartidos/alice"), perfil(nombre, "AAAAAA"));
  }));
  assert.equal((await getDoc(ref(database, "usuarios/alice"))).data().contactoEmergencia, "Contacto privado");
  assert.equal((await getDoc(ref(database, "codigos/AAAAAA"))).data().nombre, nombres.alice);
});

test("sin autenticar no se puede leer ni escribir ninguna colección", async () => {
  const anon = env.unauthenticatedContext().firestore();
  for (const path of ["usuarios/alice", "codigos/AAAAAA", "perfilesCompartidos/alice", "ubicaciones/alice"]) {
    await assertFails(getDoc(ref(anon, path)));
  }
  await assertFails(setDoc(solicitudRef(anon), solicitud("alice", "bob")));
  await assertFails(getDocs(collection(anon, "invitaciones")));
});

test("el código muestra únicamente UID y nombre; no concede acceso a la salud", async () => {
  const indice = await assertSucceeds(getDoc(ref(db("bob"), "codigos/AAAAAA")));
  assert.deepEqual(Object.keys(indice.data()).sort(), ["nombre", "uid"]);
  await assertFails(getDoc(ref(db("bob"), "usuarios/alice")));
  await assertFails(getDoc(ref(db("bob"), "perfilesCompartidos/alice")));
  await assertFails(getDocs(collection(db("bob"), "codigos")));
  await assertFails(getDocs(collection(db("bob"), "usuarios")));
});

test("solicitud pendiente visible con las dos consultas exactas de Android", async () => {
  await assertSucceeds(enviar());
  const enviadas = query(collection(db("alice"), "invitaciones"), where("emisorUid", "==", "alice"));
  const recibidas = query(collection(db("bob"), "invitaciones"), where("destinatarioUid", "==", "bob"));
  assert.equal((await assertSucceeds(getDocs(enviadas))).size, 1);
  assert.equal((await assertSucceeds(getDocs(recibidas))).size, 1);
  await assertFails(getDocs(collection(db("carol"), "invitaciones")));
  await assertFails(getDoc(solicitudRef(db("carol"))));
  await assertFails(getDoc(ref(db("alice"), "perfilesCompartidos/bob")));
});

test("no permite auto-invitaciones, códigos ajenos ni suplantar al emisor", async () => {
  await assertFails(setDoc(solicitudRef(db("alice"), "alice", "alice"), solicitud("alice", "alice")));
  await assertFails(setDoc(solicitudRef(db("alice")), solicitud("alice", "bob", { codigoDestino: "CCCCCC" })));
  await assertFails(setDoc(solicitudRef(db("carol")), solicitud("alice", "bob")));
  await assertFails(setDoc(solicitudRef(db("alice")), solicitud("alice", "bob", { nombreEmisor: "Otra persona" })));
  await assertFails(setDoc(solicitudRef(db("alice")), solicitud("alice", "bob", { codigoDestino: "ZZZZZZ" })));
});

test("impide crear una solicitud ya aceptada y duplicar solicitudes pendientes", async () => {
  await assertFails(setDoc(solicitudRef(db("alice")), solicitud("alice", "bob", { estado: "ACEPTADA" })));
  await enviar();
  await assertFails(enviar());
  await assertFails(enviar("bob", "alice"));
});

test("las transacciones simultáneas de dos usuarios dejan una sola solicitud activa", async () => {
  async function transaccion(a, b) {
    const database = db(a);
    return runTransaction(database, async tx => {
      const propia = await tx.get(solicitudRef(database, a, b));
      const inversa = await tx.get(solicitudRef(database, b, a));
      assert.ok(!propia.exists() && !inversa.exists(), "Ya existe una solicitud");
      tx.set(solicitudRef(database, a, b), solicitud(a, b));
    });
  }
  const resultados = await Promise.allSettled([transaccion("alice", "bob"), transaccion("bob", "alice")]);
  assert.equal(resultados.filter(r => r.status === "fulfilled").length, 1);
});

test("solo el destinatario acepta o rechaza; solo el emisor cancela", async () => {
  await enviar();
  await assertFails(responder("ACEPTADA", "alice"));
  await assertFails(responder("RECHAZADA", "alice"));
  await assertFails(responder("CANCELADA", "bob"));
  await assertFails(responder("ACEPTADA", "carol"));
  await assertSucceeds(responder("ACEPTADA"));
});

test("aceptar habilita salud y registros en ambos sentidos, nunca la cuenta privada", async () => {
  await setDoc(ref(db("alice"), "usuarios/alice/citas/cita-prueba"), cita());
  await setDoc(ref(db("bob"), "usuarios/bob/medicamentos/med-prueba"), medicamento());
  await aceptar();
  await assertSucceeds(getDoc(ref(db("bob"), "perfilesCompartidos/alice")));
  await assertSucceeds(getDoc(ref(db("alice"), "perfilesCompartidos/bob")));
  assert.equal((await assertSucceeds(getDocs(collection(db("bob"), "usuarios/alice/citas")))).size, 1);
  assert.equal((await assertSucceeds(getDocs(collection(db("alice"), "usuarios/bob/medicamentos")))).size, 1);
  await assertFails(getDoc(ref(db("bob"), "usuarios/alice")));
  await assertFails(getDoc(ref(db("alice"), "usuarios/bob")));
  await assertFails(getDocs(collection(db("alice"), "perfilesCompartidos")));
});

test("un vínculo permite consultar pero no escribir registros ni perfiles ajenos", async () => {
  await aceptar();
  await assertFails(setDoc(ref(db("bob"), "usuarios/alice/citas/cita-prueba"), cita()));
  await assertFails(setDoc(ref(db("bob"), "usuarios/alice/medicamentos/med-prueba"), medicamento()));
  await assertFails(updateDoc(ref(db("bob"), "perfilesCompartidos/alice"), { alergias: "Cambiar", actualizadoEn: serverTimestamp() }));
  await assertFails(updateDoc(ref(db("bob"), "usuarios/alice"), { compartirUbicacion: true }));
});

test("rechazar o cancelar no comparte datos; se permite una nueva solicitud", async () => {
  await enviar();
  await assertSucceeds(responder("RECHAZADA"));
  await assertFails(getDoc(ref(db("alice"), "perfilesCompartidos/bob")));
  await assertSucceeds(enviar());
  await assertSucceeds(responder("CANCELADA", "alice"));
  await assertFails(getDoc(ref(db("bob"), "perfilesCompartidos/alice")));
});

test("cualquiera de las partes revoca y el acceso se retira inmediatamente", async () => {
  await aceptar();
  await assertSucceeds(responder("REVOCADA", "alice"));
  await assertFails(getDoc(ref(db("alice"), "perfilesCompartidos/bob")));
  await assertFails(getDocs(collection(db("bob"), "usuarios/alice/citas")));
  await assertFails(responder("ACEPTADA"));
  await assertSucceeds(enviar());
  await assertSucceeds(responder("ACEPTADA"));
  await assertSucceeds(responder("REVOCADA", "bob"));
});

test("varias peticiones independientes se conservan para cada destinatario", async () => {
  await enviar("alice", "bob");
  await enviar("alice", "carol");
  await enviar("bob", "carol");
  const q = query(collection(db("carol"), "invitaciones"), where("destinatarioUid", "==", "carol"));
  assert.equal((await getDocs(q)).size, 2);
});

test("impide cambiar participantes, fechas de creación y metadatos al aceptar", async () => {
  await enviar();
  for (const alteracion of [
    { emisorUid: "carol" }, { destinatarioUid: "carol" },
    { creadaEn: Timestamp.fromMillis(0) }, { nombreEmisor: "Nombre manipulado" },
    { campoExtra: true }
  ]) {
    await assertFails(updateDoc(solicitudRef(db("bob")), { estado: "ACEPTADA", actualizadaEn: serverTimestamp(), ...alteracion }));
  }
  await assertFails(setDoc(solicitudRef(db("alice")), solicitud("alice", "bob", { creadaEn: Timestamp.fromMillis(0) })));
});

test("no permite robar códigos ni reservar códigos que el propio perfil no tiene", async () => {
  await assertFails(setDoc(ref(db("bob"), "codigos/AAAAAA"), { uid: "bob", nombre: nombres.bob }));
  await assertFails(setDoc(ref(db("alice"), "codigos/EEEEEE"), { uid: "alice", nombre: nombres.alice }));
  await assertFails(updateDoc(ref(db("alice"), "usuarios/alice"), { codigoVinculacion: "BBBBBB" }));
});

test("el dueño tampoco puede eludir tipos, límites o esquemas con update", async () => {
  const user = ref(db("alice"), "usuarios/alice");
  for (const cambio of [
    { nombreCompleto: "x".repeat(151) }, { condiciones: [7] }, { condiciones: ["No permitida"] },
    { alergias: "x".repeat(2001) }, { permisoUbicacion: "sí" }, { isAdmin: true },
    { correo: "correo-invalido" }, { fotoUri: "javascript:alert(1)" },
    { acompanantes: ["bob"] }, { creadoEn: Timestamp.fromMillis(0) }
  ]) await assertFails(updateDoc(user, cambio));
  const shared = ref(db("alice"), "perfilesCompartidos/alice");
  await assertFails(updateDoc(shared, { correo: "privado@example.test", actualizadoEn: serverTimestamp() }));
  await assertFails(updateDoc(shared, { telefonoEmergencia: "3000000000", actualizadoEn: serverTimestamp() }));
  await assertFails(updateDoc(shared, { fotoUri: "content://local", actualizadoEn: serverTimestamp() }));
});

test("los registros no admiten adjuntos locales, cambios de propietario ni campos extra", async () => {
  const r = ref(db("alice"), "usuarios/alice/citas/cita-prueba");
  await assertSucceeds(setDoc(r, cita()));
  for (const cambio of [
    { id: "otro" }, { personaId: "bob" }, { ownerId: "bob" },
    { soporteUri: "content://privado" }, { hora: 24 }, { minuto: -1 },
    { fechaMillis: "ayer" }, { notas: "x".repeat(4001) }, { modalidad: "OTRA" },
    { enlaceVirtual: "intent://actividad-interna" }, { enlaceVirtual: "javascript:alert(1)" }
  ]) await assertFails(updateDoc(r, cambio));
  await assertSucceeds(updateDoc(r, { motivo: "Motivo actualizado", enlaceVirtual: "https://meet.example.test/prueba" }));
  assert.equal((await getDoc(r)).data().motivo, "Motivo actualizado");
  await assertSucceeds(deleteDoc(r));
});

test("cada elemento de horarios se valida en create y update", async () => {
  const r = ref(db("alice"), "usuarios/alice/medicamentos/med-prueba");
  await assertSucceeds(setDoc(r, medicamento()));
  for (const cambio of [
    { horarios: [8] }, { horarios: ["08:00", "x".repeat(21)] },
    { horarios: Array(25).fill("08:00") }, { horarios: [] },
    { horarios: [...Array(23).fill("08:00"), 8] },
    { horarios: [...Array(23).fill("08:00"), "x".repeat(21)] },
    { tratamientoPermanente: "sí" }, { forma: "OTRA" },
    { ordenMedicaUri: "content://privado" }, { fechaFinMillis: -1 }
  ]) await assertFails(updateDoc(r, cambio));
  await assertSucceeds(updateDoc(r, { horarios: Array(24).fill("08:00") }));
});

test("create y update rechazan la omisión de campos requeridos", async () => {
  const database = db("alice");
  const { tipo, ...sinTipo } = cita();
  const r = ref(database, "usuarios/alice/citas/cita-prueba");
  await assertFails(setDoc(r, sinTipo));
  await setDoc(r, cita());
  await assertFails(updateDoc(r, { tipo: deleteField() }));
  await assertFails(updateDoc(ref(database, "usuarios/alice"), { codigoVinculacion: deleteField() }));
  await assertFails(setDoc(solicitudRef(database, "alice", "carol"),
    solicitud("alice", "carol", { creadaEn: Timestamp.fromMillis(0) })));
});

test("no se accede a subcolecciones huérfanas ni a la conexión y familiares simulados", async () => {
  await assertFails(setDoc(ref(db("huerfano"), "usuarios/huerfano/citas/cita-prueba"), cita()));
  await assertFails(getDocs(collection(db("huerfano"), "usuarios/huerfano/citas")));
  await assertFails(setDoc(ref(db("alice"), "_conexion/prueba"), { ultimaConexion: serverTimestamp() }));
  await assertFails(setDoc(ref(db("alice"), "usuarios/alice/familiares/BBBBBB"), { simulado: true }));
});

test("la ubicación requiere consentimiento separado y vínculo aceptado", async () => {
  const own = ref(db("alice"), "ubicaciones/alice");
  const posicion = () => ({ punto: new GeoPoint(4.6, -74.1), actualizadoEn: serverTimestamp() });
  await assertFails(setDoc(own, posicion()));
  await updateDoc(ref(db("alice"), "usuarios/alice"), { compartirUbicacion: true });
  await assertSucceeds(setDoc(own, posicion()));
  await assertFails(getDoc(ref(db("bob"), "ubicaciones/alice")));
  await aceptar();
  await assertSucceeds(getDoc(ref(db("bob"), "ubicaciones/alice")));
  await assertFails(setDoc(ref(db("bob"), "ubicaciones/alice"), posicion()));
  await assertFails(updateDoc(own, { punto: [4.6, -74.1], actualizadoEn: serverTimestamp() }));
  await assertFails(updateDoc(own, { actualizadoEn: Timestamp.fromMillis(0) }));
  const batch = writeBatch(db("alice"));
  batch.update(ref(db("alice"), "usuarios/alice"), { compartirUbicacion: false });
  batch.delete(own);
  await assertSucceeds(batch.commit());
  await assertFails(getDoc(ref(db("bob"), "ubicaciones/alice")));
});

test("una sesión nueva recupera el vínculo; un tercero sigue sin poder consultarlo", async () => {
  await aceptar();
  const otroDispositivo = env.authenticatedContext("alice", { dispositivo: "segundo" }).firestore();
  const q = query(collection(otroDispositivo, "invitaciones"), where("emisorUid", "==", "alice"));
  const docs = await assertSucceeds(getDocs(q));
  assert.equal(docs.docs[0].data().estado, "ACEPTADA");
  await assertSucceeds(getDoc(ref(otroDispositivo, "perfilesCompartidos/bob")));
  await assertFails(getDoc(ref(db("carol"), "perfilesCompartidos/bob")));
});
