# Widget UV 1.4.0 — auditoría de UX

Fecha: 26 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 1.3.0.

## Método

App recorrida en emulador (Android 16) en español e inglés, modo claro y oscuro y letra al 200 %; revisión del selector de widgets y del código de los tres widgets (1×1, 2×1 y 2×2).

## Hallazgos corregidos

| # | Hallazgo | Corrección |
|---|---|---|
| 1 | Al cambiar a modo oscuro, el tamaño de letra o girar la pantalla, la app se quedaba en blanco y volvía a pedir el GPS y los datos a Open-Meteo. | La pantalla se adapta sin reiniciarse: conserva los datos y no repite peticiones. |
| 2 | Barra de estado con iconos blancos sobre fondo claro: no se veían la hora ni la batería. | Iconos y fondo de la barra según el tema. |
| 3 | En español los números salían con punto («3.5», «Máx 7.1»), en la app y en los widgets. | Coma decimal en español y punto en inglés. |
| 4 | «Auto-GPS: 7:00 AM / 2:00 PM» en la versión española. | «Actualizado: 12:40 · Automático a las 7:00 y 14:00». |
| 5 | Botón principal con jerga: «Forzar Lectura GPS y Actualizar». | «Actualizar ubicación y UV» / «Update location & UV». |
| 6 | Las tres vistas previas del selector de widgets eran el mismo cuadrado oscuro con un punto; la del 2×1 horizontal salía cuadrada. | Vista previa propia de cada tamaño con su aspecto real (Android 12+). Compiladas sin errores; no se pudieron comprobar en el selector del emulador. |

## Pendiente (accesibilidad, 1.5.0)

- Con letra al 200 % la etiqueta del nivel se sale del círculo y los puntos del carrusel de recomendaciones se montan sobre el título.
- Textos de 9 sp en el widget 1×1 y texto negro sobre el violeta de «Extremo».
