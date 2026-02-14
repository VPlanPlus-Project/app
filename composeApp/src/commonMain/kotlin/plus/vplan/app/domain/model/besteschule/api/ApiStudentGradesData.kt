package plus.vplan.app.domain.model.besteschule.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The type for the list at the /grades endpoint. Represents a single grade
 */
@Serializable
data class ApiStudentGradesData(
    @SerialName("teacher") val teacher: Teacher,
    @SerialName("collection") val collection: Collection,
    @SerialName("value") val value: String,
    @SerialName("id") val id: Int,
    @SerialName("given_at") val givenAt: String
) {
    val isOptional: Boolean
        get() = this.value.startsWith("(") && this.value.endsWith(")")

    val cleanedValue: String?
        get() {
            val regexForGradeInParentheses = "\\((.*?)\\)".toRegex()
            val matchResult = regexForGradeInParentheses.find(this.value)

            val value =
                if (matchResult != null) matchResult.groupValues[1]
                else if (this.value == "-") null
                else this.value

            if (matchResult != null) matchResult.groupValues[1] else this.value

            return value
        }


    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val shortName: String,
        @SerialName("forename") val forename: String,
        @SerialName("name") val lastname: String
    )

    @Serializable
    data class Collection(
        @SerialName("id") val id: Int,
        @SerialName("type") val type: String,
        @SerialName("name") val name: String,
        @SerialName("given_at") val givenAt: String,
        @SerialName("interval_id") val intervalId: Int,
        @SerialName("teacher_id") val teacherId: Int,
        @SerialName("subject_id") val subjectId: Int
    )
}