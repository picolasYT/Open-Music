# Arquitectura

Aplicación Android nativa en Java 17, minSdk 26 y targetSdk 35. Los controles visuales son Views y RecyclerView: no necesita WebView, un sitio desplegado ni un servidor para el uso principal.

## Contrato de proveedores

`MusicProvider` define `search`, `discover` y `refresh`. `Track` normaliza identificador, proveedor, título, artista, carátula, duración, fuente, licencia, URI local y capacidades de streaming/descarga. Los IDs se componen como `provider:id`, evitando mezclar canciones diferentes con el mismo identificador.

`AudiusProvider` adapta las respuestas actuales de las APIs públicas de Audius, incluyendo la diferencia entre una respuesta singular y una lista. Solo concede stream cuando existe un permiso público explícito. Una descarga requiere además permiso público independiente y URL emitida por el servicio. No se resuelven ni se eluden las condiciones de contenido bloqueado.

`JamendoProvider` conserva `audiodownload_allowed`, la URL de licencia y la atribución al artista. Renueva los datos antes de una descarga. `SpotifyMetadataProvider` jamás concede derechos de audio: solo muestra metadatos del puente opcional. `SpotdlImport` no confía en URLs de streaming o permisos declarados en JSON importado.

El proveedor de archivos locales usa el Storage Access Framework. Los permisos persistentes reemplazan el acceso amplio al almacenamiento. SQLite guarda la biblioteca; los permisos no se transfieren a otro teléfono.

## Audio y sistema Android

`PlaybackService` es un MediaSessionService de Media3. Aloja ExoPlayer, audio focus, controles del sistema y un temporizador. La Activity se conecta mediante MediaController. La cola y la posición se persisten y se restauran en pausa. El historial se actualiza cuando una canción comienza realmente a reproducirse.

DownloadManager administra las descargas fuera del proceso. `Downloads` conserva sus IDs en SQLite y concilia archivos completados o fallidos al volver a la app y al consultar Offline. Los archivos descargados quedan en la carpeta privada externa de la app; Android los elimina al desinstalar.

## Datos y red

MusicStore usa SQLite con claves foráneas y consultas parametrizadas. Eliminar una playlist no borra canciones ni favoritos. Las recomendaciones ponderan géneros del historial (1) y favoritos (3), y solicitan tendencias de ese género; no se presentan como un motor complejo de personalización.

OkHttp aplica límites de conexión, lectura y tiempo total. Las consultas corren en un executor; un identificador de generación impide que una respuesta de una búsqueda anterior sustituya a la nueva pantalla. Glide gestiona carátulas y caché. Las letras de LRCLIB tienen caché local y un parser LRC de múltiples marcas temporales.

## Extender

Implementá MusicProvider y normalizá las capacidades desde los permisos reales del servicio. Agregá su configuración a Ajustes y su búsqueda al agregador. La reproducción debe recibir una URL autorizada o una URI local. Proveedores con cuentas, suscripciones o DRM necesitan su SDK oficial y un flujo de autenticación; no pueden convertirse en fuentes libres de MP3.

## Próximas versiones

La base y el contrato admiten Room, WorkManager, pantallas Compose, OAuth por usuario y nuevos proveedores sin cambiar el contrato de pista. Esta versión usa SQLite y DownloadManager para mantener baja la complejidad y funcionar sin infraestructura propia.

## Biblioteca portátil y descargas (1.1)

`LibraryBackup` define un esquema JSON versionado de metadatos. El parser limita tamaño, entradas, nombres e identificadores y elimina todas las capacidades/URIs de audio. La restauración valida todo antes de abrir una transacción SQLite. Combina por nombre de playlist y clave de canción, conserva registros confiables ya existentes y no reemplaza archivos. Los identificadores locales se convierten a SHA-256, sin exponer rutas ni URI de documentos.

`Downloads` sincroniza enqueue, reconciliación y cancelación. Solo permite cancelar IDs que pertenezcan a la tabla local de trabajos. El panel Offline consulta Android DownloadManager en el ejecutor de IO cada dos segundos; actualiza las filas sin reconstruir la lista en cada avance. No estima porcentajes si el servidor no informa el tamaño. La descarga sigue administrada por Android al cerrar la Activity.

## Proveedor YouTube (1.3)

YouTubeProvider sigue buscando metadatos con el enfoque ytInitialData de yt-search. Track.canPlay distingue pistas resolubles por ID de streams ya disponibles; no modifica los permisos de descarga.

PlaybackService construye MediaItems con URI lógica openmusic-youtube:///VIDEO_ID. YouTubeAudio conecta ResolvingDataSource y el youtube-dl incluido mediante Chaquopy. La resolución corre en el loader de Media3; la URI lógica se transforma en una dirección HTTPS temporal de audio de Googlevideo y cabeceras permitidas. AudioAddress valida dominio, formato, caducidad y cabeceras, y conserva capacidades solo en memoria. Un error invalida la caché de esa pista para que Play reintente con una resolución nueva. La caché no bloquea la UI mientras hay solicitudes de red.

openmusic_audio.py usa el YoutubeIE original sin descargar archivos, sin credenciales/cookies de usuario y sin desactivar TLS. Selecciona formatos de audio compatibles no DRM. Reconoce también el protocolo http_dash_segments cuando describe rangos contiguos de un mismo archivo completo; entrega la URL base original a Media3 para lectura y seek HTTP. Los segmentos por tiempo u otros recursos no se presentan como archivos progresivos.

El mismo ExoPlayer y MediaSessionService gestionan reproducción, segundo plano, notificación, cola, historial, temporizador y foco de audio de todas las fuentes. No existe YouTubeActivity ni WebView/IFrame en la aplicación. Los backups y la cola guardan IDs estables; no guardan URLs firmadas ni cabeceras. El código youtube-dl vendorizado permanece sin modificaciones y se registra en YOUTUBE_DL_VENDOR.json.

## Actualización 1.4

El apartado 1.3 anterior es histórico. NewPipeBackend reemplaza el runtime Python por NewPipeExtractor v0.26.5. YouTubeAudio conserva ResolvingDataSource, caché en memoria, vencimientos y URIs lógicas estables; NewPipeBackend selecciona audio progresivo M4A/WebM y rechaza manifiestos presentados como archivos. El archivo AudioAddress valida dominio, formato y cabeceras.

YouTubeProvider prueba primero el extractor de búsqueda y conserva el parser HTML de yt-search como alternativa. La biblioteca y el esquema SQLite no cambian. PlaybackService usa el mismo Media3 para todas las fuentes y rechaza HTML/JSON que lleguen con HTTP 200/206. Los errores invalidan la dirección temporal de esa pista. MainActivity consulta player.getPlayerError() antes de mostrar estados normales, evitando perder el error en onEvents.

AudioDiagnostics conserva en memoria el último resultado de esa sesión. No registra URLs, cabeceras, cuentas ni cookies. Ajustes permite copiarlo. No hay Python, Chaquopy, WebView ni IFrame en el motor de reproducción de 1.4. Los exportadores Python y el puente Node son herramientas externas opcionales de metadatos.
