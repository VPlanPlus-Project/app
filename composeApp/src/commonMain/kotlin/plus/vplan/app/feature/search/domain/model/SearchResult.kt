package plus.vplan.app.feature.search.domain.model

import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.feature.calendar.ui.LessonLayoutingInfo
import plus.vplan.app.feature.grades.page.view.ui.GradesItem
import kotlin.uuid.Uuid

sealed class SearchResult(val type: Type) {
    sealed class SchoolEntity(
        type: Type,
        val id: Uuid,
        val name: String,
    ) : SearchResult(type) {

        abstract val lessons: List<LessonLayoutingInfo>

        data class Group(
            val group: plus.vplan.app.core.model.Group,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Group, group.id, group.name)

        data class Teacher(
            val teacher: plus.vplan.app.core.model.Teacher,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Teacher, teacher.id, teacher.name)

        data class Room(
            val room: plus.vplan.app.core.model.Room,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Room, room.id, room.name)
    }

    data class Homework(val homework: PopulatedHomework): SearchResult(Type.Homework)
    data class Assessment(val assessment: PopulatedAssessment): SearchResult(Type.Assessment)
    data class Grade(
        val grade: GradesItem
    ): SearchResult(Type.Grade)

    enum class Type {
        Group, Teacher, Room, Homework, Assessment, Grade
    }
}