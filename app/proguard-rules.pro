# Proguard / R8 rules for Widget UV

# Keep data models used by Gson
-keepclassmembers class com.hermes.widgetuv.data.model.** { *; }
-keep class com.hermes.widgetuv.data.model.** { *; }

# WorkManager reflection
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Glance AppWidgets
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
