package plus.vplan.app.data.source.indiware.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("VpMobil")
data class MobdatenClassData(
    @SerialName("FreieTage")
    @XmlChildrenName("ft")
    val holidays: List<String>,

    @SerialName("Klassen")
    @XmlChildrenName("Kl")
    val classes: List<Class>,

    @SerialName("Kopf")
    @Serializable
    val header: Header
) {

    @SerialName("Kopf")
    @Serializable
    data class Header(
        @SerialName("tageprowoche") val daysPerWeek: DaysPerWeek
    ) {
        @SerialName("tageprowoche")
        @Serializable
        data class DaysPerWeek(
            @XmlValue val daysPerWeek: Int
        )
    }

    @SerialName("Kl")
    @Serializable
    data class Class(
        @SerialName("Kurz") val name: ClassName,

        @SerialName("KlStunden")
        @XmlChildrenName("KlSt")
        val lessonTimes: List<ClassLessonTime>,

        @SerialName("Unterricht")
        @XmlChildrenName("Ue")
        val subjectInstances: List<ClassSubjectInstanceWrapper>,

        @SerialName("Kurse")
        @XmlChildrenName("Ku")
        val courses: List<ClassCourseWrapper>
    ) {

        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )

        @SerialName("KlSt")
        @Serializable
        data class ClassLessonTime(
            @XmlSerialName("ZeitVon") val startTime: String,
            @XmlSerialName("ZeitBis") val endTime: String,
            @XmlValue val lessonNumber: Int,
        )

        @Serializable
        @SerialName("Ue")
        data class ClassSubjectInstanceWrapper(
            @SerialName("UeNr") val subjectInstance: ClassSubjectInstance
        ) {
            @Serializable
            @SerialName("UeNr")
            data class ClassSubjectInstance(
                @XmlValue val subjectInstanceNumber: Int,
                @SerialName("UeLe") val teacherName: String,
                @SerialName("UeFa") val subjectName: String,
                @SerialName("UeGr") val courseName: String? = null
            )
        }

        @Serializable
        @SerialName("Ku")
        data class ClassCourseWrapper(
            @SerialName("KKz") val course: ClassCourse
        ) {
            @Serializable
            @SerialName("KKz")
            data class ClassCourse(
                @SerialName("KLe") val courseTeacherName: String,
                @XmlValue val courseName: String
            )
        }
    }
}