# Verificación de Open Music 1.7.0

Código de reproducción: `8d302e1e32f8f05432c4a2a86c8110f791b6ae39`.

La primera ejecución [Android](https://github.com/picolasYT/Open-Music/actions/runs/40092823496) pasó compilación, Android lint, 49 pruebas unitarias y 10 pruebas en Android 15. Incluye selección de formatos, rechazo de enlaces inválidos, video renderizado con audio seleccionado, saltos de posición, pausa, cola y disponibilidad del selector.

La ejecución final de Android agrega una prueba de ida y vuelta Música / Video a través de una MediaSession real, pantalla completa, posición y pausa conservadas, y la instalación del APK firmado 1.7.0 sobre 1.6.1 conservando un archivo de datos. La publicación depende del éxito de esa ejecución en GitHub Actions.

El APK publicado se reconstruye con `python3 tools/assemble_update.py --video`. Los SHA-256, tamaño y certificado esperado se registran en `distribution/update-1.7.0.json`. Se verificaron las firmas APK v2 y v3 y la coincidencia del certificado con las versiones anteriores. El código y las claves privadas están separados.

El video se probó con un MP4/AVC local de 12 segundos con audio, generado para los tests. No se verificaron en este entorno la extracción de un video real de YouTube ni su CDN, ni la instalación manual en un teléfono físico. Las restricciones o cambios del proveedor pueden impedir acceder a una pista. La app conserva la opción de audio y vuelve a ella si el video falla.
