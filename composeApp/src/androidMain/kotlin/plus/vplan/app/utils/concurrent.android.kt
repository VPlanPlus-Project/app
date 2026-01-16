package plus.vplan.app.utils

actual fun currentThreadName(): String {
    return Thread.currentThread().name
}