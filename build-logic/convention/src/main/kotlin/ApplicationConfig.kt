import org.gradle.api.Plugin
import org.gradle.api.Project

val applicationConfig = ApplicationConfig(
    versionMajor = 0,
    versionMinor = 4,
    versionPatch = 14,
    build = 7,
    channel = ApplicationConfig.Channel.Internal,
    android = ApplicationConfig.Android(
        minSdk = 24,
        targetSdk = 36
    )
)

class ApplicationConfig(
    versionMajor: Int,
    versionMinor: Int,
    versionPatch: Int,
    build: Int,
    channel: Channel,
    val android: Android
) {
    @Suppress("unused")
    enum class Channel(val suffix: String, val versionVariantCode: Int) {
        Production("production", 0),
        Open("open", 1),
        Closed("closed", 2),
        Internal("internal", 3)
    }

    val versionCode = versionMajor * 10000000 +
            versionMinor * 100000 +
            versionPatch * 1000 +
            build * 10 +
            channel.versionVariantCode

    val versionName = "${versionMajor}.${versionMinor}.${versionPatch}.${build}-${channel.suffix}"

    val cfBundleShortVersionString = "${versionMajor}.${versionMinor}.${versionPatch}"
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