import groovy.lang.MissingFieldException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.util.Base64
import kotlin.text.Charsets

object ApplicationConfig {
    const val APP_VERSION_NAME = "0.1.54-internal"
    const val APP_VERSION_CODE = 96
    var isDebug = false
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.serialization)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.buildconfig)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.androidx.browser)
            implementation(libs.androidx.biometric)

            implementation(libs.androidx.material)
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.androidx.work.runtime.ktx)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)

            implementation(libs.posthog.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)

            implementation(libs.cmp.easy.permission)

            implementation(libs.filekit.compose)

            implementation(libs.kermit)

            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.vpp.sp24)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
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
    compileSdk = 36

    defaultConfig {
        applicationId = "plus.vplan.app"
        minSdk = 24
        targetSdk = 36
        versionCode = ApplicationConfig.APP_VERSION_CODE
        versionName = ApplicationConfig.APP_VERSION_NAME
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            ApplicationConfig.isDebug = true
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
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    debugImplementation(compose.uiTooling)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

buildConfig {
    useKotlinOutput {
        topLevelConstants = false
        internalVisibility = false
    }

    className("BuildConfig")
    packageName("plus.vplan.app")

    buildConfigField("APP_VERSION_CODE", ApplicationConfig.APP_VERSION_CODE)
    buildConfigField("APP_VERSION", ApplicationConfig.APP_VERSION_NAME)
    buildConfigField("APP_DEBUG", ApplicationConfig.isDebug)

    buildConfigField("POSTHOG_API_KEY", localProperties.getProperty("posthog.api.key") ?: throw MissingFieldException("posthog.api.key not found in local.properties", String::class.java))

    generateAtSync = true
}