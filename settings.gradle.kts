pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ATK-Cargo"
include(":app")
include(":baselineprofile")
include(":core:designsystem")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:domain")
include(":feature:auth")
include(":feature:startup")
include(":feature:admin")
include(":feature:chat")
