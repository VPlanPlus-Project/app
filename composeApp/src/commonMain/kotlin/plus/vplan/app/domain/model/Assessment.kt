package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class Assessment(
    val id: Int,
    val creator: AppEntity,
    val createdAt: LocalDateTime,
    val date: LocalDate,
    val isPublic: Boolean,
    val defaultLessonId: Int,
    val description: String,
    val type: Type,
) {
    enum class Type {
        SHORT_TEST, CLASS_TEST, PROJECT, ORAL, OTHER
    }
}