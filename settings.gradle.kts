pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Sintaks yang benar untuk Kotlin DSL (.kts)
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}

rootProject.name = "kasirApp"
include(":app")
