plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

                // Ktor Client
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("io.ktor:ktor-client-logging:2.3.7")

                // Date Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

                // Koin for Dependency Injection
                implementation("io.insert-koin:koin-core:3.5.3")

                // Multiplatform Settings
                implementation("com.russhwolf:multiplatform-settings:1.1.1")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")
            }
        }

        val androidMain by getting {
            dependencies {
                // Ktor Android Engine
                implementation("io.ktor:ktor-client-android:2.3.7")

                // AndroidX Security for encrypted storage
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
            }
        }

        val desktopMain by getting {
            dependencies {
                // Ktor JVM Engine
                implementation("io.ktor:ktor-client-cio:2.3.7")
            }
        }
    }
}

android {
    namespace = "com.truewarg.claude.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
