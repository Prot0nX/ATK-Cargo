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
    // UserPreferencesStore (مرز DI برای featureها — Phase5.12) از Flow استفاده می‌کند.
    implementation(libs.kotlinx.coroutines.android)
    // فقط برای staticCompositionLocalOf در LocalStartupViewModel/
    // LocalNotificationPermissionRequester (Phase4 #29) — runtime تنها، بدون
    // نیاز به UI toolkit کامل.
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.runtime)

    testImplementation(libs.junit)
}
