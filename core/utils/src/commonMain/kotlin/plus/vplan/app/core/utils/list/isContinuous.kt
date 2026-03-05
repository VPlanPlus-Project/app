package plus.vplan.app.core.utils.list

fun List<Int>.isContinuous(): Boolean {
    if (isEmpty()) return true
    var last = first()
    drop(1).forEach { current ->
        if (current != last + 1) return false
        last = current
    }
    return true
}