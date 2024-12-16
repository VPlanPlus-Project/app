package plus.vplan.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform