plugins {
    alias(libs.plugins.vplanplus.kmp.compose.library)
    alias(libs.plugins.vplanplus.kmp.buildconfig)
    alias(libs.plugins.vplanplus.build.applicationConfig)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kmpNativeCoroutines)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    android {
        namespace = "plus.vplan.app.feature.grades"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        androidResources.enable = true
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }

        commonMain.dependencies {
            // Project modules
            implementation(project(":core:data"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:ui"))
            implementation(project(":core:utils"))
            implementation(project(":core:sync"))
            implementation(project(":core:analytics"))

            // Navigation
            implementation(libs.navigation.compose)
            implementation(libs.navigation3.ui)
            implementation(libs.navigation3.material3.adaptive)
            implementation(libs.navigation3.lifecycle)

            implementation(libs.vpp.sp24)
            implementation(libs.kermit)

            implementation(libs.kmp.observable.viewmodel)

            // KotlinX
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

buildConfig {
    packageName("plus.vplan.app.feature.grades")
    generateAtSync = true
}
