plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.atk.atk_cargo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.atk.atk_cargo"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "3.0.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // بهینه‌سازی تنظیمات NDK
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        
        // تنظیمات بهینه‌سازی اضافی برای کاهش حجم
        resourceConfigurations += setOf("en", "fa")

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
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // بهینه‌سازی APK
            multiDexEnabled = true
            
            // تنظیمات بهینه‌سازی کد
            packaging {
                resources {
                    excludes += setOf(
                        "**/kotlin/**",
                        "META-INF/**.version",
                        "META-INF/**.kotlin_module",
                        "META-INF/DEPENDENCIES",
                        "META-INF/LICENSE*",
                        "META-INF/NOTICE*",
                        "META-INF/ASL2.0",
                        "META-INF/*.SF",
                        "META-INF/*.DSA",
                        "META-INF/*.RSA",
                        "META-INF/services/**",
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
        
        // غیرفعال کردن ویژگی‌های غیرضروری برای کاهش حجم
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
        dataBinding = false
        mlModelBinding = false
        prefab = false
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    
    lint {
        disable += setOf("NullSafeMutableLiveData")
        abortOnError = false
        checkReleaseBuilds = false
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/license*",
                "META-INF/NOTICE*",
                "META-INF/notice*",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                "**/attach_hotspot_windows.dll",
                "META-INF/services/javax.annotation.processing.Processor"
            )
        }
        
        // بهینه‌سازی JNI libraries
        jniLibs {
            useLegacyPackaging = false
        }
    }
    ndkVersion = "26.1.10909125"
    buildToolsVersion = "34.0.0"
}

dependencies {
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
    implementation(libs.text.recognition)

    // ==================== Data Storage ====================
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

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