package plus.vplan.app.core.model

/**
 * Represents an optional value that can be:
 * - [Present]: The value is explicitly set (including null)
 * - [Absent]: The value is not provided (sentinel for "don't update")
 *
 * This is useful for update operations where null is a valid value
 * but we need to distinguish between "set to null" and "don't change".
 */
sealed class Optional<out T> {
    /**
     * The value is explicitly provided (can be null)
     */
    data class Present<T>(val value: T) : Optional<T>()

    /**
     * The value is not provided (don't update this field)
     */
    data object Absent : Optional<Nothing>()

    /**
     * Returns the value if present, or null if absent
     */
    fun getOrNull(): T? = when (this) {
        is Present -> value
        is Absent -> null
    }

    /**
     * Returns true if this optional contains a value (even if that value is null)
     */
    fun isPresent(): Boolean = this is Present

    /**
     * Returns true if this optional is absent
     */
    fun isAbsent(): Boolean = this is Absent

    companion object {
        /**
         * Creates an Optional with the given value
         */
        fun <T> of(value: T): Optional<T> = Present(value)

        /**
         * Creates an absent Optional
         */
        fun <T> absent(): Optional<T> = Absent
    }
}
