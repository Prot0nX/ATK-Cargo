plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.atk.atk_cargo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.atk.atk_cargo"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "3.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++11"
            }
        }

        proguardFiles("proguard-rules.pro")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf()
            multiDexEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
        }
    }
    ndkVersion = "26.1.10909125"
    buildToolsVersion = "34.0.0"
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")

    // AndroidX AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material 3
    implementation("androidx.compose.material3:material3:1.2.1")

    // Compose UI
    implementation("androidx.compose.ui:ui:1.6.8")

    // AndroidX Material
    implementation("androidx.compose.material:material:1.6.8")

    // UI Tooling Preview
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")

    // Lifecycle Runtime
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.1")

    // Coroutines for Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // LiveData KTX
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // ViewModel KTX
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // Navigation Fragment KTX
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")

    // Navigation UI KTX
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Activity
    implementation("androidx.activity:activity:1.9.1")

    // Material 3 for Android
    implementation("androidx.compose.material3:material3-android:1.2.1")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // Compose Graphics
    implementation("androidx.compose.ui:ui-graphics:1.6.8")

    // Compose Material
    implementation("androidx.compose.material:material:1.3.1")

    // DataStore Core
    implementation("androidx.datastore:datastore-core-android:1.1.1")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences-core-jvm:1.1.1")

    // Material
    implementation("com.google.android.material:material:1.4.0")

    // Camera View
    implementation("androidx.camera:camera-view:1.3.4")

    // ML Kit Barcode Scanning
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")

    // Camera Lifecycle
    implementation("androidx.camera:camera-lifecycle:1.3.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")

    // Debugging Tools
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Foundation
    implementation("androidx.compose.foundation:foundation:1.6.8")

    // Coil Compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    // JSON
    implementation("org.json:json:20240303")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-serialization:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")

    // Serialization JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // JXL
    implementation("net.sourceforge.jexcelapi:jxl:2.6.12")

    // ZXing Barcode
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Work Runtime KTX
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Lottie Compose
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // iTextPDF
    implementation("com.itextpdf:itextpdf:5.5.13.2")

    // Apache POI
    implementation("org.apache.poi:poi:5.3.0")
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // ICU4J
    implementation("com.ibm.icu:icu4j:70.1")

    // Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Konfetti Compose
    implementation("nl.dionsegijn:konfetti-compose:2.0.2")

    // Camera 2
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation("com.patrykandpatrick.vico:core:1.7.3")
}
