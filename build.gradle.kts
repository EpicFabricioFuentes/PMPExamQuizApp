// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Declared here (apply false) so the version is set in one place and subprojects
    // apply the plugin without re-specifying a version (avoids "already on the classpath"
    // conflicts in multi-module builds). Add more plugins here as modules need them.
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    // Firebase — put the plugins on the classpath here; :app applies them conditionally
    // (only when app/google-services.json exists) so credential-less builds still work.
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}