import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin that wires up a shared AppBuildConfig for any KMP module.
 *
 * Applies the buildconfig plugin and populates the standard app-wide fields
 * (APP_VERSION, APP_VERSION_CODE) from [ApplicationConfig], which is already
 * registered as a project extension by [ApplicationConfigPlugin].
 *
 * The generated class is placed in the module's own package under the name
 * "AppBuildConfig", so each module gets e.g.
 *   plus.vplan.app.feature.onboarding.AppBuildConfig
 *
 * Modules that need additional fields (e.g. POSTHOG_API_KEY) should configure
 * the buildConfig { } block themselves after applying this plugin.
 */
class KmpBuildConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.github.gmazzo.buildconfig")
        pluginManager.apply("vplanplus.build.applicationConfig")

        configure<BuildConfigExtension> {
            useKotlinOutput {
                topLevelConstants = false
                internalVisibility = false
            }

            className("AppBuildConfig")

            buildConfigField("Int", "APP_VERSION_CODE", applicationConfig.versionCode.toString())
            buildConfigField("String", "APP_VERSION", "\"${applicationConfig.versionName}\"")
        }
    }
}
