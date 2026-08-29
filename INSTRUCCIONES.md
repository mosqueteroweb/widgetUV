# ☀️ Guía de Usuario: Widget UV para Android (Samsung Galaxy & One UI)

Aplicación y Widget 1x1 para la pantalla de inicio de Android, diseñado para monitorizar en tiempo real el **Índice Ultravioleta (UV)**, optimizar la fotoprotección de la piel y ahorrar el máximo de batería en tu dispositivo.

---

## 📥 1. Instalación del APK

1. **Descarga el archivo `widgetUV.apk`** en tu móvil (desde Telegram o mediante transferencia de archivos).
2. Toca sobre el archivo descargado para abrirlo.
3. Si el sistema te lo solicita, activa el permiso **"Permitir desde esta fuente"** (Instalar aplicaciones desconocidas).
4. Pulsa en **Instalar** y, al finalizar, abre la aplicación **Widget UV**.
5. **Concede el permiso de Ubicación**: Es indispensable para que la app conozca tus coordenadas y consulte los datos solares exactos de tu zona.

---

## 📱 2. Cómo colocar el Widget 1x1 en tu Pantalla de Inicio

1. Ve a la **pantalla de inicio** de tu móvil.
2. Mantén pulsado un **espacio vacío** del escritorio hasta que aparezca el menú de personalización.
3. Toca en el icono **Widgets**.
4. En el buscador o en la lista, localiza **Widget UV**.
5. Mantén pulsado el widget **Índice UV (1x1)** y arrástralo a la posición que prefieras en tu pantalla.

---

## ⚡ 3. Cómo Funciona (Ahorro Inteligente de Batería)

A diferencia de otras apps del tiempo que mantienen el GPS encendido todo el día, **Widget UV** utiliza un sistema de **doble sincronización inteligente**:

* **Lectura Automática a las 7:00 AM**: Al comenzar el día, obtiene tu ubicación y descarga la curva solar completa de 24 horas desde la API de Open-Meteo.
* **Lectura Automática a las 2:00 PM (14:00)**: Realiza una actualización a mediodía por si has cambiado de zona o hay variaciones en la previsión.
* **Actualización Horaria sin Gasto de Batería**: Durante el resto del día, el widget actualiza su valor automáticamente leyendo la curva horaria guardada en memoria, **sin encender el GPS ni consumir datos móviles**.
* **Actualización Manual**: Si viajas o cambias de ciudad, abre la app y pulsa el botón **"Forzar Lectura GPS y Refrescar"**.

---

## 🎨 4. Escala de Radiación y Código de Colores (OMS)

El valor del widget y del círculo central cambia de color automáticamente según el estándar internacional de la Organización Mundial de la Salud (OMS):

| Rango UV | Nivel de Riesgo | Color | Recomendación de Fotoprotección |
| :---: | :---: | :---: | :--- |
| **0.0 - 2.9** | **Bajo** | 🟢 Verde | Sin riesgo. Seguro para actividades al aire libre. |
| **3.0 - 5.9** | **Moderado** | 🟡 Amarillo | Usar protector solar, sombrero y gafas de sol en paseos prolongados. |
| **6.0 - 7.9** | **Alto** | 🟠 Naranja | Protección indispensable. Buscar la sombra durante las horas centrales. |
| **8.0 - 10.9** | **Muy Alto** | 🔴 Rojo | Evitar la exposición solar directa entre las 12:00 y las 16:00. |
| **11.0+** | **Extremo** | 🟣 Violeta | Peligro severo para la piel. Permanecer en interiores. |

---

## 📊 5. Uso de la Aplicación

Al tocar sobre el widget o abrir la app **Widget UV**, podrás ver:
1. **Índice UV en tiempo real**: Valor numérico grande con su nivel de riesgo.
2. **Pico máximo previsto**: Indica el valor máximo que alcanzará la radiación hoy y a qué hora ocurrirá (ej. `Máx 6.5 UV a las 14:00`).
3. **Curva UV por Horas**: Carrusel horizontal interactivo que muestra la evolución de la radiación hora a hora de **7:00 AM a 22:00 PM**.
4. **Consejos de Salud**: Recomendaciones médicas personalizadas según el nivel de alerta actual.

---

## ⚙️ 6. Consejos y Preguntas Frecuentes (FAQ)

### ¿Por qué mi widget no se actualiza a las 7:00 AM o 14:00?
En capas de personalización como **One UI (Samsung)** o **MIUI/HyperOS (Xiaomi)**, el sistema puede poner apps en suspensión profunda para ahorrar batería.
* **Solución**: Ve a *Ajustes del móvil > Aplicaciones > Widget UV > Batería* y selecciona **"Sin restricciones"** o **"No optimizar"**.

### ¿Qué ocurre de noche?
Entre las 22:00 y las 07:00 el valor del índice UV se mantendrá en `0.0 (BAJO)` y la app entrará en reposo para no gastar recursos.

### ¿Se requiere conexión a Internet?
Solo necesita unos pocos kilobytes de datos en las sincronizaciones de las 7:00 AM y 2:00 PM para descargar el pronóstico del día.
