# Verificación de Open Music 1.3

Validación realizada el 21 de octubre de 2026. Esta entrega sustituye el player web de YouTube por el reproductor nativo de Open Music. Los informes de 1.1 y 1.2 describen entregas anteriores.

## Compilación y paquete

JDK 17, Gradle 8.13, Android Gradle Plugin 8.9.2, SDK/target 35, minSdk 26, Chaquopy 17.1.0 y Python 3.11.17. El extractor youtube-dl está fijado al commit registrado en YOUTUBE_DL_VENDOR.json; su versión interna es 2025.04.07. El módulo upstream se incluye sin cambios.

Se generó un APK universal instalable para armeabi-v7a, arm64-v8a y x86_64, con applicationId `com.picolas.openmusic.next`, versionCode 4 y versionName 1.3.0. La firma de desarrollo se conserva desde 1.1. Apksigner verifica la firma y zipalign comprueba alineación de 16 KB. El runtime Python aumenta el APK a aproximadamente 58 MiB; el usuario no instala Python ni un servidor.

## Pruebas automatizadas

- 20 pruebas JVM: políticas de proveedores y permisos, respaldos, letras, parser de búsqueda de YouTube y validación de direcciones de audio temporales.
- 5 pruebas Python del selector de audio: formatos compatibles, exclusión de DRM/acceso condicionado, eliminación de credenciales, cabeceras y validación de rangos contiguos de un mismo archivo completo.
- Lint: cero errores y 11 advertencias de recursos/textos y otros avisos no bloqueantes, conservadas en el informe.
- 5 pruebas instrumentadas en Android 9/API 28 x86_64: persistencia de favoritos/playlists, respaldo transaccional, cancelación real de DownloadManager, audio local con seek e historial y continuidad al pasar a segundo plano, e integración del runtime Python con la ruta nativa de audio YouTube.

La prueba nueva arranca el Python incluido en el APK y carga el youtube-dl real. Ejecuta el selector Python, valida su resultado en Java y conecta el mismo ResolvingDataSource a ExoPlayer/MediaSession/MediaController. Toca una fila real de la interfaz y verifica inicio, pausa, seek, dos pistas en la cola y paso a la segunda. Para aislar la red utiliza un archivo AAC/M4A generado para la prueba y una fábrica de datos inyectada en el test. Ese audio solo pertenece al APK de pruebas; no está en el APK de entrega. No se sustituyen controles por botones simulados en la aplicación.

La prueba local verifica por separado la continuidad del PlaybackService real en segundo plano. El test del adaptador YouTube utiliza una MediaSession de prueba para entregar AAC de manera controlada; no demuestra reproducción en vivo desde Googlevideo.

## Prueba online y límite de esta red

El adaptador real openmusic_audio.py, con el mismo código youtube-dl vendorizado y Python 3.11, resolvió el video `aBSkvI0CkgU` de Milo J a un formato M4A. Se validaron dominio, formato, cabeceras y vencimiento sin guardar la URL firmada en los informes.

La posterior solicitud de bytes al CDN devolvió una página HTML «Site Unavailable» del filtro de red de este entorno, en lugar de audio. Por ello **no se verificó de extremo a extremo la reproducción en vivo de YouTube**, ni en este host ni en un teléfono físico. No se intentó eludir el filtro. La extracción exitosa de una URL no se cuenta como reproducción exitosa. La disponibilidad desde otra conexión depende de la fuente y sus restricciones; la app muestra errores y permite reintentar dentro del player nativo.

## Actualización y datos

Se instaló 1.2, se preparó una base con favorito y playlist y se actualizó a 1.3 con `adb install -r`. La base preservó exactamente sus 61440 bytes antes del primer inicio; el hash y resultado constan en validation-1.3/upgrade-preservation.json. No cambian los identificadores de pistas. Las URLs firmadas y cabeceras se guardan solo en memoria, no en favoritos, cola persistida ni respaldos. YouTube no recibe permiso de descarga offline por resolver una URL de reproducción.

No hay YouTubeActivity, WebView ni IFrame en el código Android de esta entrega. Se mantienen los accesos explícitos «Abrir fuente» y la búsqueda web como opciones de navegación elegidas por el usuario, sin usarlos automáticamente para reproducir.

## Alcance

Las funciones previas de Audius, Jamendo, LRCLIB, archivos locales y puentes opcionales se conservan; sus verificaciones históricas están en los documentos de las versiones anteriores. No se repitieron consultas con credenciales de Spotify o Jamendo, no disponibles en este entorno. No se publicaron servidores ni se configuraron cuentas de terceros.

Las pruebas en un único emulador no garantizan compatibilidad con todos los fabricantes, restricciones de batería ni versiones de Android. No se ensayó esta entrega en hardware ARM ni en Android 15/16. El APK está firmado para instalación directa de desarrollo, no publicado en Google Play. Informes y evidencia: `docs/validation-1.3/`.
