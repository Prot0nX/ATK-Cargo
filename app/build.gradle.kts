plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.atk.atk_cargo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.atk.atk_cargo"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "3.0.32"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }

        // تنظیمات ProGuard
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isShrinkResources = false

            // تنظیمات NDK برای دیباگ - شامل تمام معماری‌ها
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // تنظیمات NDK برای ریلیز - فقط ARM
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }

            // بهینه‌سازی APK
            multiDexEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true

        aidl = false
        renderScript = false
        resValues = false
        shaders = false
        dataBinding = false
        mlModelBinding = false
        prefab = false
    }

    lint {
        disable += setOf("NullSafeMutableLiveData")
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/license*",
                "META-INF/NOTICE*",
                "META-INF/notice*",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                "**/attach_hotspot_windows.dll",
                "META-INF/services/javax.annotation.processing.Processor",
                "**/kotlin/**",
                "**/*.proto",
                "**/*.properties",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "**/*.txt",
                "**/*.md",
                "**/*.html",
                "**/*.css",
                "**/*.js",
                "**/*.map",
                "**/*.bin",
                "**/*.dat",
                "**/*.cfg",
                "**/*.ini",
                "**/*.log",
                "**/*.tmp",
                "**/*.bak",
                "**/*.orig",
                "**/*.rej",
                "**/*.patch",
                "**/*.diff",
                "**/*.swp",
                "**/*.swo",
                "**/*.DS_Store",
                "**/*.gitignore",
                "**/*.gitkeep",
                "**/*.gradle",
                "**/*.pro",
                "**/*.iml",
                "**/*.idea/**",
                "**/.git/**",
                "**/.svn/**",
                "**/.hg/**",
                "**/.bzr/**",
                "**/CVS/**",
                "**/Thumbs.db",
                "**/*.pyc",
                "**/*.pyo",
                "**/*.class",
                "**/*.jar",
                "**/*.war",
                "**/*.ear",
                "**/*.zip",
                "**/*.tar",
                "**/*.gz",
                "**/*.bz2",
                "**/*.7z",
                "**/*.rar",
                "**/*.iso",
                "**/*.dmg",
                "**/*.exe",
                "**/*.msi",
                "**/*.deb",
                "**/*.rpm",
                "**/*.pkg",
                "**/*.apk",
                "**/*.ipa",
                "**/*.aab"
            )
        }
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += setOf(
                "**/libc++_shared.so",
                "**/libjsc.so"
            )
        }
    }

    ndkVersion = "26.1.10909125"
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.androidx.foundation.layout)
    // ==================== Core Library Desugaring ====================
    coreLibraryDesugaring(libs.desugar.jdk.libs.v215)

    // ==================== Compose BOM (باید اول باشد) ====================
    implementation(platform(libs.compose.bom))

    // ==================== AndroidX Core Libraries ====================
    implementation(libs.androidx.core.ktx.v1160)
    implementation(libs.androidx.appcompat.v171)
    implementation(libs.androidx.activity.compose.v1101)
    implementation(libs.androidx.constraintlayout)

    // ==================== Lifecycle Components ====================
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ==================== Compose UI ====================
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)

    // ==================== Material Design ====================
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material)

    // ==================== Navigation ====================
    implementation(libs.androidx.navigation.compose.v290)
    implementation(libs.androidx.navigation.fragment.ktx.v290)
    implementation(libs.androidx.navigation.ui.ktx.v290)

    // ==================== Camera & ML Kit ====================
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // ==================== Data Storage ====================
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // ==================== Room Database ====================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // ==================== Networking ====================
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // ==================== Coroutines & Serialization ====================
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // ==================== Work Manager ====================
    implementation(libs.androidx.work.runtime.ktx)

    // ==================== Image Loading ====================
    implementation(libs.coil.compose.v260)

    // ==================== Barcode Scanning ====================
    implementation(libs.zxing.android.embedded)

    // ==================== Animation & UI Effects ====================
    implementation(libs.lottie.compose)

    // ==================== Video Player ====================
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    // ==================== Charts & Visualization ====================
    implementation(libs.core)

    // ==================== Document Processing ====================
    implementation(libs.itextpdf)

    // ==================== Testing ====================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // ==================== Debug Tools ====================
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}