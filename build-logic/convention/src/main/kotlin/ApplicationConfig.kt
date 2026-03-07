import org.gradle.api.Plugin
import org.gradle.api.Project

val applicationConfig = ApplicationConfig(
    versionMajor = 0,
    versionMinor = 4,
    versionPatch = 5,
    versionSuffix = "internal",
    android = ApplicationConfig.Android(
        minSdk = 24,
        targetSdk = 36
    )
)

class ApplicationConfig(
    versionMajor: Int,
    versionMinor: Int,
    versionPatch: Int,
    versionSuffix: String? = null,
    val android: Android
) {
    val versionVariantCode = when (versionSuffix) {
        null -> "0"
        "production" -> "1"
        "closed" -> "2"
        "internal" -> "3"
        else -> throw Exception("Unknown Version suffix")
    }
    val versionCode = "1${versionMajor.toString().padStart(3, '0')}${versionMinor.toString().padStart(3, '0')}${versionPatch.toString().padStart(3, '0')}$versionVariantCode".toLong()

    val versionName = "${versionMajor}.${versionMinor}.${versionPatch}" +
            versionSuffix?.ifBlank { null }?.let { "-$it" }.orEmpty()

    val cfBundleShortVersionString = "${versionMajor}.${versionMinor}.${versionPatch}.${versionVariantCode}"
    val cfBundleVersion = versionCode

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