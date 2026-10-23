# Proyectos y fuentes revisados — 21 de octubre de 2026

| Proyecto / servicio | Decisión | Motivo y licencia |
|---|---|---|
| androidx/media, Media3 1.8.0 | Integrado en el APK | ExoPlayer, sesión y reproducción en segundo plano. Apache-2.0. Se fija una versión estable compilada con SDK 35. |
| square/okhttp 4.12.0 | Integrado en el APK | Cliente HTTP compartido con el reproductor, límites de tiempo. Apache-2.0. |
| bumptech/glide 4.16.0 | Integrado en el APK | Carga y caché de carátulas, recursos de error, reciclado de vistas. BSD/MIT/Apache; texto completo en assets. |
| tranxuanthang/lrclib | API integrada, servidor no incluido | Letras simples y LRC sincronizado, MIT para el código del servidor; los derechos de las letras son independientes. |
| AudiusProject/apps | API pública integrada con un adaptador nativo | Catálogo y streams publicados por los usuarios, permisos de acceso y descarga independientes. No se copia la aplicación del servicio. |
| Jamendo API v3 | Adaptador nativo integrado | Descarga condicionada a audiodownload_allowed, con identificación propia del cliente. No se incrustan claves ajenas. |
| spotDL/spotify-downloader 4.5.2 | Integración de formato y exportador opcional | MIT. Instalación y CLI comprobadas. El exportador fuerza use_official_api=True; el APK importa metadatos, no ejecuta Python ni el descargador. |
| GFrancV/spotify-search-wrapper 0.2.1 | Contrato adaptado en bridge/ | El paquete declara MIT, pero no trae un archivo LICENSE independiente. El puente implementa un adaptador propio y acredita el origen. Se retira la extracción web de tokens y se usan credenciales oficiales. |
| MusicBrainz / Cover Art Archive | No incorporado a esta versión | Los proveedores y los archivos locales ya suministran metadatos y portadas; duplicar consultas aumenta latencia y errores de coincidencia. Es compatible como futuro proveedor de enriquecimiento. |

Fuentes primarias revisadas:

- https://github.com/spotDL/spotify-downloader
- https://github.com/spotDL/spotify-downloader/releases/tag/v4.5.2
- https://github.com/GFrancV/spotify-search-wrapper
- https://github.com/GFrancV/spotify-search-wrapper/blob/main/src/utils/index.ts
- https://github.com/GFrancV/spotify-search-wrapper/blob/main/package.json
- https://github.com/androidx/media
- https://developer.android.com/media/media3/session/background-playback
- https://github.com/square/okhttp/tree/parent-4.12.0
- https://github.com/bumptech/glide/tree/v4.16.0
- https://github.com/tranxuanthang/lrclib
- https://docs.audius.co/api/
- https://developer.jamendo.com/v3.0/tracks
- https://styles.refero.design/style/3b6a17f0-3bdf-418c-a95e-0b89e5a8b2f8

No se ha realizado una auditoría de seguridad completa de todo el código de terceros. Las pruebas y el alcance verificado están documentados en VERIFICATION.md.

## YouTube — 1.3

La búsqueda continúa usando metadatos públicos y la reproducción es nativa con youtube-dl y Media3. El extractor y Python vienen incluidos en el APK. El puente Node de yt-search sigue siendo opcional para búsquedas; no hace falta configurarlo para escuchar. No se usa WebView ni IFrame.

Las URLs temporales de audio no se guardan en favoritos, playlists ni respaldos. Se valida HTTPS, host de Googlevideo, caducidad y cabeceras. Solo se aceptan formatos compatibles de audio sin DRM; las restricciones de acceso provocan un error visible. La extracción de un stream no se toma como permiso de descarga. Los permisos de Audius y Jamendo continúan validándose como antes.

Fuentes: https://github.com/ytdl-org/youtube-dl, https://chaquo.com/chaquopy/doc/current/ y https://github.com/talmobi/yt-search. Los permisos de uso del contenido son independientes de las licencias del software.

## Actualización 1.4

Los apartados anteriores sobre Python describen 1.3. El APK 1.4 resuelve audio mediante el adaptador Java NewPipeBackend y NewPipeExtractor v0.26.5, con Rhino y desugaring NIO. Mantiene el contrato de Track y los permisos independientes de descarga. La investigación y los límites de las pruebas constan en REPOSITORIES-1.4.md y VERIFICATION.md.
