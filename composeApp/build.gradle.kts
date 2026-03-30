@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import groovy.lang.MissingFieldException
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "VPlanPlusShared"
            isStatic = false

//            export(project(":core:analytics"))
            export(project(":core:data"))
//            export(project(":core:database"))
//            export(project(":core:model"))
//            export(project(":core:network"))
//            export(project(":core:platform"))
//            export(project(":core:sync"))
            export(project(":core:ui"))
            export(project(":core:utils"))
//            export(project(":core:common"))


            export(project(":feature:assessment:detail"))
            export(project(":feature:assessment:create"))
            export(project(":feature:assessment:core"))
            export(project(":feature:homework:detail"))
            export(project(":feature:homework:create"))
            export(project(":feature:homework:core"))
            export(project(":feature:file:core"))
            export(project(":feature:calendar"))
            export(project(":feature:onboarding"))
            export(project(":feature:grades"))

        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        androidMain.dependencies {
            // AndroidX
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.biometric)
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
            implementation(project(":core:common"))


            implementation(project(":feature:assessment:detail"))
            implementation(project(":feature:assessment:create"))
            implementation(project(":feature:assessment:core"))
            implementation(project(":feature:homework:detail"))
            implementation(project(":feature:homework:create"))
            implementation(project(":feature:homework:core"))
            implementation(project(":feature:file:core"))
            implementation(project(":feature:calendar"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:grades"))

            // Compose
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
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
            api(libs.kmp.observable.viewmodel)

            // VPlanPlus
            implementation(libs.vpp.sp24)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

            // We need to use api here so that they are correctly included in the iOS build as we
            // export them in the iOS target.
            api(project(":core:data"))
            api(project(":core:ui"))
            api(project(":core:utils"))

            api(project(":feature:assessment:detail"))
            api(project(":feature:assessment:create"))
            api(project(":feature:assessment:core"))
            api(project(":feature:homework:detail"))
            api(project(":feature:homework:create"))
            api(project(":feature:homework:core"))
            api(project(":feature:file:core"))
            api(project(":feature:calendar"))
            api(project(":feature:onboarding"))
            api(project(":feature:grades"))
        }
    }
}

val generateXcodeVersionConfig by tasks.registering {
    group = "build"
    description = "Writes iosApp/Configuration/Version.xcconfig from ApplicationConfig"
    val outFile = rootProject.file("iosApp/Configuration/Version.xcconfig")

    val marketingVersion = applicationConfig.cfBundleShortVersionString

    outputs.file(outFile)
    doLast {
        outFile.parentFile.mkdirs()
        outFile.writeText(
            "// Auto-generated from ApplicationConfig - do not edit manually\n" +
                    "MARKETING_VERSION = $marketingVersion\n" +
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
