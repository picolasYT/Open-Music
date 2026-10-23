# Verificación 1.5

Compilación local: assembleDebug, testDebugUnitTest, lintDebug, assembleDebugAndroidTest. 33 pruebas unitarias aprobadas, sin errores de lint. Se añadieron pruebas de afinidad, variedad, exclusión y deduplicación; límites del QR, lectura real con ZXing, rechazo de perfiles malformados y bombas de descompresión, y eliminación de permisos/rutas falsificadas.

La migración SQLite pasa de versión 1 a 2 agregando únicamente metadatos de playlists. Conserva applicationId com.picolas.openmusic.next, firma de desarrollo, favoritos, historial y canciones. El tutorial se guarda en preferencias locales y es repetible desde Ajustes.

Pruebas instrumentadas de esta entrega: en ejecución al preparar el código. El informe final se añadirá tras terminar Android.

El scanner usa ZXing y solicita permiso de cámara al escanear. El QR no depende de un servidor; sus datos no prueban identidad ni se actualizan automáticamente. Un perfil local por instalación. No se verificó lectura óptica entre teléfonos físicos ni audio online de YouTube de extremo a extremo en esta entrega.
