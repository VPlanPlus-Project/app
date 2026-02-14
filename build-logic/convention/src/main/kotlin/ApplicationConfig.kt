import org.gradle.api.Plugin
import org.gradle.api.Project

val applicationConfig = ApplicationConfig(
    versionMajor = 0,
    versionMinor = 3,
    versionPatch = 0,
    versionCode = 409,
    android = ApplicationConfig.Android(
        minSdk = 24,
        targetSdk = 36
    )
)

class ApplicationConfig(
    versionMajor: Int,
    versionMinor: Int,
    versionPatch: Int,
    val versionCode: Int,
    val android: Android
) {
    val versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

    data class Android(
        val minSdk: Int,
        val targetSdk: Int
    )
}

@Suppress("unused")
class ApplicationConfigPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.add("applicationConfig", applicationConfig)
    }
}