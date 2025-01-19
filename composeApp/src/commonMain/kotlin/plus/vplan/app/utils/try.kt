package plus.vplan.app.utils

fun tryNoCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        // ignore
    }
}