plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

/**
 * Backend base URL. Retrofit resolves relative paths ("auth/login") against it, so it must
 * include the "/api/v1/" prefix AND end with a slash.
 *
 * Both build types default to the deployed Render API so a fresh clone runs with no setup.
 * Point a debug build at a local backend with:
 *   ./gradlew installDebug -PapiBaseUrl=http://10.0.2.2:5170/api/v1/
 */
val deployedApiBaseUrl = "https://oryxen-backend.onrender.com/api/v1/"
val debugApiBaseUrl = (project.findProperty("apiBaseUrl") as String?) ?: deployedApiBaseUrl

android {
    namespace = "io.oryxen.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.oryxen.mobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            // Only needed when -PapiBaseUrl points at a plaintext local backend.
            manifestPlaceholders["usesCleartextTraffic"] = debugApiBaseUrl.startsWith("http://")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"$deployedApiBaseUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
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
}

firebaseAppDistribution {
    artifactType = "APK"
    releaseNotesFile = file("release-notes.txt").toString()
    testers = file("testers.txt").toString()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)

    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    implementation("com.google.firebase:firebase-analytics")
}
