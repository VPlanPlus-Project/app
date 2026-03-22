import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Convention plugin for KMP libraries that include Jetpack Compose.
 *
 * Applies on top of the base KMP setup (targets, coroutines, datetime) and additionally:
 * - Applies the Compose Multiplatform plugin and the Kotlin Compose compiler plugin
 * - Adds the standard Compose dependency set to commonMain
 * - Wires up the ui-tooling runtime for @Preview support (androidRuntimeClasspath,
 *   required when using the com.android.kotlin.multiplatform.library AGP 9 plugin)
 */
class KmpComposeLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        extensions.configure<KotlinMultiplatformExtension> {
            targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget::class.java) {
                publishLibraryVariantsGroupedByFlavor = true
            }

            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
//                iosTarget.binaries.framework {
//                    baseName = project.name.replaceFirstChar { it.uppercase() }
//                    isStatic = false
//                }
            }

            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())

                // Compose
                implementation(libs.findLibrary("compose-components-resources").get())
                implementation(libs.findLibrary("compose-foundation").get())
                implementation(libs.findLibrary("compose-material3").get())
                implementation(libs.findLibrary("compose-runtime").get())
                implementation(libs.findLibrary("compose-ui").get())
                implementation(libs.findLibrary("compose-ui-tooling-preview").get())

                // Lifecycle & Navigation
                implementation(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())

                // Koin
                implementation(libs.findLibrary("koin-core").get())
                implementation(libs.findLibrary("koin-compose").get())
                implementation(libs.findLibrary("koin-compose-viewmodel").get())

                implementation(libs.findLibrary("kermit").get())
            }
            sourceSets.commonTest.dependencies {
                implementation(kotlin("test"))
            }

            sourceSets.androidMain.dependencies {
                implementation(libs.findLibrary("compose-ui-tooling").get())
            }
        }

        tasks.withType(KotlinCompilationTask::class.java).configureEach {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-opt-in=kotlin.uuid.ExperimentalUuidApi",
                    "-opt-in=kotlin.time.ExperimentalTime"
                )
            }
        }
    }
}
