plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
//    alias(libs.plugins.composeMultiplatform) // Only for preview in File
//    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "plus.vplan.app.core.model"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.kermit)
            implementation(libs.vpp.sp24)

            api(libs.koin.core)

            implementation(libs.compose.foundation) // Only for preview in File

            implementation(project(":core:utils"))
        }
    }
}
