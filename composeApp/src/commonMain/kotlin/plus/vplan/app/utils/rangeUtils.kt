package plus.vplan.app.utils

inline infix fun <reified T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean {
    return this.endInclusive >= other.start && this.start <= other.endInclusive
}

infix fun Number.progressIn(range: ClosedRange<Double>): Double {
    val start = range.start
    val end = range.endInclusive
    val current = this.toDouble()
    return (current - start) / (end - start)
}