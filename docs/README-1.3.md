# Open Music

Reproductor Android nativo creado por **Picolas**. Una interfaz negra y coral, inspirada en la referencia Raycast de Refero, con identidad y navegación propias.

## Versión 1.3 — reproducción nativa con youtube-dl

Las pistas de YouTube ahora se reproducen con **el reproductor de Open Music**: controles nativos, cola, seek, notificación y segundo plano. Se eliminó YouTubeActivity, el WebView y la integración IFrame. No se abre un reproductor externo al tocar Play.

El APK incluye **youtube-dl** (snapshot fijado 956b8c585591b401a543e409accb163eeaaa1193) y Python 3.11 mediante **Chaquopy 17.1.0**. No necesitás instalar Python, Termux, FFmpeg ni desplegar un servidor. El extractor trabaja en el hilo de carga de Media3, al reproducir una pista; la interfaz sigue disponible mientras resuelve el audio.

Las búsquedas siguen combinando YouTube, Audius y archivos locales, con resultados parciales. El adaptador Android de yt-search y el puente opcional de búsquedas se mantienen. Favoritos, playlists y respaldos guardan IDs de canciones; los enlaces de audio y sus cabeceras son temporales y no se escriben en la biblioteca.

**La 1.3 actualiza la 1.2 y la 1.1 sin desinstalar:** conserva applicationId y firma de desarrollo. La antigua 1.0 permanece separada por su clave anterior.

El audio de una pista puede fallar si YouTube requiere verificación, acceso adicional o cambia su respuesta. La app muestra el error y permite reintentar; no inventa una reproducción ni cambia automáticamente al player de YouTube. Resolver una URL no habilita descargas offline: los permisos de descarga siguen separados de la reproducción.

La ruta nativa de audio pasó las pruebas en Android con AAC controlado y youtube-dl resolvió un video real de Milo J. Esta red bloqueó los bytes del CDN, por lo que la reproducción en vivo de YouTube no quedó verificada de extremo a extremo. Ver `docs/VERIFICATION.md` para el alcance y los informes.

## Instalar

1. Descargá `Open-Music-1.3.apk` al teléfono.
2. Abrilo y habilitá la instalación desde esa aplicación si Android lo solicita.
3. Requiere Android 8.0 o posterior. El APK universal admite armeabi-v7a, arm64-v8a y x86_64. Las fuentes online y las letras necesitan Internet.

No requiere un servidor propio para el catálogo Audius, los archivos locales, favoritos, playlists, historial y reproducción en segundo plano. El APK de entrega está firmado para instalación directa con la clave de desarrollo de este proyecto; no es una publicación de Google Play.

La versión 1.1 se instala como **Open Music 1.1**, con identificador `com.picolas.openmusic.next`, en paralelo a la 1.0. La clave de desarrollo original de 1.0 no quedó disponible para esta continuación; Android no permite actualizar una app con una firma diferente. No borres la 1.0 para instalar esta versión. Sus datos permanecen separados y no se migran automáticamente. El respaldo JSON se agrega en 1.1; la 1.0 no tiene exportador de respaldos.

La firma de desarrollo de 1.1–1.3 se conserva en `signing/configured-privately.jks` para poder actualizar las próximas entregas. Es una clave de prueba incluida deliberadamente con el código, con contraseña `configured-privately` y alias `openmusic`; no debe usarse para publicar una aplicación de producción.

## Novedades de 1.1

- Offline muestra bytes descargados, porcentaje cuando Android informa el tamaño total, estado de espera y cancelación por canción. Se actualiza cada dos segundos y muestra automáticamente las descargas completadas.
- Ajustes permite exportar y restaurar favoritos y playlists con el selector de archivos de Android. La restauración valida el archivo completo y combina listas del mismo nombre sin borrar datos ni duplicar canciones.
- Un respaldo contiene metadatos, hasta 5000 entradas y 500 playlists. No incluye audio, claves, URLs de reproducción ni permisos sobre archivos del dispositivo. En otro dispositivo, se vuelve a buscar el audio o se importan los archivos locales. Si una canción ya está guardada en el dispositivo, conserva sus datos y acceso anteriores.
- Botones para reintentar tras errores de conexión en descubrimiento, búsquedas y colecciones de Audius.

