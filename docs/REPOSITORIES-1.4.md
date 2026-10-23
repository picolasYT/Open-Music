# Repositorios investigados

Investigación realizada entre el 21 y el 22 de octubre de 2026 a partir de repositorios originales y su documentación.

| Repositorio | Resultado y decisión |
|---|---|
| https://github.com/TeamNewPipe/NewPipeExtractor | v0.26.5, Java, GPL-3.0-or-later. Seleccionado para el APK y fijado a esta versión. Extrae independientemente de la interfaz de NewPipe. Usa Rhino; Android anterior a API 33 necesita desugaring NIO. |
| https://github.com/yt-dlp/yt-dlp | Release 2026.10.21. Probado con dependencias oficiales y Node 24 antes del reinicio del entorno. Resolvió formatos de Milo J, pero el CDN devolvió HTML en esta red. La documentación exige contemplar yt-dlp-ejs y un runtime JS para soporte completo. No se incrustó una integración incompleta en Android. |
| https://github.com/yausername/youtubedl-android | Wrapper Android de yt-dlp, GPL-3.0. El README todavía menciona Python 3.8, mientras el yt-dlp actual pide 3.11+. Referencia de empaquetado, no elegido sin validar sus runtimes actuales. |
| https://github.com/LuanRT/YouTube.js | v18.2.0, TypeScript/JavaScript, MIT; alternativa para un backend Node. No incluido en el APK Java. |
| https://github.com/MetrolistGroup/Metrolist | Cliente de música Android, GPL-3.0. El README declara modo mantenimiento. Referencia de arquitectura, no se copió como aplicación. |
| https://github.com/deniscerri/ytdlnis | App Android con yt-dlp, GPL-3.0. Referencia de integración de descargas; no es una biblioteca reproductora por sí misma. |
| https://github.com/OuterTune/OuterTune | Su README anuncia que ya no está en desarrollo activo. Excluido como base nueva. |
| https://github.com/JunkFood02/Seal | App Android de descargas con yt-dlp. Su release estable pública observada era v1.13.1 de 2024; no se eligió como motor de streaming solo por disponer de interfaz Android. |

La versión 1.3 incluía un snapshot de youtube-dl con versión interna 2025.04.07. La antigüedad no identifica por sí sola el error del teléfono. No se recibió el mensaje exacto de ese dispositivo y no corresponde atribuirlo al filtro de esta red. La versión 1.4 cambia el extractor y corrige la interfaz que podía ocultar el fallo.

La licencia del software no otorga derechos sobre contenidos. No se agregaron cuentas, cookies del navegador, claves privadas ni métodos para eludir controles de acceso. No se publicaron servidores ni cambios en GitHub.
