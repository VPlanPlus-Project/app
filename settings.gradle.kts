import java.util.Properties

val localProperties = Properties().apply {
    val file = file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

rootProject.name = "VPlanPlus"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven")
            name = "confluence"
        }
    }
}

include(":composeApp")
include(":androidApp")

// Core modules
include(":core:model")
include(":core:utils")
include(":core:database")
include(":core:data")
include(":core:network")
include(":core:platform")
include(":core:ui")
include(":core:common")
include(":core:sync")
include(":core:analytics")

// Feature modules
include(":feature:calendar")
include(":feature:onboarding")
include(":feature:grades")