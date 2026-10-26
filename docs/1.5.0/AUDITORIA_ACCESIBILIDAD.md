# Widget UV 1.5.0 — auditoría de accesibilidad

Fecha: 26 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 1.4.0.

## Método

Árbol de accesibilidad de la app (lo que anuncia TalkBack), contraste WCAG de cada color de nivel sobre los fondos de la app y del widget, letra del sistema al 200 % y revisión del código de los tres widgets.

## Hallazgos corregidos

| # | Hallazgo | Criterio | Corrección |
|---|---|---|---|
| 1 | El número grande y los valores por horas usaban el color del nivel sobre fondo claro: «Moderado» 1,33:1, «Alto» 2,27:1, «Bajo» 3,16:1, «Muy alto» 4,04:1. | Contraste 4,5:1 (WCAG 1.4.3) | Tonos más oscuros del mismo color en modo claro, todos por encima de 4,6:1. |
| 2 | En los widgets (fondo oscuro), el violeta de «Extremo» daba 2,64:1 y «Muy alto» 4,39:1; la etiqueta «EXTREMO» con texto negro, 2,98:1. | WCAG 1.4.3 | Tonos más claros en fondo oscuro y texto blanco en la etiqueta violeta (7,0:1). |
| 3 | TalkBack leía el número suelto («3,5») sin decir qué era ni su nivel. | 1.1.1 / 4.1.2 | «Índice UV 3,5, moderado». |
| 4 | Cada hora de la curva se leía en tres trozos (hora, «ahora», valor). | 1.3.1 | Una sola frase: «12:00, ahora: Índice UV 3,5, moderado». |
| 5 | El botón de ubicación se leía «Madrid, flechas» y el de idioma «bandera de España ES», sin decir qué hacen. | 4.1.2 | «Ubicación: Madrid», con acción «Actualizar ubicación»; «Idioma: español. Cambiar a inglés». |
| 6 | Los puntos del carrusel de consejos no se anunciaban. | 4.1.2 | «Consejo 1 de 4». |
| 7 | Con letra al 200 %, la etiqueta del nivel quedaba recortada por el círculo y los puntos del carrusel tapaban el título. | Texto ampliable (WCAG 1.4.4) | El círculo crece con el texto y no recorta; el título ocupa su espacio y los puntos van detrás. |
| 8 | Los widgets no tenían descripción para el lector de pantalla y el 1×1 usaba textos de 9 sp. | 1.1.1 / legibilidad | Descripción con índice, nivel y máximo del día; textos mínimos de 10 sp. |
