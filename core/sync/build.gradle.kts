plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.sync"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            // Project modules
            implementation(project(":core:data"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:utils"))

            // Koin
            implementation(libs.koin.core)

            // Third-party
            implementation(libs.kermit)
            implementation(libs.vpp.sp24)
        }
    }
}
