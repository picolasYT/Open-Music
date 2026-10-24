# APK 1.7.0

Ejecutá `python3 tools/assemble_update.py --video` desde el repositorio para reconstruir el APK firmado, con comprobaciones de hash y tamaño. Los bloques de actualización son transporte del archivo de release y no se ejecutan en el teléfono.

El APK 1.7.0 conserva el identificador y la firma de las versiones anteriores. `update-1.7.0.json` registra la procedencia y el certificado. [Descargar la versión publicada](https://github.com/picolasYT/Open-Music/releases/latest).

# APK firmado

La versión 1.6.0 se conserva en partes pequeñas de transporte dentro de apk-1.6.0. Para reconstruir el APK exacto: `python3 tools/assemble_release.py`, ejecutado desde la raíz del proyecto. El script comprueba el SHA-256 antes de generar el archivo y la CI publica el APK completo en Releases. No se necesita ninguna clave para reconstruirlo; ya está firmado.

Las partes solo contienen el APK público. Las claves privadas no están en el repositorio.

## Open Music 1.6.1

`python3 tools/assemble_update.py` reconstruye el APK firmado a partir de 1.6.0 y un parche verificado. Comprueba SHA-256 de base, parche y resultado. No requiere paquetes adicionales; el instalador descargable es un APK completo.
