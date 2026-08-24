import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

val keystoreProperties = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

fun signingProperty(key: String, envVar: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(envVar)

val releaseStorePath = signingProperty("storeFile", "KEYSTORE_PATH")
val releaseSigningConfigured = releaseStorePath != null

android {
    namespace = "com.atk.atk_cargo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.atk.atk_cargo"
        minSdk = 28
        targetSdk = 34
 // ۴.۱.۰: آستانه‌ی گیت کدهای HTTP معنادار در Response::HTTP_CODES_MIN_APP_VERSION
        versionCode = 12
        versionName = "4.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(releaseStorePath!!)
                storePassword = signingProperty("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingProperty("keyAlias", "KEY_ALIAS")
                keyPassword = signingProperty("keyPassword", "KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isShrinkResources = false

            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }

        release {
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }

            // بهینه‌سازی APK
            multiDexEnabled = true
        }

        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            isProfileable = true
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
        viewBinding = false
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
        abortOnError = true
        checkReleaseBuilds = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "**/*.md",
                "**/*.html",
                "**/*.css",
                "**/*.js",
                "**/*.map",
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
}

baselineProfile {
    automaticGenerationDuringBuild = false
}

val archiveReleaseMapping by tasks.registering(Copy::class) {
    description = ""
    val versionName = android.defaultConfig.versionName
    val versionCode = android.defaultConfig.versionCode
    from(layout.buildDirectory.dir("outputs/mapping/release"))
    into(rootProject.layout.projectDirectory.dir("mapping-archive/$versionName-$versionCode"))
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy(archiveReleaseMapping)
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:startup"))
    implementation(project(":feature:admin"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:cargo"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:update"))
    implementation(project(":feature:cargo-workflow"))
    implementation(project(":feature:home"))
    implementation(project(":feature:monitoring"))
    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.foundation.layout)
    // کتابخانه‌های Desugaring هسته جاوا.
    coreLibraryDesugaring(libs.desugar.jdk.libs.v215)

    // ==================== Compose BOM (باید اول باشد) ====================
    implementation(platform(libs.compose.bom))

    // کتابخانه‌های اصلی AndroidX.
    implementation(libs.androidx.core.ktx.v1160)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose.v1101)

    // کامپوننت‌های مدیریت چرخه حیات (Lifecycle).
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ==================== Compose UI ====================
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)

    // کامپوننت‌های طراحی متریال.
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ==================== Navigation ====================
    implementation(libs.androidx.navigation.compose.v290)

    // کتابخانه‌های دوربین و پردازش تصویر ML Kit.
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.text.recognition)

    // ذخیره‌سازی داده‌ها با DataStore و Room.
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // ==================== Networking ====================
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // کوروتین‌ها و سریال‌سازی داده‌ها.
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // مدیریت کارهای پس‌زمینه با WorkManager.
    implementation(libs.androidx.work.runtime.ktx)

    // اسکن بارکد و QR کد.
    implementation(libs.zxing.android.embedded)

    // انیمیشن‌ها و افکت‌های بصری رابط کاربری.
    implementation(libs.lottie.compose)

    // ==================== Testing ====================
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // ابزارهای عیب‌یابی و دیباگ.
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // تزریق وابستگی با فریم‌ورک Koin.
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}