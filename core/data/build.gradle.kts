plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.data"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.core)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.graphics.core)
        }

        commonMain.dependencies {
            // Project modules
            implementation(project(":core:database"))
            implementation(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:utils"))

            // Room & SQLite
            api(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            // Koin
            implementation(libs.koin.core)

            // KotlinX
            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.core)

            // Third-party
            implementation(libs.filekit.core)
            implementation(libs.kermit)
            implementation(libs.vpp.sp24)
        }
    }
}
