# Verificación de Open Music 1.6.1

## Interfaz y comportamiento

La compilación de origen es `70e707af8080eb5be7bbf903f3d7616046f99b29`. La [ejecución de Android](https://github.com/picolasYT/Open-Music/actions/runs/40016129324) terminó correctamente: compilación, Android lint, 45 pruebas unitarias y 8 pruebas de dispositivo sobre Android 15.

Las pruebas de dispositivo cubren persistencia de fotos y plantillas, exportación/importación del estilo, guardar/cancelar en el editor, el aviso de actualización mediante HTTPS, migración de playlists, copia de imágenes, finalización de la guía y navegación por sus seis pasos, incluido Atrás. Las capturas se revisaron en un viewport de 320 × 640. Se conservaron en [la guía con imágenes](QUICK-GUIDE.md).

## APK público

El APK probado por esa ejecución se volvió a firmar con la identidad publicada anteriormente, sin modificar sus archivos de aplicación. `apksigner verify --verbose --print-certs` confirmó firmas v2/v3 y el certificado SHA-256:

`db235fcb26e84a58ab5b7d7abbe866bb0cad90eaeb820e0a9966a0681da45bed`

APK: `Open-Music-1.6.1.apk`, 8.362.419 bytes, SHA-256:

`db05776cc926bfda7b7c5901d89bbb9ea3ab50f0b0bb7dfa34aa93762e913eed`

Se conserva `com.picolas.openmusic.next`; versionName `1.6.1`, versionCode `9`. El transporte público reconstruye el APK completo mediante un parche BSDIFF40 y comprueba los hashes de la base, el parche y el resultado. No contiene claves privadas. La CI instala 1.6.0 y actualiza con `adb install -r`, comprobando que permanece un archivo de datos antes de habilitar la publicación.

## Alcance

Las pruebas cubren esta interfaz y sus datos locales. No se verificaron la reproducción de YouTube en vivo ni la instalación desde el navegador de un teléfono físico en esta entrega.
