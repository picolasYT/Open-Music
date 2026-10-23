# Verificación — Open Music 1.0.0

Fecha: 21 de octubre de 2026. Autor: Picolas.

## Compilación y pruebas de lógica

- Compilación real con JDK 17, Android SDK 35 y Gradle 8.13 / Android Gradle Plugin 8.9.2.
- `assembleDebug`: APK generado y firmado, 6,1 MB aproximadamente.
- `testDebugUnitTest`: 9 pruebas, 0 errores y 0 fallos. Incluyen restricciones de acceso y descarga, JSON de pistas offline, rechazo de permisos de audio importados y letras LRC.
- `lintDebug`: 0 errores. Persisten advertencias de localización, servicio exportado para MediaSession, backups y algunas asignaciones en dibujo. No se ha ocultado un baseline de errores.
- Firma APK v2 verificada con apksigner; applicationId com.picolas.openmusic, minSdk 26, targetSdk 35.
- Puente Node: 2 pruebas de lógica aprobadas y 4 rutas HTTP comprobadas. Sin dependencias npm de producción; npm audit no reportó vulnerabilidades.

Los registros originales están en `docs/validation/`.

## Servicios públicos comprobados

Se hicieron consultas reales a Audius para tendencias, canciones, artistas y playlists. Se validó un stream público como MP3 de 48 kHz estéreo y una descarga habilitada por el artista como WAV PCM de 44,1 kHz estéreo. Solo se solicitaron 64 KiB por archivo para verificar formato y respuesta, sin redistribuir música dentro del proyecto.

LRCLIB devolvió metadatos y letras simples/sincronizadas en una consulta real. La instalación de spotDL 4.5.2, su CLI y las firmas de su parser y autenticación oficial se comprobaron en un entorno aislado.

## Alcance de los proveedores opcionales

No se suministraron credenciales válidas de Spotify ni Jamendo: no se ha probado su catálogo autenticado ni una exportación en vivo de Spotify. La app incluye configuración y manejo de errores, el puente oficial y el importador .spotdl. La compatibilidad de Spotify queda sujeta a los permisos de la aplicación que registres allí.

La revisión automática bloqueó la prueba de extracción web de tokens del wrapper original de Spotify. La entrega elimina ese método; el adaptador requiere credenciales oficiales del propietario del servidor.

## Android en ejecución

La instalación real del APK y del paquete de pruebas en emulador Android 9 fue exitosa. Las 2 pruebas de instrumentación pasaron: persistencia de biblioteca, integridad al borrar playlists, reproducción de un WAV local real, avance de posición y seek, navegación por reproductor/biblioteca/offline/ajustes/búsqueda y reproducción continua al pasar la Activity a segundo plano. Se repitieron después de corregir el ancho de las listas y revisar las seis pantallas a 360 dp de ancho. El reproductor completo usa una vista propia sin el minirreproductor ni la navegación superpuesta; los controles quedan visibles en una pantalla de teléfono de 360 × 800. La última ejecución sobre el APK de entrega terminó con 2 pruebas aprobadas en 56,359 segundos. No se simularon los controles del reproductor.

No se ha probado en un teléfono físico, con Bluetooth real, bajo todos los fabricantes ni con cada versión de Android. No se realizó una auditoría completa de seguridad de las dependencias. La firma de desarrollo permite instalación directa, pero no reemplaza una firma de publicación propia para Google Play.
