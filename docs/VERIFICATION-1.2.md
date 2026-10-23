# Open Music 1.2.0 — verificación

21 de octubre de 2026. Autor: Picolas.

## Compilación y pruebas

APK real compilado con Java 17, SDK/target 35, minSdk 26, Gradle 8.13 y AGP 8.9.2. assembleDebug, testDebugUnitTest, lintDebug y assembleDebugAndroidTest finalizaron correctamente. Lint: 0 errores, 16 advertencias, sin baseline que oculte errores. Firma v2 verificada. VersionCode 3, applicationId com.picolas.openmusic.next. Se confirmó que usa el mismo certificado que el APK 1.1. Se instaló 1.1 con una base de datos de prueba y se actualizó a 1.2 sin desinstalar. La base de datos conservó exactamente sus bytes, su favorito y su playlist. Ver upgrade-check.txt.

- 17 pruebas JVM aprobadas: políticas de proveedores, spotDL, letras, respaldos, parser de resultados reales Milo J/Trueno, deduplicación, rechazo de IDs inválidos y descarte de capacidades de audio importadas.
- 6 pruebas Node aprobadas: puente Spotify, parser original yt-search integrado, validación, caché y permisos separados de los metadatos.
- 5 pruebas de instrumentación aprobadas en emulador Android 9 en 127,804 segundos. Incluyen SQLite, backups, cancelación real de DownloadManager, reproducción/seek/background de WAV local y pulsación de un resultado YouTube para abrir su Activity con WebView oficial visible y configurada sin acceso a archivos locales. El video requiere gesto de reproducción y no se pasa a Media3.
- Revisión visual de resultados, reproductor YouTube y pantallas existentes a 360 dp. Las imágenes internas y el audio generado por pruebas no se incluyen en el APK.

Registros: validation-1.2/. El proyecto conserva comprobaciones anteriores en carpetas con su versión.

## Consultas reales

El código del proveedor Android YouTubeProvider se ejecutó también en JVM con sus dependencias reales y solicitudes HTTPS públicas: 19 resultados para «milo j» y 29 para «trueno», con títulos relevantes verificados. El mismo parser y código HTTP se compila en el APK. El registro android-provider-live.txt enumera títulos e IDs de muestra, sin URLs directas de medios.

El paquete original yt-search 2.13.1 se verificó con consultas reales: 19 resultados de Milo J y 20 de Trueno. El servidor HTTP /youtube/search se probó con respuesta 200 y 20 resultados de Trueno; /health, validación 400 y ruta desconocida 404 también se comprobaron.

youtube-dl se instaló desde el snapshot fijado 956b8c585591b401a543e409accb163eeaaa1193 (versión declarada 2025.4.7). Una búsqueda pública de Milo J devolvió 5 metadatos solicitados y el adaptador del puente devolvió 20 resultados de Trueno. No se descargó audio o video en esas comprobaciones. La release estable 2021.12.17 figura como antigua y con problemas conocidos en el repositorio original; por eso se usa el snapshot fijado.

npm audit detectó inicialmente avisos altos por minimatch transitivo. Se fijó minimatch 3.1.4, se reinstaló con el lockfile y el informe final de dependencias de producción reportó 0 vulnerabilidades conocidas. Esto no constituye una auditoría completa del código.

## Alcance de la reproducción YouTube

La prueba Android verifica el control real, el cambio de pantalla y la configuración del WebView. No certifica que un video de YouTube se haya reproducido con audio en el emulador: su entorno no tiene la misma conectividad/WebView que un teléfono actual. La búsqueda sí se comprobó contra respuestas reales, con el código del proveedor y el puente. La reproducción usa la IFrame API oficial, con Referer/origin de la app, y ofrece abrir el video en YouTube si no admite incrustación. La disponibilidad regional, la conexión y los permisos del canal dependen de YouTube. No se promete todo su catálogo, descarga de videos ni reproducción oculta/en segundo plano.

No se suministraron credenciales de Spotify/Jamendo. Sus rutas autenticadas siguen sin prueba de catálogo en vivo. No se probó en teléfono físico ni con Bluetooth real. Las licencias open source del software no conceden derechos sobre la música.
