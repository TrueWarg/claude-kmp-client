plugins {
    // Kotlin Multiplatform
    kotlin("multiplatform") version "1.9.22" apply false
    kotlin("android") version "1.9.22" apply false

    // Android
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false

    // Compose Multiplatform
    id("org.jetbrains.compose") version "1.5.12" apply false

    // Serialization
    kotlin("plugin.serialization") version "1.9.22" apply false
}

allprojects {
    group = "com.truewarg.claude"
    version = "1.0.0"
}
