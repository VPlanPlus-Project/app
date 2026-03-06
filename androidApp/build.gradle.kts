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
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xnested-type-aliases")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
    }
}

val allKeysSet = arrayOf(
    "signing.default.file",
    "signing.default.storepassword",
    "signing.default.keyalias",
    "signing.default.keypassword"
).all { localProperties.containsKey(it) }

android {
    if (allKeysSet) {
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

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("default") ?: run {
                println("No default signing config found, using debug signing config")
                signingConfigs.getByName("debug")
            }
        }

        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets["main"].apply {
        java.directories.add("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:platform"))
    implementation(project(":core:analytics"))

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)

    // Compose
    implementation(libs.compose.material3)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

    // Ktor
    implementation(libs.ktor.client.core)

    // Third-party
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs.compose)
    implementation(libs.kermit)
    implementation(libs.posthog.android)

    debugImplementation(libs.androidx.ui.tooling)
}
