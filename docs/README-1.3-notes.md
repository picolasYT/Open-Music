# Open Music 1.3

El reproductor incrustado de YouTube fue eliminado. Los resultados de YouTube ahora se reproducen con el mismo reproductor nativo Media3 que el resto de las fuentes. Se incluye youtube-dl y un intérprete Python dentro del APK mediante Chaquopy; el usuario no instala Python, Termux, FFmpeg ni un servidor.

El audio se resuelve al abrir una pista de la cola. El modelo persistente guarda un ID de video, no una URL firmada ni cabeceras. Las URLs caducan, se conservan solo en memoria y se vuelven a resolver al abrir una pista o reintentar un error. Los formatos por rangos de un archivo completo que entrega youtube-dl se validan y se reproducen desde su URL base, con los Range requests de Media3. Los formatos segmentados por tiempo no se hacen pasar por archivos progresivos.

Controles nativos: play/pausa, seek, siguiente/anterior, cola, aleatorio, repetición, notificación y segundo plano. Se mantienen favoritos, playlists, respaldos, historial, descargas autorizadas y archivos locales. La extracción de una URL no se considera permiso de descarga: no se activa automáticamente descarga offline para YouTube.

Compilación: JDK 17, SDK 35 y Python 3.11 en el equipo de desarrollo. `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Si hace falta, usar `-PopenMusicBuildPython=/ruta/a/python3.11`. El APK universal incluye armeabi-v7a, arm64-v8a y x86_64 y requiere Android 8.0+. Ocupa más por incluir el runtime y el extractor; Python se inicia en el hilo de carga, al necesitarlo, no al dibujar la interfaz.

La app no pide cookies ni credenciales de YouTube y no desactiva verificación TLS. Un video que requiera verificación, permisos adicionales o DRM no se reproduce mediante este adaptador. Se muestran errores y reintento en el reproductor nativo; no se abre automáticamente un player externo.
