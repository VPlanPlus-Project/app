import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

/**
 * Convention plugin that wires up a shared AppBuildConfig for any KMP module.
 *
 * Applies the buildconfig plugin and populates the standard app-wide fields
 * (APP_VERSION_CODE, APP_VERSION, APP_DEBUG, POSTHOG_API_KEY) from [ApplicationConfig]
 * and local.properties, which is already registered as a project extension by
 * [ApplicationConfigPlugin].
 *
 * The generated class is placed in the module's own package under the name
 * "AppBuildConfig", so each module gets e.g.
 *   plus.vplan.app.feature.onboarding.AppBuildConfig
 */
@Suppress("unused")
class KmpBuildConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.github.gmazzo.buildconfig")
        pluginManager.apply("vplanplus.build.applicationConfig")

        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use { load(it) }
        }

        configure<BuildConfigExtension> {
            useKotlinOutput {
                topLevelConstants = false
                internalVisibility = false
            }

            className("AppBuildConfig")

            buildConfigField("Int", "APP_VERSION_CODE", applicationConfig.versionCode.toString())
            buildConfigField("String", "APP_VERSION", "\"${applicationConfig.versionName}\"")
            buildConfigField("Boolean", "APP_DEBUG", (localProperties.getProperty("app.debug")?.toBoolean() ?: true).toString())
            buildConfigField("String", "POSTHOG_API_KEY", "\"${localProperties.getProperty("posthog.api.key") ?: ""}\"")
        }
    }
}
