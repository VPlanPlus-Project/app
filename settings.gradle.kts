import java.util.Properties
import kotlin.apply

val localProperties = Properties().apply {
    val file = file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

rootProject.name = "VPlanPlus"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
            url = uri("https://gitlab.jvbabi.es/api/v4/groups/12/-/packages/maven")
            credentials {
                username = localProperties.getProperty("GROUP_MAVEN_USERNAME") ?: System.getenv("GROUP_MAVEN_USERNAME")
                password = localProperties.getProperty("GROUP_MAVEN_PASSWORD") ?: System.getenv("GROUP_MAVEN_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            url = uri("https://packages.confluent.io/maven")
            name = "confluence"
        }
    }
}

include(":composeApp")