## Funciones reales

- Búsqueda de música en YouTube y resolución de audio con youtube-dl incluido en el APK, reproducido por Media3.
- Catálogo, descubrimiento y búsqueda de canciones en Audius. Búsqueda adicional de artistas y playlists públicas de Audius y acceso a sus canciones disponibles.
- Jamendo configurable con un client_id propio; búsqueda por nombre y descargas únicamente cuando `audiodownload_allowed` es verdadero.
- Archivos locales a través del selector de Android: permisos persistentes, metadatos y carátulas embebidas. Los archivos quedan en su ubicación original.
- ExoPlayer / AndroidX Media3: audio en segundo plano, notificación multimedia, controles de auriculares, pausa al desconectarlos y foco de audio.
- Reproductor completo, seek, siguiente/anterior, aleatorio, repetición de una o de toda la cola, edición de cola y temporizador de apagado.
- Favoritos, playlists con creación/renombrado/eliminación y canciones agregadas o quitadas; historial persistente en SQLite.
- Recomendaciones sencillas y transparentes según el género de tus escuchas y favoritos. Sin cuentas, perfilado remoto ni IA simulada.
- Letras simples o sincronizadas de LRCLIB, caché local y salto al tocar una línea. No todas las canciones tienen letras.
- Descargas públicas mediante Android DownloadManager: continúa el trabajo fuera de la aplicación y conserva su estado. El permiso se vuelve a consultar antes de iniciar. No se descargan pistas restringidas.
- Recuperación de cola y posición al abrir de nuevo. No inicia audio automáticamente al abrir.
- Importación del formato `.spotdl` para traer playlists y metadatos; al tocar una entrada se busca audio en los proveedores disponibles.

## Proveedores y límites

| Proveedor | Uso | Configuración |
|---|---|---|
| YouTube | Audio nativo con youtube-dl; búsqueda de canciones | Activo de inicio, sin servidor propio ni reproductor web. Los permisos de descarga no se infieren de los streams. |
| Audius | Audio completo público, búsqueda de canciones/artistas/playlists, descubrimiento | Activo de inicio. Se excluyen pistas bloqueadas, eliminadas o con acceso condicionado. |
| Jamendo | Audio y descargas permitidas por el artista | Ajustes → Proveedores → client_id propio. |
| Archivos locales | Audio offline y carátulas/metadatos embebidos | Biblioteca → Importar música. |
| Spotify | Metadatos opcionales a través de un puente HTTPS | Servidor `bridge/`, credenciales oficiales propias. No proporciona audio de Spotify. |
| spotDL | Importación y gestión de playlists/metadatos | Archivo `.spotdl`; exportador oficial opcional en `tools/`. |
| LRCLIB | Letras, cuando existen | Sin clave; requiere conexión la primera vez. |

No incluye el catálogo completo de Spotify ni promete encontrar una versión autorizada de cualquier canción comercial. Una licencia open source del programa no concede derechos sobre la música. Los términos de cada servicio y los permisos de sus artistas siguen aplicando. Los servicios pueden cambiar, limitar el acceso o dejar de responder.

## Compilar en Android Studio

Abrí esta carpeta como proyecto Gradle con un **JDK 17 completo**. Instalá Android SDK 35 y Build Tools 35.0.0. Android Studio puede instalar los componentes que falten. También necesitás Python 3.11 en el equipo que compila; es independiente del Python ya incluido en el APK. Si no se detecta automáticamente, pasá `-PopenMusicBuildPython=/ruta/a/python3.11`.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

En Windows: `gradlew.bat :app:assembleDebug`. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas en dispositivo se ejecutan con un teléfono o emulador conectado:

```sh
./gradlew :app:connectedDebugAndroidTest
```

La integración continua en `.github/workflows/android.yml` compila el APK y corre las pruebas de lógica. No se ha creado ni publicado un repositorio por esta entrega.

## YouTube: puente opcional y youtube-dl

Este puente es opcional y se usa para búsquedas y metadatos. El audio se resuelve dentro del APK; no requiere este servidor. Si lo desplegás, ingresá su URL HTTPS en Ajustes → Proveedores → Puente yt-search.

