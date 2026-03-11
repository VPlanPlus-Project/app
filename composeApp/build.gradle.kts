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
    android {
        namespace = "plus.vplan.app.composeapp"
        compileSdk = applicationConfig.android.targetSdk
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        androidResources.enable = true
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xnested-type-aliases")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
        freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
        optIn.add("kotlin.time.ExperimentalTime")
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
            // AndroidX
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.icons.extended)
            implementation(libs.androidx.sqlite.framework)
            implementation(libs.androidx.work.runtime.ktx)

            // Compose tooling
            implementation(libs.compose.ui.tooling)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.navigation3.material3.adaptive)

            // Firebase
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.messaging)

            // Koin
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.androidx.workmanager)

            // KotlinX
            implementation(libs.kotlinx.coroutines.android)

            // Ktor
            implementation(libs.ktor.client.okhttp)
        }

        commonMain.dependencies {
            // Project modules
            implementation(project(":core:analytics"))
            implementation(project(":core:data"))
            implementation(project(":core:database"))
            implementation(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:platform"))
            implementation(project(":core:sync"))
            implementation(project(":core:ui"))
            implementation(project(":core:utils"))


            implementation(project(":feature:onboarding"))

            // Compose
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)

            // Lifecycle & Navigation
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.navigation.compose)

            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // KotlinX
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization.json)

            // Permissions
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.notifications)

            // Third-party
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.kermit)

            // VPlanPlus
            implementation(libs.vpp.sp24)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// Generate a Version.xcconfig that Xcode includes so MARKETING_VERSION and
// CURRENT_PROJECT_VERSION are always in sync with ApplicationConfig.
val generateXcodeVersionConfig by tasks.registering {
    group = "build"
    description = "Writes iosApp/Configuration/Version.xcconfig from ApplicationConfig"
    val outFile = rootProject.file("iosApp/Configuration/Version.xcconfig")
    outputs.file(outFile)
    doLast {
        outFile.parentFile.mkdirs()
        outFile.writeText(
            "// Auto-generated from ApplicationConfig - do not edit manually\n" +
            "MARKETING_VERSION = ${applicationConfig.cfBundleShortVersionString}\n" +
            "CURRENT_PROJECT_VERSION = 1\n"
        )
    }
}

tasks.named("embedAndSignAppleFrameworkForXcode") {
    dependsOn(generateXcodeVersionConfig)
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
