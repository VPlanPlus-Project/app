plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "plus.vplan.app.utils"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}
