import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "plus.vplan.app.build"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("applicationConfig") {
            id = libs.plugins.vplanplus.build.applicationConfig.get().pluginId
            implementationClass = "ApplicationConfigPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.vplanplus.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
    }
}