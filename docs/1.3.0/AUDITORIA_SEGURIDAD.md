# Widget UV 1.3.0 — auditoría de seguridad y privacidad

Fecha: 26 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 1.2.6 (no publicada).

## Alcance

Manifiesto y permisos, ubicación, peticiones a Open-Meteo y BigDataCloud, validación de datos, almacenamiento, firma, contenido del repositorio y GitHub Actions. La auditoría anterior (`AUDITORIA.md`, 9,9/10) no detectó los puntos siguientes.

## Hallazgos corregidos

| # | Hallazgo | Riesgo | Corrección |
|---|---|---|---|
| 1 | La ubicación se enviaba a Open-Meteo y a BigDataCloud (un tercero, para el nombre de la provincia) con 4 decimales, unos 11 m de precisión, y se guardaba igual. | Medio (privacidad) | Redondeo a 2 decimales (~1 km) antes de guardarla o enviarla. El índice UV no cambia dentro de una localidad. |
| 2 | Se pedía ubicación precisa (GPS) además de la aproximada. | Bajo | Solo ubicación aproximada; se retira `ACCESS_FINE_LOCATION`. |
| 3 | Los datos de Open-Meteo no se validaban ni se limitaba el tamaño de la respuesta: un valor negativo, infinito o absurdo se mostraba tal cual. | Bajo | Índice UV aceptado solo entre 0 y 25; respuesta limitada a 256 KB y conexión cerrada. Pruebas `OpenMeteoValidationTest`. |
| 4 | Las versiones publicadas en GitHub (1.2.3-1.2.5) se compilaron sin clave (el repositorio no tiene secretos) y quedaron firmadas con una clave de depuración temporal distinta en cada compilación. | Medio (actualizaciones imposibles) | Publicación desde el equipo del autor con la clave permanente. Quien tenga una versión anterior debe desinstalarla una vez. |
| 5 | La compilación de depuración usaba la clave de release. | Bajo | Depuración con el certificado de depuración de Android. |
| 6 | El APK estaba versionado en Git. | Bajo | Retirado y excluido con `*.apk`. |
| 7 | Sin reglas de extracción de datos: en Android 12+ la ubicación guardada podía pasar a otro móvil en una transferencia. | Bajo | `dataExtractionRules` que excluye todos los datos. |
| 8 | GitHub Actions con permiso de escritura, acciones sin fijar y roto desde el 18-10-2026 (`setup-android` pedía aceptar una licencia preliminar). | Bajo | Flujo de solo validación (pruebas, lint, compilación) con permisos de lectura y acciones fijadas por SHA. |
| 9 | El remoto de Git apuntaba a un alias SSH inexistente; la 1.2.6 nunca se había subido. | Operativo | Remoto HTTPS. |

## Sin hallazgos

- Receptores de widget exportados solo para `APPWIDGET_UPDATE`, como exige Android; el de arranque no está exportado.
- Todo el tráfico es HTTPS; sin claves ni tokens en el código; la clave de firma nunca ha estado en el historial.
- Sin registros (`Log`) con datos personales.