```sh
cd bridge
npm ci --ignore-scripts
npm start
```

Requiere Node 22.12 o superior. `GET /youtube/search?q=milo%20j` usa el parser del paquete yt-search; `GET /health` informa las capacidades. Se mantiene el lockfile y un override compatible de minimatch 3.1.4 para corregir una vulnerabilidad transitiva de la versión original. El respaldo youtube-dl se activa configurando `YOUTUBE_DL_PYTHON` con la ruta absoluta del intérprete donde se instaló `tools/requirements-youtube.txt`. El proceso usa argumentos separados, sin shell, con límites de tiempo y tamaño; no descarga medios.

El Dockerfile incluye ambos motores. Desde la raíz del proyecto:

```sh
docker build -f bridge/Dockerfile -t openmusic-bridge .
docker run --rm -p 3000:3000 openmusic-bridge
```

Para exportar e importar una búsqueda o playlist pública:

```sh
python -m pip install -r tools/requirements-youtube.txt
python tools/export_youtube_dl.py --query "milo j" -o milo.info.json
python tools/export_youtube_dl.py --url "https://www.youtube.com/playlist?list=ID" -o lista.info.json
```

Importá el JSON desde Ajustes → Importar youtube-dl. Se fijó el commit 956b8c585591b401a543e409accb163eeaaa1193 y se verificaron búsquedas reales; los mantenedores marcan la release estable 2021.12.17 como antigua y con problemas conocidos. La licencia del programa no concede derechos sobre la música. No se eluden bloqueos, cuentas o verificaciones del servicio.

## Puente opcional de Spotify

El puente adapta el contrato de búsqueda del proyecto GFrancV/spotify-search-wrapper y elimina la extracción de tokens desde la web de Spotify. Agrega codificación de consultas, validación, expiración del token, límites de concurrencia, caché y tiempos de espera. Es un adaptador de metadatos, separado de las fuentes de audio.

```sh
cd bridge
npm ci
# Configurá SPOTIFY_CLIENT_ID y SPOTIFY_CLIENT_SECRET en las variables del servidor.
npm start
```

Desplegalo detrás de HTTPS e ingresá su dirección en Ajustes → Proveedores. `GET /health` indica si el servidor tiene credenciales; `GET /search?q=...` busca canciones. Las claves nunca van dentro del APK. Para instalaciones públicas, aplicá autenticación y límites por usuario en tu pasarela; el límite básico incluido usa la IP del socket y puede agrupar usuarios detrás de un proxy.

El token de Client Credentials tiene las restricciones de acceso que Spotify aplique a tu aplicación. La búsqueda en vivo de Spotify requiere tus credenciales y no pudo verificarse sin ellas. El método original del wrapper no se ejecutó porque la revisión automática bloqueó la extracción de tokens desde la página web.

## spotDL, adaptado a la gestión de biblioteca

La importación nativa acepta los archivos exportados por `spotdl save`. El exportador incluido utiliza la API de metadatos de spotDL 4.5.2 en modo **oficial** y no llama al descargador de YouTube:

```sh
python -m pip install -r tools/requirements.txt
# Configurá SPOTIFY_CLIENT_ID y SPOTIFY_CLIENT_SECRET en el entorno.
python tools/export_spotdl.py https://open.spotify.com/playlist/ID -o playlist.spotdl
```

Importá el archivo desde Biblioteca. El audio que ya tengas autorizado también puede importarse como archivo local. spotDL no corre dentro del APK: requiere Python y otras dependencias; la interoperabilidad evita meterlas en el teléfono o prometer descargas de Spotify.

## Proyecto

`app/` contiene la aplicación. `bridge/` contiene el proveedor opcional de metadatos. `tools/` contiene el exportador spotDL. `docs/` describe arquitectura, diseño, proveedores y verificación. Los créditos y textos de licencia de las bibliotecas integradas también están disponibles dentro de Ajustes.

Código propio: licencia MIT, autor Picolas. Ver `LICENSE` y `app/src/main/assets/THIRD_PARTY_NOTICES.txt`.
