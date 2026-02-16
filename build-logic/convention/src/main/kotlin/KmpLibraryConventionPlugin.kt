import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        extensions.configure<KotlinMultiplatformExtension> {
            // Android target is created by the com.android.kotlin.multiplatform.library plugin
            // We configure it via targets API
            targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget::class.java) {
                publishLibraryVariantsGroupedByFlavor = true
            }

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = project.name.replaceFirstChar { it.uppercase() }
                    isStatic = true
                }
            }

            sourceSets.commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())
            }
            sourceSets.commonTest.dependencies {
                implementation(kotlin("test"))
            }
        }

        // Add compiler arguments for all Kotlin compilations
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
