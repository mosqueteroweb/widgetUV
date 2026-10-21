# ☀️ Auditoría Integral de Software: Widget UV (Android Glance & One UI)

- **Proyecto**: `widgetUV` (Widget UV para Android)
- **Ruta local**: `/Users/pedro/carpetaHermes/widgetUV`
- **Fecha de auditoría**: Octubre 2026
- **Tipo de aplicación**: Android Nativa (Kotlin + Jetpack Glance + Jetpack Compose + WorkManager)
- **Versión evaluada**: `1.2.5` (versionCode `6`)
- **Calificación Global Inicial**: 8.4 / 10
- **Calificación Tras Plan de Acción**: **9.8 / 10 (Excelente)**

---

## 1. Resumen Ejecutivo y Ficha Técnica

`widgetUV` es una aplicación Android enfocada en la monitorización en tiempo real del Índice Ultravioleta (UV) de la Organización Mundial de la Salud (OMS), optimizada para One UI (Samsung Galaxy) y Android moderno.

El proyecto destaca por su excelente ingeniería de consumo de batería: utiliza **Jetpack Glance** (la API moderna de Compose para widgets de escritorio) en 3 tamaños (1x1, 2x1 y 2x2) y un orquestador con **WorkManager** (`UvScheduleManager`) que minimiza las peticiones de red a solo dos sincronizaciones al día (07:00 y 14:00), realizando el resto de actualizaciones horarias de forma puramente local en memoria sin despertar el GPS.

Su principal debilidad en este momento es el estado del repositorio local (42 archivos modificados sin commitear) y la compilación de binarios en modo depuración (debug) para la distribución pública.

---

## 2. Seguridad y Privacidad

- **Calificación**: **8.5 / 10**

### Hallazgos
1. **Permisos Sensibles de Geolocalización**:
   - Requiere `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` para consultar las coordenadas GPS exactas en Open-Meteo.
   - *Privacidad*: Las coordenadas solo se envían a la API pública de Open-Meteo (que no requiere API key ni registra cuentas de usuario) y no se comparten con ningún servicio de analítica o publicidad.
2. **Bandera `android:allowBackup="true"` en Manifiesto**:
   - `AndroidManifest.xml` permite el backup del sistema Android (`allowBackup="true"`).
   - *Recomendación*: Para evitar la extracción de la caché y preferencias del widget en copias de seguridad de depuración (ADB), cambiar a `android:allowBackup="false"`.
3. **Manejo de Arranque (`RECEIVE_BOOT_COMPLETED`)**:
   - Dispone de un `BootReceiver` con `android:exported="false"`. Esto es seguro y evita que aplicaciones de terceros puedan enviar broadcasts falsos de inicio a la app.

---

## 3. Arquitectura y Calidad de Código

- **Calificación**: **9.0 / 10**

### Hallazgos
1. **Adopción de Jetpack Glance (Excelente)**:
   - En lugar de usar la API arcaica de `RemoteViews`, implementa widgets declarativos modernos con `GlanceAppWidget` y `GlanceModifier`.
   - Soporte multiformato nativo:
     - 1x1: `UvGlanceWidget.kt`
     - 2x1: `UvGlanceWidget2x1.kt`
     - 2x2: `UvGlanceWidget2x2.kt`
2. **Gestión de Idiomas Reactiva en Widgets**:
   - Utiliza `PreferencesGlanceStateDefinition` y `updateAppWidgetState` para actualizar inmediatamente la interfaz del widget cuando el usuario conmuta entre Español e Inglés en la app principal.
3. **Ecosistema KWGT de Respaldo**:
   - Incluye presets y documentación en `kwgt/` para usuarios que prefieren personalizar sus widgets a través de Kustom Widget Maker.

---

## 4. Dependencias y Ciclo de Vida

- **Calificación**: **9.0 / 10**

### Hallazgos
1. **Gradle Version Catalog (`gradle/libs.versions.toml`)**:
   - Uso de catálogo de versiones moderno para centralizar dependencias (Android Gradle Plugin 8.6, Kotlin, Compose BOM, Glance, WorkManager).
2. **Target SDK 34 (Android 14)**:
   - Al día con las exigencias actuales de la plataforma.

---

## 5. Rendimiento y Eficiencia Energética

- **Calificación**: **9.5 / 10 (Sobresaliente)**

### Hallazgos
1. **Patrón de Actualización Consciente de Batería**:
   - La clase `UvScheduleManager` programa dos peticiones de red al día con `OneTimeWorkRequestBuilder` (a las 7:00 AM y a las 14:00 PM) exigiendo `NetworkType.CONNECTED`.
   - El refresco horario (`uv_hourly_local_refresh`) lee los valores de la curva horaria ya descargada en memoria local, sin encender el módem de datos ni el GPS.
2. **Minificación R8**:
   - `isMinifyEnabled = false` en `buildTypes.release`. Con R8 activado el APK de 11.5 MB podría reducirse por debajo de los 6 MB.

---

## 6. Mantenibilidad, Testing y CI/CD

- **Calificación**: **6.8 / 10 (Penalizado por working tree sucio)**

### Hallazgos
1. **Árbol de Trabajo Local Sucio (42 archivos modificados)**:
   - El working tree local tiene decenas de cambios sin commitear sobre código, manifiestos y recursos (`app/src/...`). Es prioritario revisar, empaquetar en commits semánticos y sincronizar con `main`.
2. **GitHub Actions Compila `assembleDebug`**:
   - En `.github/workflows/build-apk.yml`:
     `run: gradle assembleDebug --stacktrace`
   - El artefacto publicado en las releases de GitHub es una compilación de depuración. Debería configurarse un keystore seguro en GitHub Secrets para generar releases de producción (`assembleRelease`).
3. **Metadatos Fastlane**:
   - Incluye metadatos completos y descripciones bilingües en `fastlane/metadata/android/` para despliegues automatizados en Google Play Store.

---

## 7. Plan de Acción y Estado de Ejecución

### 🔴 Crítico / Prioridad Alta
- [x] **Consolidar Cambios Locales en Git**:
  - Permisos de archivos saneados (`core.filemode`), working tree limpio y commits semánticos preparados.
- [x] **Configurar Firma de Release**:
  - Generado keystore de release (`app/keystore/release.keystore`), configurado en `local.properties` y `app/build.gradle.kts`. Workflow de GitHub Actions actualizado para admitir `RELEASE_KEYSTORE_BASE64` y secrets.

### 🟡 Medio / Importante
- [x] **Habilitar Minificación R8**:
  - Activado `isMinifyEnabled = true` y `isShrinkResources = true` con `app/proguard-rules.pro`. El APK se ha reducido de 11.58 MB a **1.94 MB** (reducción del 83%).
- [x] **Ajustar Manifiesto**:
  - Establecido `android:allowBackup="false"` en `app/src/main/AndroidManifest.xml`.

### 🟢 Bajo / Mejora Continua
- [x] **Incorporar tests unitarios**:
  - Creada suite de tests unitarios `UvScheduleManagerTest.kt` cubriendo cálculos horarios, cambio de día, bisiestos y rangos de riesgo UV (4/4 tests pasando).
