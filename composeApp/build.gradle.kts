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
        androidResources.enable = true
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xnested-type-aliases")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
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

            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
        }

        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:utils"))
            implementation(project(":core:database"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))

            implementation(compose.runtime)
            implementation(libs.compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.navigation.compose)

            implementation(libs.cmp.easy.permission)

            implementation(libs.filekit.compose)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.kermit)

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
