# Verificación 1.5

Compilación local: assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest. 34 pruebas unitarias aprobadas, sin errores de lint. Se añadieron pruebas de afinidad, variedad, exclusión y deduplicación; límites del QR, lectura real con ZXing, rechazo de perfiles malformados y bombas de descompresión, y eliminación de permisos/rutas falsificadas.

La migración SQLite pasa de versión 1 a 2 agregando únicamente metadatos de playlists. Conserva applicationId com.picolas.openmusic.next, firma de desarrollo, favoritos, historial y canciones. El tutorial se guarda en preferencias locales y es repetible desde Ajustes.

La suite 1.5 terminó OK (10 tests) en Android 15: controles AAC, audio local, segundo plano, error visible, HTTPS, biblioteca, respaldo, cancelación de descargas, migración, edición/orden de playlists y tutorial inicial. La CI con aceleración pasó otras 4 comprobaciones enfocadas, incluida la copia de una foto PNG y el render de la playlist. La revisión visual examinó Inicio, Perfil y Buscar.

1.5.1 corrige el QR de listas con nombres repetidos y tolera imágenes sin EXIF; pasó 34 pruebas unitarias y lint sin errores. La comprobación enfocada sobre el APK 1.5.1 está en ejecución; se conserva el informe anterior como evidencia de regresión.

El scanner usa ZXing y solicita permiso de cámara al escanear. El QR no depende de un servidor; sus datos no prueban identidad ni se actualizan automáticamente. Un perfil local por instalación. No se verificó lectura óptica entre teléfonos físicos ni audio online de YouTube de extremo a extremo en esta entrega.
