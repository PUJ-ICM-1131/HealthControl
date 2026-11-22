import { readFileSync } from "node:fs";
import { after, before, beforeEach, test } from "node:test";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import { doc, collection, getDoc, getDocs, setDoc, deleteDoc, updateDoc, serverTimestamp, Timestamp } from "firebase/firestore";

let env;
const DIA = 86400000;
const hoy = Math.floor(Date.now() / DIA) * DIA;
const horario = "8:00 AM";
const clave = (med = "med-1", fecha = hoy, hora = horario) => med + "_" + fecha + "_" + hora;
const ref = (db, uid = "abuelo", id = clave()) => doc(db, "usuarios", uid, "tomas", id);
const datos = (extra = {}) => ({
    medicamentoId: "med-1", fechaMillis: hoy, horario, registradaEn: serverTimestamp(), ...extra
});
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
        const db = ctx.firestore();
        await setDoc(doc(db, "usuarios/abuelo"), { nombreCompleto: "Abuelo" });
        await setDoc(doc(db, "usuarios/cuidador"), { nombreCompleto: "Cuidador" });
        await setDoc(doc(db, "usuarios/abuelo/medicamentos/med-1"), {
            id: "med-1", fechaInicioMillis: hoy - DIA, fechaFinMillis: hoy + DIA,
            tratamientoPermanente: false, horarios: [horario, "8:00 PM"]
        });
    });
});
after(async () => { await env?.cleanup(); });

test("titular confirma, consulta y deshace una toma", async () => {
    const db = env.authenticatedContext("abuelo").firestore();
    await assertSucceeds(setDoc(ref(db), datos()));
    await assertSucceeds(getDoc(ref(db)));
    await assertSucceeds(getDocs(collection(db, "usuarios/abuelo/tomas")));
    await assertSucceeds(deleteDoc(ref(db)));
});
test("no permite repetir una confirmacion sobrescribiendo su hora", async () => {
    const db = env.authenticatedContext("abuelo").firestore();
    await assertSucceeds(setDoc(ref(db), datos()));
    await assertFails(setDoc(ref(db), datos()));
    await assertFails(updateDoc(ref(db), { registradaEn: serverTimestamp() }));
});
test("cuidador aceptado puede leer pero no confirmar ni deshacer", async () => {
    const abuelo = env.authenticatedContext("abuelo").firestore();
    await setDoc(ref(abuelo), datos());
    await env.withSecurityRulesDisabled(ctx =>
        setDoc(doc(ctx.firestore(), "invitaciones/cuidador_abuelo"), { estado: "ACEPTADA" }));
    const db = env.authenticatedContext("cuidador").firestore();
    await assertSucceeds(getDoc(ref(db)));
    await assertSucceeds(getDocs(collection(db, "usuarios/abuelo/tomas")));
    await assertFails(setDoc(ref(db, "abuelo", clave("med-1", hoy, "8:00 PM")), datos({ horario: "8:00 PM" })));
    await assertFails(deleteDoc(ref(db)));
    await assertFails(getDoc(doc(db, "usuarios/abuelo")));
});
test("visitante, desconocido, invitacion pendiente y revocada no leen las tomas", async () => {
    const abuelo = env.authenticatedContext("abuelo").firestore();
    await setDoc(ref(abuelo), datos());
    for (const ctx of [env.unauthenticatedContext(), env.authenticatedContext("desconocido")]) {
        await assertFails(getDoc(ref(ctx.firestore())));
        await assertFails(getDocs(collection(ctx.firestore(), "usuarios/abuelo/tomas")));
        await assertFails(setDoc(ref(ctx.firestore()), datos()));
    }
    const db = env.authenticatedContext("cuidador").firestore();
    for (const estado of ["PENDIENTE", "REVOCADA"]) {
        await env.withSecurityRulesDisabled(ctx =>
            setDoc(doc(ctx.firestore(), "invitaciones/cuidador_abuelo"), { estado }));
        await assertFails(getDoc(ref(db)));
    }
});
test("rechaza campos extra, tipos incorrectos, campos omitidos y timestamp del cliente", async () => {
    const db = env.authenticatedContext("abuelo").firestore();
    for (const payload of [
        datos({ extra: "no permitido" }), datos({ horario: 8 }), datos({ fechaMillis: "hoy" }),
        datos({ registradaEn: Timestamp.fromMillis(0) }), datos({ medicamentoId: "x".repeat(129) }),
        datos({ horario: "x".repeat(21) })
    ]) await assertFails(setDoc(ref(db), payload));
    const incompleto = datos(); delete incompleto.registradaEn;
    await assertFails(setDoc(ref(db), incompleto));
});
test("rechaza horarios inventados, id duplicado, medicamento inexistente y tomas futuras", async () => {
    const db = env.authenticatedContext("abuelo").firestore();
    await assertFails(setDoc(ref(db, "abuelo", "id-distinto"), datos()));
    await assertFails(setDoc(ref(db, "abuelo", clave("otro")), datos({ medicamentoId: "otro" })));
    await assertFails(setDoc(ref(db, "abuelo", clave("med-1", hoy, "9:00 AM")), datos({ horario: "9:00 AM" })));
    const futuro = hoy + 7 * DIA;
    await assertFails(setDoc(ref(db, "abuelo", clave("med-1", futuro)), datos({ fechaMillis: futuro })));
    await assertFails(setDoc(ref(db, "abuelo", clave("med-1", hoy + 1)), datos({ fechaMillis: hoy + 1 })));
});
test("rechaza tratamientos que no han iniciado, finalizados y titulares inexistentes", async () => {
    const db = env.authenticatedContext("abuelo").firestore();
    for (const fechas of [
        { fechaInicioMillis: hoy + DIA, fechaFinMillis: hoy + 2 * DIA },
        { fechaInicioMillis: hoy - 2 * DIA, fechaFinMillis: hoy - DIA }
    ]) {
        await env.withSecurityRulesDisabled(ctx =>
            updateDoc(doc(ctx.firestore(), "usuarios/abuelo/medicamentos/med-1"), fechas));
        await assertFails(setDoc(ref(db), datos()));
    }
    const inexistente = env.authenticatedContext("sin-perfil").firestore();
    await assertFails(setDoc(ref(inexistente, "sin-perfil"), datos()));
});
test("permite consultar una confirmacion historica pero no alterar la fecha", async () => {
    await env.withSecurityRulesDisabled(ctx => setDoc(ref(ctx.firestore(), "abuelo", clave("med-1", hoy - 10 * DIA)),
        datos({ fechaMillis: hoy - 10 * DIA, registradaEn: Timestamp.fromMillis(hoy - 10 * DIA) })));
    const db = env.authenticatedContext("abuelo").firestore();
    const historica = ref(db, "abuelo", clave("med-1", hoy - 10 * DIA));
    await assertSucceeds(getDoc(historica));
    await assertFails(updateDoc(historica, { fechaMillis: hoy }));
    await assertFails(deleteDoc(historica));
});
