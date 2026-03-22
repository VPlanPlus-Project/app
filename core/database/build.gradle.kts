plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    android {
        namespace = "plus.vplan.app.core.database"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            // Project modules
            implementation(project(":core:model"))

            // Room & SQLite
            api(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            // Koin
            implementation(libs.koin.core)

            // Third-party
            implementation(libs.kermit)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    kspAndroid(libs.androidx.room.compiler)
    kspIosArm64(libs.androidx.room.compiler)
    kspIosSimulatorArm64(libs.androidx.room.compiler)
}
