plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.common"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            // Project modules
            implementation(project(":core:data"))
            implementation(project(":core:model"))
            implementation(project(":core:utils"))
        }
    }
}
