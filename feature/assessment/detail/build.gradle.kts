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
        namespace = "plus.vplan.app.feature.assessment.detail"
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
            implementation(project(":core:common"))
            implementation(project(":feature:file:core"))
            implementation(project(":feature:assessment:core"))

            implementation(libs.vpp.sp24)
            implementation(libs.kermit)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.kmp.observable.viewmodel)

            // KotlinX
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}

buildConfig {
    packageName("plus.vplan.app.feature.assessment.detail")
    generateAtSync = true
}
