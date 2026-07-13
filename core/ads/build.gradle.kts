import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

// The banner unit ID is loaded from a git-ignored secrets.properties at the repo root (see
// secrets.properties.template) and exposed via BuildConfig. Absent it, the build falls back to
// Google's public TEST unit (never bills), so the repo always builds/runs and forks never ship the
// maintainer's real ID. The AdMob *app* ID is injected as a manifest placeholder by the :app module
// (placeholders in a library manifest are resolved at the app's final manifest merge).
val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) secretsFile.inputStream().use { load(it) }
}
fun secret(key: String, default: String): String = secrets.getProperty(key) ?: default

val admobBannerUnitIdValue = secret("ADMOB_BANNER_UNIT_ID", "ca-app-pub-3940256099942544/6300978111")

android {
    namespace = "com.fax.passyourpmpexam.core.ads"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        // Unit ID is read by AdIds via BuildConfig.
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$admobBannerUnitIdValue\"")
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
