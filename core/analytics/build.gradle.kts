plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.vplanplus.kmp.buildconfig)
    alias(libs.plugins.serialization)
}

buildConfig {
    packageName("plus.vplan.app.core.analytics")
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.analytics"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.core)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.graphics.core)

            // Analytics
            implementation(libs.posthog.android)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
        }

        commonMain.dependencies {
            // Project modules
            implementation(project(":core:database"))
            implementation(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:utils"))

            // Koin
            implementation(libs.koin.core)

            // Third-party
            implementation(libs.kermit)
        }
    }
}
