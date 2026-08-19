plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.atk.atk_cargo.core.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // مدل دامنه‌ی حواله/کوتاژ/کشتی از DTOهای شبکه (CargoInfo/InitialInfo)
    // مپ می‌شود — DEEP_CODE_AUDIT.md #Phase3.6.
    implementation(project(":core:network"))

    testImplementation(libs.junit)
}
