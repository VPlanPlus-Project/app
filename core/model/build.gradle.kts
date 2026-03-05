plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
    // alias(libs.plugins.composeMultiplatform) // Only for preview in File
    // alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.model"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            // Project modules
            implementation(project(":core:utils"))

            // Compose (for preview only)
            implementation(libs.compose.foundation)

            // Koin
            api(libs.koin.core)

            // KotlinX
            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.core)

            // Third-party
            implementation(libs.kermit)
            implementation(libs.vpp.sp24)
        }
    }
}
