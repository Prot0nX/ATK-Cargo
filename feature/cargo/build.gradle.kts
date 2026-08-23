plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.atk.atk_cargo.feature.cargo"
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
    // QuotaValidationUseCase به QuotaRepository (core:domain) و
    // MessageType/QuotaValidationResult (core:network) نیاز دارد؛ بدون
    // Compose چون هر دو کلاس این ماژول (CargoSnackbarQueue،
    // QuotaValidationUseCase) خالص Kotlin/coroutines هستند
 //.
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:common"))
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.kotlinx.coroutines.android)
 // برای CargoModule.kt (فیچر خودش را در Koin ثبت می‌کند، فاز۳ #۳۳)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
