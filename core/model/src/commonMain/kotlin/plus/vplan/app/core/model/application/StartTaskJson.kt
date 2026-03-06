package plus.vplan.app.core.model.application

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.Alias

@Serializable
data class StartTaskJson(
    @SerialName("type") val type: String,
    @SerialName("profile_id") val profileId: String? = null,
    @SerialName("value") val value: String
) {
    @Serializable
    data class StartTaskOpen(
        @SerialName("type") val type: String,
        @SerialName("payload") val value: String
    ) {
        @Serializable
        data class Homework(
            @SerialName("homework_id") val homeworkId: Int
        )

        @Serializable
        data class SchulverwalterReauth(
            @SerialName("user_id") val userId: Int,
        )

        @Serializable
        data class Assessment(
            @SerialName("assessment_id") val assessmentId: Int
        )

        @Serializable
        data class Grade(
            @SerialName("grade_id") val gradeId: Int
        )
    }

    @Serializable
    data class StartTaskNavigateTo(
        @SerialName("screen") val screen: String,
        @SerialName("payload") val value: String?
    ) {
        @Serializable
        data class StartTaskCalendar(
            @SerialName("date") val date: String
        )

        @Serializable
        data class SchoolSettings(
            @SerialName("open_school_alias") val openIndiwareSettingsSchool: Alias? = null,
        )

        @Serializable
        data class Grades(
            @SerialName("vpp_id") val vppId: Int
        )
    }
}