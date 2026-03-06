package plus.vplan.app.core.platform

enum class AppPlatform {
    Android, iOS
}

interface PlatformRepository {
    fun getPlatform(): AppPlatform
}
