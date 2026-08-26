# Preset de Widget UV 1x1 para KWGT (Samsung Galaxy One UI)

Si prefieres usar **KWGT (Kustom Widget Maker)** en lugar de compilar la app nativa:

## 1. Configuración de Petición HTTP en KWGT
Fórmula para obtener el índice UV en tiempo real desde Open-Meteo:

```kustom
$wg("https://api.open-meteo.com/v1/forecast?latitude="+li(lat)+"&longitude="+li(lon)+"&hourly=uv_index&daily=uv_index_max&timezone=auto&forecast_days=1", json, .hourly.uv_index[df(H)])$
```

## 2. Pico Máximo de UV del Día
```kustom
$wg("https://api.open-meteo.com/v1/forecast?latitude="+li(lat)+"&longitude="+li(lon)+"&hourly=uv_index&daily=uv_index_max&timezone=auto&forecast_days=1", json, .daily.uv_index_max[0])$
```

## 3. Color Dinámico según Escala OMS
Fórmula para el color del texto / badge:
```kustom
$if(gv(uv) < 3, "#43A047", if(gv(uv) < 6, "#FDD835", if(gv(uv) < 8, "#FB8C00", if(gv(uv) < 11, "#E53935", "#8E24AA"))))$
```

## 4. Etiqueta de Riesgo (Bajo, Moderado, Alto, Muy Alto, Extremo)
```kustom
$if(gv(uv) < 3, "BAJO", if(gv(uv) < 6, "MODERADO", if(gv(uv) < 8, "ALTO", if(gv(uv) < 11, "MUY ALTO", "EXTREMO"))))$
```

## 5. Diseño Recomendado (1x1)
1. **Fondo**: Rectángulo redondeado (Esquinas: 45, Color: `#DD121316`).
2. **Texto Superior**: `☀️ UV` (Tamaño 14, Color `#94A3B8`).
3. **Texto Central**: `$gv(uv)$` (Tamaño 32, Negrita, Color dinámico).
4. **Badge**: Rectángulo de fondo color dinámico con texto negro del nivel de riesgo.
5. **Texto Inferior**: `Máx $gv(uv_max)$` (Tamaño 11, Color `#94A3B8`).
6. **Acción al Tocar**: Forzar actualización de RSS / Texto Web (`Kustom Action -> Force RSS/Web update`).
