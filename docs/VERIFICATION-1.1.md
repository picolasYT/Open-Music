# Verificación — Open Music 1.1.0

Fecha: 21 de octubre de 2026.

## Compilación y firma

Se compiló el APK real con JDK 17, Android SDK 35, Build Tools 35.0.0, Gradle 8.13 y AGP 8.9.2. assembleDebug, testDebugUnitTest, lintDebug y assembleDebugAndroidTest completaron correctamente. Lint reportó 0 errores y 10 advertencias, sin baseline de errores. Los registros y el informe de lint están en validation/.

La firma APK v2 se verificó con apksigner. applicationId: com.picolas.openmusic.next; versionCode: 2; versionName: 1.1.0; minSdk: 26; targetSdk: 35. Certificado SHA-256: db235fcb26e84a58ab5b7d7abbe866bb0cad90eaeb820e0a9966a0681da45bed.

La versión 1.1 se instala en paralelo a 1.0 porque la clave de desarrollo original no está disponible. No modifica su instalación ni migra su base de datos automáticamente. La nueva clave de prueba se conserva con el proyecto para futuras actualizaciones. No es una firma de publicación de Google Play.

## Pruebas ejecutadas

- 12 pruebas JVM aprobadas: restricciones de acceso y descarga de proveedores, formato spotDL, serialización de pistas offline, letras LRC y políticas del respaldo. Incluyen rechazo de versiones/identificadores inválidos, límite de entradas y descarte de URI y permisos falsificados.
- 2 pruebas Node del puente opcional aprobadas. No cambió su integración con Spotify en esta versión.
- APK y paquete de pruebas instalados realmente en emulador Android 9. 4 pruebas de instrumentación aprobadas en 49,203 segundos.
- Biblioteca SQLite: guardado, favoritos, playlists, deduplicación y conservación de favoritos al borrar una lista.
- Respaldo: exportación sin rutas privadas ni audio, restauración repetida sin duplicar, conservación de audio confiable ya existente y rechazo completo de un archivo malformado sin cambios parciales.
- Descargas: trabajo real en Android DownloadManager con una URL de prueba y restricción de inactividad para mantenerlo pendiente sin obtener música; consulta desde Offline y cancelación pulsando el control nativo. Se verificó que se borrara el trabajo y se conservara el favorito. Se rechazó un ID ajeno. Esta prueba no mide velocidad de descarga en Internet.
- Reproducción real de un WAV generado temporalmente por las pruebas, seek y avance de posición, persistencia de historial y continuación en segundo plano. El audio de prueba no se incluye en el APK.
- Navegación y validación de ancho de listas en las pantallas nativas. Las capturas se usan solo para revisión interna.

## Proveedores y límites

No se modificaron los protocolos de Audius, Jamendo, LRCLIB ni el puente oficial de Spotify en esta continuación. Las consultas públicas y las comprobaciones del catálogo y audio de la entrega anterior se documentan en VERIFICATION-1.0.md y validation-1.0/. No se repiten aquí como pruebas nuevas. Jamendo y Spotify siguen requiriendo credenciales propias; su catálogo autenticado no se ha probado sin ellas.

No se probó en un teléfono físico, con Bluetooth real ni en todas las versiones/fabricantes de Android. No se realizó una auditoría completa de seguridad de dependencias. El respaldo es de metadatos: no transfiere música ni permisos de documentos entre dispositivos.
