plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "plus.vplan.app.core.platform"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.core)
        }

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }
    }
}
