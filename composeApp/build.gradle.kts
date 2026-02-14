import groovy.lang.MissingFieldException
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.vplanplus.build.applicationConfig)
}

kotlin {
    androidLibrary {
        namespace = "plus.vplan.app.composeapp"
        compileSdk = applicationConfig.android.targetSdk
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xnested-type-aliases")
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.androidx.browser)
            implementation(libs.androidx.biometric)

            implementation(libs.androidx.material)
            implementation(libs.androidx.icons.extended)
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.androidx.work.runtime.ktx)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)

            implementation(libs.posthog.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)

            implementation(libs.cmp.easy.permission)

            implementation(libs.filekit.compose)

            implementation(libs.kermit)

            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.vpp.sp24)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

buildConfig {
    useKotlinOutput {
        topLevelConstants = false
        internalVisibility = false
    }

    className("AppBuildConfig")
    packageName("plus.vplan.app")

    buildConfigField("APP_VERSION_CODE", applicationConfig.versionCode)
    buildConfigField("APP_VERSION", applicationConfig.versionName)
    buildConfigField("APP_DEBUG", localProperties.getProperty("app.debug")?.toBoolean()!!)

    buildConfigField("POSTHOG_API_KEY", localProperties.getProperty("posthog.api.key") ?: throw MissingFieldException("posthog.api.key not found in local.properties", String::class.java))

    generateAtSync = true
}
