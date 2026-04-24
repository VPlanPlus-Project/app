import org.gradle.api.Plugin
import org.gradle.api.Project

val applicationConfig = ApplicationConfig(
    versionMajor = 0,
    versionMinor = 4,
    versionPatch = 19,
    build = 1,
    channel = ApplicationConfig.Channel.Production,
    android = ApplicationConfig.Android(
        minSdk = 24,
        targetSdk = 37
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