# Análisis previo de Firestore
Proyecto existente: healthcontrol-d0948. Base (default), STANDARD / FIRESTORE_NATIVE, southamerica-east1.
Reglas anteriores consultadas mediante Firebase MCP: acceso general hasta 2026-11-19. No se modificaron datos existentes para este análisis.

## Rutas y consultas de la aplicación actualizada
- usuarios/{uid}: lectura de la propia cuenta mediante documento; perfil privado y preferencias. Escritura propia con código reservado.
- codigos/{codigo}: get exacto autenticado. Solo UID y nombre; prohibido listar. Reserva atómica con el usuario.
- perfilesCompartidos/{uid}: documento de salud, separado del correo, teléfono, contacto de emergencia, ajustes y URI locales. Lectura propia o tras aceptación mutua.
- invitaciones/{emisorUid}_{destinatarioUid}: dos consultas separadas, whereEqualTo(emisorUid, uid) y whereEqualTo(destinatarioUid, uid). Ninguna consulta global.
- usuarios/{uid}/citas y usuarios/{uid}/medicamentos: snapshots de cada cuenta autorizada. Escritura solo del titular. Identidad determinada por la ruta, nunca por campos del cliente.
- ubicaciones/{uid}: lectura de documento real solo con vínculo aceptado y consentimiento de compartir activo. Publicación propia, con mapa visible. Timestamp de servidor.
- _conexion y familiares simulados: ya no se consultan ni escriben. Se denegarán sin borrar documentos anteriores.

## Modelo y límites
Todos los documentos se validan en create y update. Solicitudes: UID de emisor/destinatario inmutables; solo el destinatario acepta/rechaza, solo el emisor cancela, cualquiera de las partes revoca. No se permite crear aceptadas ni aprobar solicitudes ajenas. Estados terminales pueden recibir un nuevo intento pendiente del emisor. Se impiden duplicados e invitaciones cruzadas activas en transacciones y reglas.
Los datos de cuenta privados no se comparten. La lectura de salud compartida es una decisión explícita del usuario al enviar/aceptar; no es un perfil público.
Los adjuntos de citas/medicamentos permanecen locales y no se incluyen en sus documentos compartidos.
Condiciones: lista restringida a las opciones del formulario. Horarios: máximo 24 elementos, cada uno string no vacío y acotado. Textos, fechas y enumeraciones con límites explícitos.
La compatibilidad de las reglas y consultas se comprobará con Firebase Emulator Suite antes de publicar.
