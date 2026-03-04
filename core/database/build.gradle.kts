plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vplanplus.kmp.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidLibrary {
        namespace = "plus.vplan.app.core.database"
        compileSdk = applicationConfig.android.targetSdk
        minSdk = applicationConfig.android.minSdk
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)

            api(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.koin.core)
            implementation(libs.kermit)

            implementation(project(":core:model"))
        }
    }
}


room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    kspAndroid(libs.androidx.room.compiler)
    kspIosSimulatorArm64(libs.androidx.room.compiler)
    kspIosX64(libs.androidx.room.compiler)
    kspIosArm64(libs.androidx.room.compiler)
}