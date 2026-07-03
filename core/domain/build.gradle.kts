plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Pure Kotlin module — models, repository interfaces, and pure engines. No Android deps.

dependencies {
    implementation(project(":core:common"))
    // Coroutines core is KMP-safe (no Android deps) and only used for Flow in repository interfaces.
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
}
