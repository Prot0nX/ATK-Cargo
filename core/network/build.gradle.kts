plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.atk.atk_cargo.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    // بدون این، android.util.Log.w (در TokenRefresher.catch) و بررسی داخلی OkHttp از Log.isLoggable روی JVM Unit Test (بدون دستگاه/امولاتور) با "Method ... not mocked" استثنا می‌اندازند، چون android.jar تست فقط stub است.
    testOptions {
        unitTests.isReturnDefaultValues = true
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
}

dependencies {
    // @Immutable روی DTOها (ReportModels.kt) — فقط runtime سبک compose، نه ui/foundation.
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.runtime:runtime")

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
}
