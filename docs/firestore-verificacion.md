# Evidencia de verificación de Firestore

Proyecto healthcontrol-d0948, base (default) STANDARD. Pruebas locales contra demo-healthcontrol (Auth + Firestore emulados). Suite: firebase-tests/invitaciones.test.mjs. Resultado: 24 aprobadas, 0 fallidas.
Prueba real explícita: firebase-tests/production-smoke.mjs. Siete comprobaciones aprobadas con dos cuentas temporales; Auth y ocho documentos creados por la prueba eliminados posteriormente mediante Firebase MCP.
Las reglas publicadas se consultaron y compararon con firestore.rules; coincidencia confirmada.

## Matriz de intentos
| Intento | Resultado y evidencia |
| --- | --- |
| Lectura o escritura sin autenticación | Denegadas; prueba del emulador. |
| Enumerar usuarios, códigos o perfiles compartidos | Denegado; consultas de lista probadas. |
| Leer salud usando únicamente un código | Denegado antes de aceptar; emulador y proyecto real. |
| Leer correo o cuenta privada de una persona vinculada | Denegado aun aceptada; emulador y real. |
| Suplantar al emisor o cambiar el UID de la ruta | Denegado; esquema y propietario probados. |
| Auto-invitarse | Denegado; prueba. |
| Destino inexistente/código de otra persona | Denegado; prueba. |
| Crear una solicitud ya aceptada | Denegado; prueba. |
| Autoaceptación por el emisor | Denegada; emulador y real. |
| Responder como tercero | Denegado; prueba. |
| Cancelar como destinatario | Denegado; prueba. |
| Cambiar participantes o fecha original al responder | Denegado; prueba. |
| Duplicar solicitudes pendientes/aceptadas | Denegado; pruebas de duplicados y contraparte activa. |
| Envío cruzado simultáneo | Solo una transacción activa final; prueba concurrente. |
| Acceso después de revocar | Nuevas lecturas denegadas; emulador y real. |
| Escribir salud/registros ajenos con vínculo aceptado | Denegado; prueba. |
| Robar o reservar un código que el propio perfil no posee | Denegado; prueba. |
| Campos extra o privilegios enviados por el cliente | Denegados por claves permitidas; prueba. |
| Omitir/borrar campos requeridos con create/update | Denegado; prueba. |
| Tipos, enumeraciones, longitudes y rangos malformados | Denegados; pruebas de perfiles y registros. |
| Acceder a subcolecciones sin titular o rutas de simuladores | Denegado; prueba. |
| Compartir ubicación sin consentimiento o sin vínculo | Denegado; prueba de consentimiento y revocación del consentimiento. |
| Guardar URI locales en documentos compartidos | Denegado; pruebas de campos de adjuntos y perfil compartido. |

Validación estática adicional: no hay reglas recursivas permisivas, contadores económicos, saldo ni atributos de administrador en este modelo; no se atribuyen pruebas dinámicas a funcionalidades inexistentes. Los cambios de estado usan listas explícitas y las escrituras clínicas aplican los mismos validadores en create y update.
La validación de los 24 horarios inicialmente superaba el límite de expresiones del motor; se simplificó y se verificaron la lista válida de 24 y un último elemento inválido.
Las consultas reales usan dos filtros individuales por emisor/destinatario y documentos concretos; no se agregaron ni eliminaron índices compuestos.
El alcance no incluye resistencia a abuso masivo de cuentas/códigos, auditoría de todos los servicios Firebase, borrado remoto de cachés, pruebas de interfaz física ni garantía clínica.
