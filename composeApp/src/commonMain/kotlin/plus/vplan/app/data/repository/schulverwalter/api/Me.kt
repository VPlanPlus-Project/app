package plus.vplan.app.data.repository.schulverwalter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Me(
    @SerialName("students") val students: List<Student>
) {
    @Serializable
    data class Student(
        @SerialName("users") val users: List<User>
    )
}