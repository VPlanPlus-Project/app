package plus.vplan.app.data.source.indiware.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class SPlan(
    @SerialName("Klassen")
    @XmlChildrenName("Kl")
    val classes: List<Class>,
) {
    @Serializable
    @SerialName("Kl")
    data class Class(
        @SerialName("Kurz") val name: ClassName,
        @SerialName("Pl")
        @XmlChildrenName("Std")
        val lessons: List<ClassLesson>
    ) {
        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )

        @Serializable
        @SerialName("Std")
        data class ClassLesson(
            @SerialName("PlTg") val dayOfWeek: DayOfWeek,
            @SerialName("PlSt") val lessonNumber: LessonNumber,
            @SerialName("PlFa") val subject: Subject,
            @SerialName("PlKl") val lessonClass: Class,
            @SerialName("PlLe") val teacher: Teacher,
            @SerialName("PlRa") val room: Room,
        ) {
            @Serializable
            @SerialName("PlTg")
            data class DayOfWeek(
                @XmlValue val value: Int
            )

            @Serializable
            @SerialName("PlSt")
            data class LessonNumber(
                @XmlValue val value: Int
            )

            @Serializable
            @SerialName("PlFa")
            data class Subject(
                @XmlValue val value: String
            )

            @Serializable
            @SerialName("PlKl")
            data class Class(
                @XmlValue val group: String
            )

            @Serializable
            @SerialName("PlLe")
            data class Teacher(
                @XmlValue val value: String
            )

            @Serializable
            @SerialName("PlRa")
            data class Room(
                @XmlValue val value: String
            )
        }
    }
}