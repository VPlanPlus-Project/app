plugins {
    alias(libs.plugins.vplanplus.kmp.compose.library)
    alias(libs.plugins.vplanplus.build.applicationConfig)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.ui"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
        }
    }
}