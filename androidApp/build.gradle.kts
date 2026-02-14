import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.vplanplus.build.applicationConfig)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    if (listOf("signing.default.file", "signing.default.storepassword", "signing.default.keyalias", "signing.default.keypassword").all { localProperties.containsKey(it) }) {
        signingConfigs {
            create("default") {
                storeFile = file(localProperties["signing.default.file"]!!)
                storePassword = Base64.getDecoder().decode(localProperties["signing.default.storepassword"]!!.toString()).toString(Charsets.US_ASCII)
                keyAlias = localProperties["signing.default.keyalias"]!!.toString()
                keyPassword = Base64.getDecoder().decode(localProperties["signing.default.keypassword"]!!.toString()).toString(Charsets.US_ASCII)
            }
        }
    }
    namespace = "plus.vplan.app"
    compileSdk = applicationConfig.android.targetSdk

    defaultConfig {
        applicationId = "plus.vplan.app"
        minSdk = applicationConfig.android.minSdk
        targetSdk = applicationConfig.android.targetSdk
        versionCode = applicationConfig.versionCode
        versionName = applicationConfig.versionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("default") ?: run {
                println("No default signing config found, using debug signing config")
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    
    sourceSets["main"].apply {
        java.directories.add("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":composeApp"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    
    implementation(libs.posthog.android)

    implementation(libs.filekit.compose)
    implementation(libs.ktor.client.core)
    
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)
    
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.kermit)
    
    debugImplementation(libs.androidx.ui.tooling)
}
