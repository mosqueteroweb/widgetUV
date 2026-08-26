# Widget UV (1x1) para Android & Samsung Galaxy (One UI)

Proyecto completo de Widget 1x1 para pantalla de inicio en Android (optimizado para Samsung Galaxy S26 Ultra y One UI), enfocado en la monitorización en tiempo real del **Índice Ultravioleta (UV)** con código de colores según el estándar internacional de la OMS.

---

## 📱 Características Principales

1. **Diseño 1x1 Compacto**:
   - Visualización grande del valor UV actual (ej. `7.2`).
   - Badge con el nivel de riesgo en color dinámico (*Bajo*, *Moderado*, *Alto*, *Muy Alto*, *Extremo*).
   - Previsión del pico máximo de UV del día y su hora estimada (ej. `Máx 8.5 (14:00)`).
   - Fondo oscuro translúcido con esquinas redondeadas adaptadas a One UI.
2. **Eficiencia y Batería**:
   - Actualización inteligente periódica en segundo plano vía `WorkManager`.
   - Reposo automático fuera de horas de luz solar para no consumir batería innecesaria.
3. **App de Detalle en Jetpack Compose**:
   - Gráfico/carrusel de radiación UV por horas (00:00 - 23:00).
   - Consejos de fotoprotección y salud personalizados según el nivel UV.
   - Botón de refresco instantáneo que sincroniza widgets y app.
4. **Dos opciones de despliegue**:
   - **App Nativa Android** (Kotlin + Jetpack Glance).
   - **Preset para KWGT** (Kustom Widget Maker) con fórmulas de Open-Meteo en [kwgt/KWGT_PRESET_GUIDE.md](kwgt/KWGT_PRESET_GUIDE.md).

---

## 🎨 Escala de Radiación UV y Colores Oficiales (OMS)

| Rango UV | Nivel de Riesgo | Color Hex | Recomendación |
| :--- | :--- | :--- | :--- |
| **0.0 - 2.9** | **Bajo** | `#43A047` (Verde) | Sin riesgo. Seguro para actividades al aire libre. |
| **3.0 - 5.9** | **Moderado** | `#FDD835` (Amarillo) | Usar protector solar, sombrero y gafas de sol. |
| **6.0 - 7.9** | **Alto** | `#FB8C00` (Naranja) | Buscar sombra en horas centrales. Protección indispensable. |
| **8.0 - 10.9** | **Muy Alto** | `#E53935` (Rojo) | Evitar el sol directo del mediodía. Extremar precauciones. |
| **11.0+** | **Extremo** | `#8E24AA` (Violeta) | Peligro severo. Permanecer en interiores. |

---

## 🛠️ Estructura del Código

```
widgetUV/
├── app/
│   ├── src/main/
│   │   ├── java/com/hermes/widgetuv/
│   │   │   ├── data/
│   │   │   │   ├── api/OpenMeteoClient.kt       # Cliente HTTP para la API de Open-Meteo
│   │   │   │   ├── location/LocationHelper.kt   # Gestión de GPS y fallback
│   │   │   │   ├── model/UvData.kt              # Modelos de datos y escala OMS
│   │   │   │   └── repository/UvRepository.kt   # Caché local y sincronización
│   │   │   ├── ui/
│   │   │   │   ├── theme/Theme.kt               # Tema Material 3 / Dynamic Colors
│   │   │   │   └── MainActivity.kt              # Pantalla principal con gráfico por horas
│   │   │   ├── widget/
│   │   │   │   ├── UvGlanceWidget.kt            # Definición del Widget 1x1 en Jetpack Glance
│   │   │   │   └── UvWidgetReceiver.kt          # Receptor de ciclo de vida del widget
│   │   │   └── worker/
│   │   │       ├── BootReceiver.kt              # Reactivación tras reinicio del móvil
│   │   │       └── UvUpdateWorker.kt            # Actualizaciones periódicas en segundo plano
│   │   ├── res/
│   │   │   ├── drawable/                        # Vectores e iconos de launcher
│   │   │   ├── values/                          # Strings, colors y estilos
│   │   │   └── xml/uv_widget_info.xml           # Configuración de tamaño 1x1 del widget
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── kwgt/
│   └── KWGT_PRESET_GUIDE.md                     # Fórmulas y guía para Kustom Widget Maker
├── build.gradle.kts
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

---

## 🚀 Compilación y Despliegue

### Opción 1: Compilar la App Nativa con Android Studio
1. Abre la carpeta `widgetUV` en **Android Studio**.
2. Conecta tu Samsung Galaxy S26 Ultra (o usa un emulador).
3. Pulsa en **Run / Install APK**.
4. En el escritorio de tu móvil, mantén pulsado -> **Widgets** -> Busca **Widget UV** -> Arrastra el widget **1x1** a tu pantalla de inicio.

### Opción 2: Usar KWGT
Sigue los pasos y fórmulas listadas en [kwgt/KWGT_PRESET_GUIDE.md](kwgt/KWGT_PRESET_GUIDE.md).
