package plus.vplan.app.feature.search.domain.model

import plus.vplan.app.core.model.School
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.feature.calendar.ui.LessonLayoutingInfo
import kotlin.uuid.Uuid

sealed class SearchResult(val type: Type) {
    sealed class SchoolEntity(
        type: Type,
        val id: Uuid,
        val name: String,
        val school: School
    ) : SearchResult(type) {

        abstract val lessons: List<LessonLayoutingInfo>

        data class Group(
            val group: plus.vplan.app.domain.model.Group,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Group, group.id, group.name, group.schoolItem!!)

        data class Teacher(
            val teacher: plus.vplan.app.domain.model.Teacher,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Teacher, teacher.id, teacher.name, teacher.school!!)

        data class Room(
            val room: plus.vplan.app.domain.model.Room,
            override val lessons: List<LessonLayoutingInfo>
        ) : SchoolEntity(Type.Room, room.id, room.name, room.school!!)
    }

    data class Homework(val homework: plus.vplan.app.domain.model.Homework): SearchResult(Type.Homework)
    data class Assessment(val assessment: plus.vplan.app.domain.model.Assessment): SearchResult(Type.Assessment)
    data class Grade(val grade: BesteSchuleGrade): SearchResult(Type.Grade)

    enum class Type {
        Group, Teacher, Room, Homework, Assessment, Grade
    }
}