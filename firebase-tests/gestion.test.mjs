import { readFileSync } from "node:fs";
import { after, before, beforeEach, test } from "node:test";
import { strict as assert } from "node:assert";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import { doc, collection, getDoc, getDocs, setDoc, deleteDoc, updateDoc, runTransaction,
    serverTimestamp, Timestamp, onSnapshot } from "firebase/firestore";

let env;
const DIA = 86400000;
const hoy = Math.floor(Date.now() / DIA) * DIA;
const db = uid => (uid ? env.authenticatedContext(uid) : env.unauthenticatedContext()).firestore();
const invitacion = database => doc(database, "invitaciones/cuidador_abuelo");
const registro = (database, tipo = "citas", uid = "abuelo", id = "reg-1") =>
    doc(database, "usuarios", uid, tipo, id);
const auditoria = (uid = "cuidador", nombre = "Cuidador") => ({
    creadoPorUid: uid, creadoPorNombre: nombre, creadoEn: serverTimestamp(),
    actualizadoPorUid: uid, actualizadoPorNombre: nombre, actualizadoEn: serverTimestamp()
});
const cita = (extra = {}) => ({
    id: "reg-1", personaId: null, tipo: "MEDICINA_GENERAL", motivo: "Control",
    fechaMillis: hoy, hora: 8, minuto: 0, modalidad: "PRESENCIAL",
    auditoria: auditoria(), ...extra
});
const medicamento = (extra = {}) => ({
    id: "reg-1", personaId: null, nombre: "Tratamiento", forma: "PASTILLA", dosis: "10",
    unidad: "mg", cantidadPorToma: "1", horarios: ["8:00 AM"], fechaInicioMillis: hoy,
    fechaFinMillis: hoy + DIA, tratamientoPermanente: false, auditoria: auditoria(), ...extra
});
const solicitud = (extra = {}) => ({
    emisorUid: "cuidador", destinatarioUid: "abuelo", nombreEmisor: "Cuidador",
    nombreDestinatario: "Abuelo", codigoDestino: "ABCDEF", estado: "PENDIENTE",
    creadaEn: serverTimestamp(), actualizadaEn: serverTimestamp(),
    solicitaGestion: true, titularGestionUid: "", ...extra
});
const autorizar = async () => {
    await setDoc(invitacion(db("cuidador")), solicitud());
    await updateDoc(invitacion(db("abuelo")), {
        estado: "ACEPTADA", titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    });
};
const sembrarVinculo = async extra => env.withSecurityRulesDisabled(ctx =>
    setDoc(invitacion(ctx.firestore()), solicitud({ estado: "ACEPTADA", ...extra })));

before(async () => {
    env = await initializeTestEnvironment({
        projectId: "demo-healthcontrol",
        firestore: { host: "127.0.0.1", port: 8089,
            rules: readFileSync(new URL("../firestore.rules", import.meta.url), "utf8") }
    });
});
beforeEach(async () => {
    await env.clearFirestore();
    await env.withSecurityRulesDisabled(async ctx => {
        const database = ctx.firestore();
        for (const [uid, nombre, codigo] of [
            ["cuidador", "Cuidador", "GHJKLM"], ["abuelo", "Abuelo", "ABCDEF"],
            ["otro", "Otro", "NPQRST"]
        ]) {
            await setDoc(doc(database, "usuarios", uid), {
                nombreCompleto: nombre, codigoVinculacion: codigo, configurado: true
            });
            await setDoc(doc(database, "codigos", codigo), { uid, nombre });
        }
    });
});
after(async () => env?.cleanup());

