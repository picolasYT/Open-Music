## Actualización 1.7.0 — Música y video

Elegí una canción de YouTube, abrí el reproductor y tocá **Video**. **Música** vuelve al audio conservando el punto de reproducción y la pausa. Video hasta 720p, pantalla completa y los controles habituales de cola, letras y temporizador.

El video requiere conexión incluso si descargaste el audio. Si no está disponible, la app vuelve a Música. Al salir del reproductor o mandar la app al fondo continúa en modo audio. Otras fuentes muestran Video desactivado.

[Descargar APK](https://github.com/picolasYT/Open-Music/releases/latest) · [Novedades](docs/RELEASE-1.7.0.md)

## Actualización 1.6.1

Perfil con avatar y banner corregidos, botones redondeados y guía rápida ilustrada sin conexión. Ver [novedades](docs/RELEASE-1.6.1.md).

[Guía rápida con imágenes](docs/QUICK-GUIDE.md) · [Verificación de la versión](docs/VERIFICATION-1.6.1.md)

# Open Music — Picolas

Reproductor Android nativo, con biblioteca local, varias fuentes de música y reproducción Media3.

## Versión 1.6.0

- Perfil con avatar y banner propios, superposición del avatar y ocho plantillas editables: Graphite, Noche estrellada, Sakura, Café, Crimson, Bosque, Océano y Lavanda.
- Editor con vista previa, colores hexadecimales, tres tipografías, tres formas de avatar, distribución centrada o lateral, altura del banner y decoraciones de estrellas, ondas y cuadrícula.
- Estado, emoji, pronombres/etiqueta, presentación y texto libre «Sobre mí». Hasta 20 plantillas personales reutilizables con sus fotos.
- Mostrar u ocultar estadísticas y playlists. Ocultar las playlists también las excluye del perfil compartido; compartir una playlist individual sigue siendo una acción explícita.
- El QR conserva el diseño sin fotos y puede recortar textos largos. El archivo completo puede incluir avatar y banner normalizados, sin EXIF, al elegir esa opción al exportar. Versiones antiguas siguen abriendo los datos básicos del perfil.
- Aviso automático de nuevas versiones estables de este repositorio, como máximo una consulta al día al abrir la app. Acciones Actualizar, Más tarde y Omitir versión. Ajustes → Actualizaciones permite desactivar avisos o buscar manualmente.
- Actualizar abre el APK oficial en el navegador; Android solicita confirmar la instalación. Instalar 1.6.0 una vez habilita los avisos para las versiones futuras.

## Versión 1.5.1

- Inicio con perfil, filtros y accesos a favoritos y playlists, inspirado en la referencia compartida.
- Crear, describir, duplicar y fijar playlists. Elegir foto propia, logo o carátula automática.
- Ordenar por título, artista y duración, invertir el orden o mover canciones manualmente.
- Tutorial inicial en tres pasos y perfil local: nombre, presentación y color de avatar. Sin correo, contraseña ni servidor de cuentas.
- Gustos iniciales por artistas y géneros. El mix combina afinidad con favoritos e historial, variedad por artista y la opción de ocultar canciones.
- Perfil y playlists compartidos por QR autocontenido: funciona sin servidor y se abre desde Open Music. Cada QR es una copia; no hay sincronización entre dispositivos.
- El QR incluye hasta 8 playlists y una muestra de hasta 6 canciones por lista, ajustada a su capacidad. Se informa el número real incluido. El archivo de perfil JSON permite compartir la colección completa dentro de un límite de 2 MB.
- Corregida la selección al compartir playlists con nombres repetidos.
- Las importaciones no transmiten archivos privados, audio, credenciales, permisos de descarga ni enlaces temporales. Los logos integrados y las descripciones viajan con el perfil; las fotos elegidas del teléfono permanecen en ese dispositivo.
- Filtros desplazables en pantallas estrechas, títulos limitados a dos líneas y posición de scroll conservada cuando llegan recomendaciones.

[Descargar APK](https://github.com/picolasYT/Open-Music/releases/latest)

## Compilar

JDK 17 completo, Android SDK 35 y Build Tools 35.0.0. Se necesita acceso a Google Maven, Maven Central y JitPack.

```sh
chmod +x gradlew
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Pruebas de dispositivo: `./gradlew :app:connectedDebugAndroidTest`. El APK está en `app/build/outputs/apk/debug/app-debug.apk`. El APK publicado conserva la firma de 1.1–1.4. El código excluye las claves de firma: las compilaciones propias usan la identidad debug de Android salvo que se proporcionen OPEN_MUSIC_KEYSTORE_FILE, OPEN_MUSIC_KEYSTORE_PASSWORD, OPEN_MUSIC_KEY_ALIAS y OPEN_MUSIC_KEY_PASSWORD en el entorno. Una compilación con otra clave no puede actualizar la instalación publicada.

## Funciones y proveedores conservados

NewPipeExtractor resuelve YouTube al reproductor nativo Media3. Audius ofrece descubrimiento y búsqueda; Jamendo y los puentes de metadatos Spotify/spotDL son opcionales. Música local, favoritos, historial, letras LRCLIB, cola, temporizador, descargas autorizadas con progreso/cancelación y respaldo JSON siguen disponibles.

Los perfiles y el cálculo de afinidad son locales. Las consultas a proveedores, audio, carátulas y letras requieren conexión. La búsqueda y resolución de YouTube no garantizan reproducción: en el entorno anterior su CDN devolvió HTML, como se documenta en `docs/VERIFICATION.md`. Los cambios 1.5 se documentan en `docs/VERIFICATION-1.5.md`.

## Licencias

La aplicación combinada utiliza GPL-3.0-or-later por NewPipeExtractor. El código original conserva su MIT en `LICENSE-MIT`; la atribución MIT inicial del repositorio permanece en `LICENSE-REPOSITORY-ORIGINAL`. ZXing Core y ZXing Android Embedded usan Apache-2.0. Créditos y licencias se muestran en Ajustes; el código upstream de NewPipe sin cambios está enlazado, con su versión y procedencia, en `third-party/README.md`.
