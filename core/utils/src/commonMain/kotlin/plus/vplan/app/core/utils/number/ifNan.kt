package plus.vplan.app.core.utils.number

inline fun <reified T: Number> T.ifNan(block: () -> T): T {
    return when (this) {
        is Float -> if (this.isNaN()) block() else this
        is Double -> if (this.isNaN()) block() else this
        else -> this
    }
}
