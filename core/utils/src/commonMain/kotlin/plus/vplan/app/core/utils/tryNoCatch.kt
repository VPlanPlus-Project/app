package plus.vplan.app.core.utils

fun tryNoCatch(block: () -> Unit) {
    try {
        block()
    } catch (_: Exception) { /* ignore */ }
}