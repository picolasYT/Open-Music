# 1.3.0

Reproducción nativa de YouTube con youtube-dl y Python incluidos en el APK mediante Chaquopy. Se elimina el reproductor IFrame/WebView. Se reutilizan controles, cola y servicio multimedia de Open Music. Resolución de direcciones temporales por ID y soporte para formatos de audio por rangos de archivo. Sin guardar enlaces firmados o cabeceras en biblioteca o backups. Firma e identificador conservados respecto de 1.2 y 1.1.

# 1.2.0

Búsqueda por nombre ampliada a YouTube, resultados parciales de varios proveedores, reproducción con player oficial visible y guardado en biblioteca. Puente Node con yt-search y respaldo youtube-dl para metadatos. Importación de JSON youtube-dl y respaldo de identidad YouTube sin capacidades de audio. Se conserva la firma y el applicationId de 1.1.

# Cambios — 1.1.0

- Progreso real de Android DownloadManager en Offline, con bytes recibidos y porcentaje cuando el tamaño total está disponible. Estado indeterminado si falta ese dato. Cancelación por trabajo desde Offline o el menú de canción, sin quitar favoritos ni playlists.
- Restauración y exportación JSON de favoritos y playlists con Storage Access Framework. Validación completa y transacción SQLite, combinación por nombre y deduplicación por clave. No incluye audio ni credenciales; los permisos y URI recibidos se descartan.
- Reintentos visibles para errores de conexión en descubrimiento, resultados de búsqueda y colecciones. Se conserva el último mix disponible y el acceso a archivos locales.
- Se conserva la nueva clave de desarrollo para próximas actualizaciones. Identificador com.picolas.openmusic.next y nombre de instalación Open Music 1.1, en paralelo con 1.0, cuya clave anterior no está disponible.

Las comprobaciones de 1.0 se conservan en VERIFICATION-1.0.md y validation-1.0/. El informe actual y sus registros están en VERIFICATION.md y validation/.
