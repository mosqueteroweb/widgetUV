import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getSecret(key: String, default: String? = null): String? {
    return System.getenv(key)
        ?: localProperties.getProperty(key)
        ?: (project.findProperty(key) as? String)
        ?: default
}

android {
    namespace = "com.hermes.widgetuv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hermes.widgetuv"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val keystorePath = getSecret("KEYSTORE_PATH", "keystore/release.keystore")
    val keystoreFile = keystorePath?.let { file(it) }
    val storePasswordVal = getSecret("KEYSTORE_PASSWORD")
    val keyAliasVal = getSecret("KEY_ALIAS")
    val keyPasswordVal = getSecret("KEY_PASSWORD")

    val hasSigningKeys = keystoreFile != null && keystoreFile.exists() &&
            !storePasswordVal.isNullOrBlank() &&
            !keyAliasVal.isNullOrBlank() &&
            !keyPasswordVal.isNullOrBlank()

    signingConfigs {
        if (hasSigningKeys) {
            create("release") {
                storeFile = keystoreFile
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasSigningKeys) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug builds keep the Android debug certificate; only releases use the release key.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Jetpack Glance (Compose for AppWidgets)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // WorkManager for background scheduled updates
    implementation(libs.androidx.work.runtime.ktx)

    // Location Services
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // Coroutines & Json
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