test("solo el destinatario acepta y concede la gestión solicitada, en una dirección", async () => {
    await setDoc(invitacion(db("cuidador")), solicitud());
    await assertFails(updateDoc(invitacion(db("cuidador")), {
        estado: "ACEPTADA", titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
    await assertSucceeds(updateDoc(invitacion(db("abuelo")), {
        estado: "ACEPTADA", titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
    await assertSucceeds(setDoc(registro(db("cuidador")), cita()));
    await assertFails(setDoc(registro(db("abuelo"), "citas", "cuidador"),
        cita({ auditoria: auditoria("abuelo", "Abuelo") })));
});

test("las dos cuentas reciben en tiempo real una cita creada en la cuenta del titular", async () => {
    await autorizar();
    const esperar = database => new Promise((resolve, reject) => {
        const timeout = setTimeout(() => { stop(); reject(new Error("Snapshot no recibido")); }, 10000);
        const stop = onSnapshot(registro(database), snapshot => {
            if (snapshot.exists() && !snapshot.metadata.hasPendingWrites) {
                clearTimeout(timeout); stop(); resolve(snapshot.data());
            }
        }, error => { clearTimeout(timeout); reject(error); });
    });
    const lecturas = [esperar(db("abuelo")), esperar(db("cuidador"))];
    // Misma secuencia atómica que usa el repositorio Android.
    const database = db("cuidador");
    await runTransaction(database, async tx => {
        const ref = registro(database);
        await tx.get(ref);
        tx.set(ref, cita({ auditoria: null }));
        tx.update(ref, { auditoria: auditoria() });
    });
    for (const datos of await Promise.all(lecturas)) assert.equal(datos.auditoria.creadoPorUid, "cuidador");
    assert.equal((await getDoc(registro(db("cuidador"), "citas", "cuidador"))).exists(), false);
});

test("titular y cuidador autorizado crean y editan citas y medicamentos conservando el autor", async () => {
    await autorizar();
    for (const [tipo, datos] of [["citas", cita()], ["medicamentos", medicamento()]]) {
        await assertSucceeds(setDoc(registro(db("cuidador"), tipo), datos));
        const anterior = (await getDoc(registro(db("abuelo"), tipo))).data().auditoria;
        await assertSucceeds(updateDoc(registro(db("abuelo"), tipo), {
            ...(tipo === "citas" ? { motivo: "Control actualizado" } : { indicaciones: "Actualizadas" }),
            auditoria: { ...anterior, actualizadoPorUid: "abuelo",
                actualizadoPorNombre: "Abuelo", actualizadoEn: serverTimestamp() }
        }));
        const actual = (await getDoc(registro(db("cuidador"), tipo))).data().auditoria;
        assert.equal(actual.creadoPorUid, "cuidador");
        assert.equal(actual.actualizadoPorUid, "abuelo");
        assert.equal(actual.creadoEn.toMillis(), anterior.creadoEn.toMillis());
    }
});

test("un vínculo antiguo conserva consulta y no se convierte automáticamente en gestión", async () => {
    await env.withSecurityRulesDisabled(ctx => setDoc(invitacion(ctx.firestore()),
        solicitud({ estado: "ACEPTADA", solicitaGestion: false, titularGestionUid: "" })));
    await assertSucceeds(getDocs(collection(db("cuidador"), "usuarios/abuelo/citas")));
    await assertFails(setDoc(registro(db("cuidador")), cita()));
    await assertFails(setDoc(registro(db("cuidador"), "medicamentos"), medicamento()));
    await assertSucceeds(updateDoc(invitacion(db("abuelo")), {
        titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
    await assertSucceeds(setDoc(registro(db("cuidador")), cita()));
});

test("el titular puede autorizar desde un vínculo legado sin campos nuevos", async () => {
    const antigua = solicitud({ estado: "ACEPTADA" });
    delete antigua.solicitaGestion; delete antigua.titularGestionUid;
    await env.withSecurityRulesDisabled(ctx => setDoc(invitacion(ctx.firestore()), antigua));
    await assertFails(setDoc(registro(db("cuidador")), cita()));
    await assertSucceeds(updateDoc(invitacion(db("abuelo")), {
        titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
    await assertSucceeds(setDoc(registro(db("cuidador")), cita()));
});

test("el cuidador no se autoautoriza, no invierte el titular ni cambia la solicitud", async () => {
    await sembrarVinculo({ solicitaGestion: false, titularGestionUid: "" });
    await assertFails(updateDoc(invitacion(db("cuidador")), {
        titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
    await autorizarDesdeLegado();
    for (const payload of [
        { titularGestionUid: "cuidador" }, { destinatarioUid: "otro" },
        { emisorUid: "otro" }, { solicitaGestion: false }, { extra: true },
        { creadaEn: serverTimestamp() }
    ]) await assertFails(updateDoc(invitacion(db("cuidador")), { ...payload, actualizadaEn: serverTimestamp() }));
});
async function autorizarDesdeLegado() {
    await updateDoc(invitacion(db("abuelo")), { titularGestionUid: "abuelo", actualizadaEn: serverTimestamp() });
}

test("retirar gestión impide nuevas escrituras y conserva la consulta; revocar elimina la consulta", async () => {
    await autorizar();
    await setDoc(registro(db("cuidador")), cita());
    await assertSucceeds(updateDoc(invitacion(db("abuelo")), {
        titularGestionUid: "", actualizadaEn: serverTimestamp()
    }));
    await assertFails(updateDoc(registro(db("cuidador")), { motivo: "Sin permiso" }));
    await assertSucceeds(getDoc(registro(db("cuidador"))));
    await assertSucceeds(updateDoc(invitacion(db("abuelo")), {
        estado: "REVOCADA", titularGestionUid: "", actualizadaEn: serverTimestamp()
    }));
    await assertFails(getDoc(registro(db("cuidador"))));
    await assertFails(setDoc(registro(db("cuidador")), cita()));
});

test("pendientes, rechazados, revocados, desconocidos y visitantes no pueden gestionar", async () => {
    for (const estado of ["PENDIENTE", "RECHAZADA", "REVOCADA"]) {
        await sembrarVinculo({ estado, titularGestionUid: "abuelo" });
        await assertFails(setDoc(registro(db("cuidador")), cita()));
    }
    for (const uid of [null, "otro"]) {
        await assertFails(getDocs(collection(db(uid), "usuarios/abuelo/citas")));
        await assertFails(setDoc(registro(db(uid)), cita()));
    }
});

test("ni siquiera el cuidador autorizado puede confirmar, modificar o deshacer las tomas", async () => {
    await autorizar();
    await setDoc(registro(db("cuidador"), "medicamentos"), medicamento());
    const clave = "reg-1_" + hoy + "_8:00 AM";
    const ref = database => doc(database, "usuarios/abuelo/tomas", clave);
    const datos = { medicamentoId: "reg-1", fechaMillis: hoy, horario: "8:00 AM", registradaEn: serverTimestamp() };
    await assertFails(setDoc(ref(db("cuidador")), datos));
    await assertSucceeds(setDoc(ref(db("abuelo")), datos));
    await assertSucceeds(getDoc(ref(db("cuidador"))));
    await assertFails(updateDoc(ref(db("cuidador")), { registradaEn: serverTimestamp() }));
    await assertFails(deleteDoc(ref(db("cuidador"))));
    await assertSucceeds(deleteDoc(ref(db("abuelo"))));
});

test("el cuidador edita pero solo el titular elimina sus registros", async () => {
    await autorizar();
    await setDoc(registro(db("cuidador")), cita());
    await assertFails(deleteDoc(registro(db("cuidador"))));
    await assertSucceeds(deleteDoc(registro(db("abuelo"))));
});

test("actualizar no permite falsificar autores, cambiar fechas inmutables ni borrar auditoría", async () => {
    await autorizar();
    await setDoc(registro(db("cuidador")), cita());
    const previa = (await getDoc(registro(db("cuidador")))).data().auditoria;
    for (const cambio of [
        { creadoPorUid: "abuelo" }, { creadoPorNombre: "Otra persona" },
        { creadoEn: Timestamp.fromMillis(0) }, { actualizadoPorUid: "abuelo" },
        { actualizadoPorNombre: "Abuelo" }, { actualizadoEn: Timestamp.fromMillis(0) },
        { extra: "no" }, { actualizadoPorNombre: "x".repeat(151) }
    ]) await assertFails(updateDoc(registro(db("cuidador")), {
        auditoria: { ...previa, actualizadoEn: serverTimestamp(), ...cambio }
    }));
    await assertFails(setDoc(registro(db("cuidador")), cita({ auditoria: null })));
});

test("crear rechaza autor ajeno, timestamp del cliente y campos de auditoría incompletos", async () => {
    await autorizar();
    for (const a of [
        auditoria("abuelo", "Abuelo"), { ...auditoria(), creadoEn: Timestamp.fromMillis(0) },
        { ...auditoria(), actualizadoPorNombre: "Abuelo" }, { actualizadoPorUid: "cuidador" },
        { ...auditoria(), creadoPorUid: null, creadoPorNombre: null, creadoEn: null }
    ]) await assertFails(setDoc(registro(db("cuidador")), cita({ auditoria: a })));
});

test("al editar un registro legado se conserva su creación desconocida y se identifica al editor", async () => {
    await autorizar();
    await env.withSecurityRulesDisabled(ctx => {
        const datos = cita(); delete datos.auditoria;
        return setDoc(registro(ctx.firestore()), datos);
    });
    await assertSucceeds(updateDoc(registro(db("cuidador")), {
        auditoria: { ...auditoria(), creadoPorUid: null, creadoPorNombre: null, creadoEn: null }
    }));
});

test("crear y actualizar rechazan campos extra, tipos incorrectos, omisiones y tamaños excesivos", async () => {
    await autorizar();
    for (const [tipo, base, invalidos] of [
        ["citas", cita(), [{ extra: true }, { motivo: 9 }, { motivo: "x".repeat(2001) },
            { hora: -1 }, { hora: 24 }, { personaId: "otro" }, { fechaMillis: -9999999999999 }]],
        ["medicamentos", medicamento(), [{ extra: true }, { dosis: 9 }, { nombre: "x".repeat(151) },
            { horarios: Array(25).fill("8:00 AM") }, { horarios: [9] }, { horarios: [true] },
            { horarios: [null] }, { horarios: [{ hora: "8:00 AM" }] },
            { horarios: ["a|b"] }, { horarios: ["x".repeat(21)] }, { fechaFinMillis: hoy - DIA }]]
    ]) {
        for (const cambio of invalidos) await assertFails(setDoc(registro(db("cuidador"), tipo), { ...base, ...cambio }));
        await setDoc(registro(db("cuidador"), tipo), base);
        for (const cambio of invalidos) await assertFails(updateDoc(registro(db("cuidador"), tipo),
            { ...cambio, "auditoria.actualizadoEn": serverTimestamp() }));
        const incompleto = { ...base }; delete incompleto.id;
        await assertFails(setDoc(registro(db("cuidador"), tipo), incompleto));
    }
});

test("no permite saltar la aceptación ni conceder permisos durante el envío", async () => {
    await assertFails(setDoc(invitacion(db("cuidador")), solicitud({ estado: "ACEPTADA", titularGestionUid: "abuelo" })));
    await assertFails(setDoc(invitacion(db("cuidador")), solicitud({ titularGestionUid: "abuelo" })));
    await setDoc(invitacion(db("cuidador")), solicitud());
    await assertFails(updateDoc(invitacion(db("abuelo")), {
        estado: "ACEPTADA", titularGestionUid: "cuidador", actualizadaEn: serverTimestamp()
    }));
    await assertFails(updateDoc(invitacion(db("otro")), {
        estado: "ACEPTADA", titularGestionUid: "abuelo", actualizadaEn: serverTimestamp()
    }));
});

test("un cuidador no puede conceder permiso a terceros ni acceder a una cuenta sin vínculo", async () => {
    await autorizar();
    await assertFails(setDoc(doc(db("cuidador"), "invitaciones/otro_abuelo"), solicitud({
        emisorUid: "otro", nombreEmisor: "Otro", estado: "ACEPTADA", titularGestionUid: "abuelo"
    })));
    await assertFails(setDoc(registro(db("cuidador"), "citas", "otro"), cita()));
});

test("los documentos privados, los ajustes y la ubicación siguen siendo del titular", async () => {
    await autorizar();
    await assertFails(getDoc(doc(db("cuidador"), "usuarios/abuelo")));
    await assertFails(getDocs(collection(db("cuidador"), "usuarios")));
    await assertFails(updateDoc(doc(db("cuidador"), "usuarios/abuelo"), { compartirUbicacion: true }));
    await assertFails(setDoc(doc(db("cuidador"), "ubicaciones/abuelo"), { punto: null, actualizadoEn: serverTimestamp() }));
});

test("el titular sigue creando sus registros sin depender de un vínculo", async () => {
    await assertSucceeds(setDoc(registro(db("abuelo")), cita({ auditoria: auditoria("abuelo", "Abuelo") })));
    await assertSucceeds(setDoc(registro(db("abuelo"), "medicamentos"),
        medicamento({ auditoria: auditoria("abuelo", "Abuelo") })));
});

test("un permiso legado también funciona cuando el titular fue quien envió la invitación", async () => {
    await sembrarVinculo({ solicitaGestion: false, titularGestionUid: "" });
    await updateDoc(invitacion(db("cuidador")), { titularGestionUid: "cuidador", actualizadaEn: serverTimestamp() });
    await assertSucceeds(setDoc(registro(db("abuelo"), "citas", "cuidador"),
        cita({ auditoria: auditoria("abuelo", "Abuelo") })));
    await assertFails(setDoc(registro(db("cuidador")), cita()));
});

test("admite crear y editar medicamentos con el máximo de 24 horarios", async () => {
    await autorizar();
    const horarios = Array.from({ length: 24 }, (_, h) =>
        (h % 12 || 12) + ":00 " + (h < 12 ? "AM" : "PM"));
    await assertSucceeds(setDoc(registro(db("cuidador"), "medicamentos"), medicamento({ horarios })));
    await assertSucceeds(updateDoc(registro(db("cuidador"), "medicamentos"), {
        indicaciones: "Ajuste de indicaciones", "auditoria.actualizadoEn": serverTimestamp()
    }));
});

test("el SDK rechaza arrays anidados antes de enviarlos a las reglas", () => {
    assert.throws(() => setDoc(registro(db("cuidador"), "medicamentos"),
        medicamento({ horarios: [["8:00 AM"]] })), { code: "invalid-argument" });
});

test("no hay acceso a subcolecciones huérfanas ni listados públicos", async () => {
    await assertFails(getDocs(collection(db(null), "usuarios/abuelo/citas")));
    await assertFails(getDocs(collection(db("otro"), "invitaciones")));
    await assertFails(setDoc(registro(db("inexistente"), "citas", "inexistente"),
        cita({ auditoria: auditoria("inexistente", "Nadie") })));
});
