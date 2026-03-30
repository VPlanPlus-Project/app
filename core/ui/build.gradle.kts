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
        androidMain.dependencies {
            implementation(libs.androidx.browser)
        }

        commonMain.dependencies {
            implementation(libs.koin.core)

            implementation(project(":core:model"))
            implementation(project(":core:utils"))
            implementation(project(":core:platform"))
        }

        iosMain.dependencies {
            implementation(libs.koin.core)
        }
    }
}
