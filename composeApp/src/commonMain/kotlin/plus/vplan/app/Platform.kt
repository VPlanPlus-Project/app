package plus.vplan.app

enum class Platform {
    Android, iOS
}

expect fun getPlatform(): Platform
