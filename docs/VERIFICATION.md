# Verificación de Open Music 1.4 — 22 octubre 2026

## Compilación

JDK 17 completo, Gradle 8.13, Android Gradle Plugin 8.9.2, SDK/target 35 y minSdk 26. El APK tiene applicationId `com.picolas.openmusic.next`, versionCode 5 y versionName 1.4.0. Su firma de desarrollo coincide con 1.1–1.3 y conserva la posibilidad de actualizar sin desinstalar. No se modificó el esquema SQLite.

La compilación final ejecutó assembleDebug, testDebugUnitTest, lintDebug y assembleDebugAndroidTest. Las 24 pruebas JVM pasaron; lint informó cero errores y 11 advertencias no bloqueantes. Se verificaron la firma con apksigner y la alineación con zipalign. Los informes están en validation-1.4/.

El APK pesa aproximadamente 8 MB. Se comprobó que no contiene el runtime Chaquopy/Python ni el archivo de audio de los tests. Puede conservar textos de licencia históricos sin incluir esos motores. El extractor NewPipe y su licencia sí se incluyen. Su código upstream completo sin cambios se suministra en third-party/.

Se corrigió además el envío duplicado de User-Agent: la implementación Media3 1.8.0 agrega el valor de setUserAgent a los de DataSpec. Se reemplazó por propiedades HTTP predeterminadas que DataSpec puede sobrescribir. La suite instrumentada incorpora un servidor HTTPS local con CA exclusiva del test para comprobar el encabezado único y el rechazo de HTML con HTTP 206. La configuración TLS de producción permanece normal.

## Pruebas online con el código real

Se compiló y ejecutó en el host el propio NewPipeBackend, YouTubeProvider, AudioAddress y AudioContent de esta entrega, usando las dependencias reales del proyecto. Las búsquedas de `milo j` y `trueno` devolvieron 19 resultados relacionados cada una. Se resolvió audio de `aBSkvI0CkgU` (Milo J) a una dirección HTTPS temporal de Googlevideo, con vencimiento limitado a 15 minutos.

La solicitud de bytes devolvió HTTP 206 con 195 bytes de HTML del filtro de red, sin firma MP4. La política nueva rechazó esa respuesta. Por tanto, **no se verificó reproducción en vivo de YouTube de extremo a extremo**. El éxito de búsqueda y de resolución no se cuenta como reproducción. No se intentó eludir el filtro y no se presume que el teléfono del usuario tenga la misma causa de fallo.

El archivo live-check.json conserva consultas, títulos, cantidades y tipos de respuesta; no incluye URLs firmadas ni cabeceras. Las pruebas TLS del host usan su almacén de certificados de confianza. El APK usa la validación TLS normal de Android: no contiene certificados del entorno, cookies de usuario ni excepciones para aceptar certificados no válidos.

## Pruebas nativas

La suite completa terminó **OK (7 tests)** en Android 15/API 35, x86_64. Verificó HTTPS con un User-Agent único y rechazo de HTML, persistencia, respaldos, cancelación real de DownloadManager, audio WAV con seek y segundo plano, carga de Rhino/Java NIO, audio AAC controlado a través del adaptador nativo, selección desde la lista, cola, pausa, siguiente y conservación del mensaje de error tras refrescar la interfaz. El informe completo está en `validation-1.4/android-device-tests.txt`.

El emulador funcionó sin KVM y con renderizado por software. Los primeros intentos sufrieron ANR durante la verificación DEX bajo alta carga del sistema; se preparó la verificación DEX y se desactivaron aplicaciones de Google innecesarias únicamente en el dispositivo de pruebas. El primer test HTTPS agotó 8 segundos y el primer arranque del decodificador AAC superó 20 segundos. Se ampliaron esas esperas de tests a 60 segundos; los límites de red y la seguridad TLS de la aplicación no se modificaron. La pasada final de los siete tests no tuvo fallos. Los intentos iniciales se conservan separados del informe final.

Los archivos AAC y WAV son señales sinusoidales generadas para pruebas; solo están en el APK de tests y en el código fuente. La prueba AAC usa una fuente de datos inyectada en el test, el selector real NewPipeBackend y el mismo ResolvingDataSource, ExoPlayer, MediaSession, MediaController y controles de la aplicación. No demuestra reproducción en vivo de Googlevideo. El WAV se copia de un asset de tests para evitar calcular 960.000 senos durante la emulación sin aceleración.

## Alcance y diagnóstico

Se mantienen favoritos, playlists, historial, letras y contratos de descargas autorizadas. No se recibieron credenciales de Spotify/Jamendo; no se verificaron consultas que las requieran. No se publicó ningún servidor ni se escribió en GitHub.

No se ensayó esta versión en un teléfono físico ARM ni en Android 8–14 o 16. El APK está firmado para instalación directa de desarrollo, sin publicación en Google Play. Si una pista falla, Ajustes → Diagnóstico de audio → Copiar conserva el resultado de la última reproducción de esa sesión, sin direcciones temporales ni datos de cuentas.
