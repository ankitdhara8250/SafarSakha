pluginManagement {
    repositories {
        google() // Android dependencies
        mavenCentral()// Kotlin, Compose, libraries
        gradlePluginPortal()// Gradle plugins
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
    }
}

rootProject.name = "SafarSakha"

// Android app module
include(":androidApp")
// Shared CMP/KMP module
include(":composeApp")