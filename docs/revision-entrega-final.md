# Revisión de HealthControl para la entrega final

## Resultado
La vinculación por invitación está implementada con Firebase Auth y Cloud Firestore reales en healthcontrol-d0948. Las reglas fueron compiladas, publicadas y consultadas después del despliegue: coinciden con firestore.rules. Base (default), STANDARD, región southamerica-east1. No se utiliza un servidor propio.

## Estructura y responsabilidades
- AuthViewModel: Firebase Auth por correo/contraseña y Google, recuperación, restauración y cierre de sesión. Expone StateFlow, usa viewModelScope y await(); retira su AuthStateListener al destruirse. Una instancia compartida entre login y registro.
- PerfilUsuario: datos del perfil; PersonaVinculada: proyección de una cuenta enlazada por UID, sin rol seleccionable. Invitacion: participantes y estado. CodigoInvitacion: generación y validación.
- UsuarioRepository: reserva atómica del código, perfil privado y proyección de salud compartida.
- VinculacionRepository: envío, recepción mediante Flow y transacciones de aceptación, rechazo, cancelación y revocación.
- SesionRepository: estado de la cuenta y del perfil consultado; no autentica.
- PerfilViewModel: escucha perfil, solicitudes, perfiles compartidos y registros; administra carga, errores y operaciones. Cancela escuchas y limpia datos al salir de la sesión.
- PerfilSetupViewModel: conserva su función necesaria de capturar salud al completar el registro y espera el guardado confirmado.
- CitaRepository y MedicamentoRepository: persistencia real por UID; cada titular escribe sus registros y las personas vinculadas pueden leerlos.
- AppNavigation: claves @Serializable, rememberNavBackStack y decoradores de estado/ViewModel por entrada. El historial se limpia al cerrar sesión.

Se eliminaron Persona, PersonaRepository, LoginViewModel, HomeViewModel vacío, MonitoreoScreen y ambos simuladores. También se retiraron TipoPerfil, personaVinculada única y cuentaCodigo como identidad.

## Correspondencia con las diapositivas
| Material | Aplicación |
| --- | --- |
| Sesión 6 | Accompanist para cámara, ubicación y notificaciones; permiso concedido, rationale y rechazo/Ajustes. Se solicita desde una acción del usuario. |
| Sesión 8 | TakePicture con FileProvider para foto completa, PickVisualMedia y OpenDocument; sin permisos generales de almacenamiento. |
| Sesión 9 | FusedLocationProviderClient, callbacks de ubicación y limpieza al salir del mapa. Se admite ubicación aproximada. |
| Sesión 10 | Se conserva mapa, rutas, Geocoder y sensores existentes. El acelerómetro se pausa fuera de primer plano. |
| Sesión 11.1 | AuthViewModel, estado observable, corutinas/await y navegación tras éxito. |
| Sesión 11.2 | Repositorios Firestore, transacciones/lotes y snapshots como Flow con awaitClose. |

Los ejemplos de clase se adaptan al dominio de invitaciones; no son una copia literal de cada ejemplo. El acceso compartido y sus reglas requieren lógica adicional.

## Flujo terminado de invitación
1. Cada persona inicia sesión y completa su perfil.
2. En Perfil aparece su código persistente de seis caracteres.
3. Una persona agrega el código de la otra: se crea una solicitud PENDIENTE.
4. La destinataria recibe la solicitud y puede aceptar o rechazar. La emisora puede cancelarla.
5. La aceptación permite consulta mutua del perfil de salud, citas y medicamentos. Cada cuenta conserva la escritura exclusiva de sus registros.
6. Cualquiera puede desvincularse; las reglas retiran nuevas lecturas del servidor.
7. Se admiten varias solicitudes a personas distintas. Se bloquean invitaciones propias, códigos inexistentes, duplicados activos e invitaciones cruzadas activas.
8. El vínculo persiste entre sesiones y dispositivos. Las listas proceden de snapshots confirmados, sin insertar personas ficticias.

## Privacidad y hardware
usuarios/{uid} es privado. perfilesCompartidos/{uid} contiene salud, sin correo, teléfonos, contacto de emergencia, preferencias ni URI locales. codigos solo contiene UID y nombre; no se permite listar el directorio.
La ubicación requiere vínculo aceptado y un consentimiento separado de compartir; solo se publica con el mapa abierto. No hay seguimiento permanente en segundo plano.
Las fotos y los documentos adjuntos permanecen locales. AdjuntosLocales los separa por cuenta y registro; sus URI no se suben como si fueran archivos disponibles en otro teléfono.
El detector de impactos se limita al primer plano y no constituye un sistema clínico. La acción de emergencia abre el marcador con el contacto configurado; no inventa un número.

## Verificación realizada
- Compilación final :app:assembleDebug: BUILD SUCCESSFUL en 17 s, tras los últimos cambios. APK en app/build/outputs/apk/debug/app-debug.apk. Permanecen advertencias de Kotlin sobre nullabilidad y destinos Composable de Mapbox; no impidieron generar el APK y el mapa necesita validación visual en dispositivo.
- 24/24 pruebas de Firebase Emulator Suite aprobadas, incluyendo validadores create/update, permisos, duplicados concurrentes, múltiples peticiones y retirada de acceso.
- Compilación remota de reglas y despliegue completados.
- Prueba en Firebase real con dos cuentas temporales: registro, reserva de códigos, recepción en tiempo real, aceptación, lectura de salud/cita, protección de cuenta privada, cierre/reinicio de sesión y revocación: aprobada.
- Las dos cuentas temporales de Auth y sus ocho documentos de prueba se eliminaron. No se borraron datos existentes.
- La prueba real utilizó el SDK cliente de Firebase en dos sesiones independientes; no sustituye una prueba de interfaz en dos teléfonos.

## Cómo comprobarlo en dos teléfonos
Instalar la misma versión actualizada del APK en ambos dispositivos. Iniciar sesión con cuentas diferentes y completar cada perfil. En Perfil, copiar el código de B; desde A agregar ese código. En B aceptar la solicitud recibida. Abrir a la persona vinculada y comprobar sus registros. Registrar una cita desde la cuenta titular y comprobar que la otra la ve. Cerrar sesión y volver a entrar; después desvincular y verificar que desaparece el acceso.

## Límites y pendientes fuera del flujo terminado
- Falta la prueba manual de interfaz y permisos en teléfonos físicos, incluidos rechazo permanente, cámara y cambios de configuración.
- Las peticiones aparecen dentro de la aplicación; no se implementaron notificaciones push de invitaciones.
- No se implementó subida de fotos o adjuntos a Firebase Storage.
- El permiso de notificaciones no equivale a haber implementado programación de alarmas o un servicio de recordatorios en segundo plano.
- No se validó una distribución amplia en producción ni todo el alcance funcional de la entrega académica; este informe cubre autenticación, invitaciones, persistencia relacionada y los accesos a hardware revisados.
- Los datos ya descargados pueden permanecer en cachés locales; revocar bloquea nuevas lecturas del servidor, no borra copias que otra persona ya obtuvo.

## Nota sobre las reglas del prototipo
I've set up prototype Security Rules to keep the data in Firestore safe. They are designed to be secure for authenticated ownership, explicit invitation acceptance, private account separation, and validated writes. However, you should review and verify them before broadly sharing your app. If you'd like, I can help you harden these rules.
