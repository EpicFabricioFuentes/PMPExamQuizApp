import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Firebase plugins are applied only when google-services.json is present, so the project still
// builds for contributors/CI without Firebase credentials. Drop app/google-services.json in to
// enable Crashlytics + Analytics (the plugins are declared `apply false` in the root build file).
val googleServicesFile = file("google-services.json")
if (googleServicesFile.exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Release signing is loaded from a git-ignored keystore.properties at the repo root (see
// keystore.properties.template). Absent it, debug builds still work; only release signing is gated.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val hasReleaseKeystore = keystorePropertiesFile.exists()
val keystoreProperties = Properties().apply {
    if (hasReleaseKeystore) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Per-owner release config (real AdMob IDs + legal URLs) from a git-ignored secrets.properties (see
// secrets.properties.template). The AdMob *app* ID is resolved here because manifest placeholders in
// a library manifest (:core:ads) are substituted at the app's final merge. Falls back to Google's
// public TEST app ID so the repo builds without the file.
val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) secretsFile.inputStream().use { load(it) }
}
val admobAppIdValue: String =
    secrets.getProperty("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"

android {
    namespace = "com.fax.passyourpmpexam"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fax.passyourpmpexam"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "2026.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Default (overridden per build type). Referenced by the Crashlytics metadata in the
        // manifest so debug crashes are not reported to the console.
        manifestPlaceholders["crashlyticsCollectionEnabled"] = true
        // Resolves ${admobAppId} in :core:ads' manifest at the final merge.
        manifestPlaceholders["admobAppId"] = admobAppIdValue
    }

    signingConfigs {
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
        release {
            // Enable full R8: code shrink + resource shrink + obfuscation. Using the classic
            // properties (still valid in AGP 9) rather than `optimization { enable = true }`, which
            // is the incubating "gradual R8" path and requires the android.r8.gradual.support flag.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            // Signing only wired when a keystore is configured; otherwise assemble an unsigned
            // release (Android Studio / Play App Signing can sign it).
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:notifications"))
    implementation(project(":core:ads"))
    implementation(project(":feature:daily"))
    implementation(project(":feature:home"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:free"))
    implementation(project(":feature:settings"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Firebase (Crashlytics + Analytics). Inert until google-services.json is added.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
