package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import plus.vplan.app.data.source.indiware.model.MobdatenClassData
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository

class IndiwareRepositoryImpl(
    private val httpClient: HttpClient
) : IndiwareRepository {
    override suspend fun checkCredentials(
        sp24Id: Int,
        username: String,
        password: String
    ): Response<Boolean> {
        return saveRequest {
            val response = httpClient.get {
                url("https://stundenplan24.de/$sp24Id/mobil/")
                basicAuth(username, password)
            }
            if (response.status == HttpStatusCode.OK) return Response.Success(true)
            if (response.status == HttpStatusCode.Unauthorized) return Response.Success(false)
            return response.toResponse()
        }
    }

    @OptIn(ExperimentalXmlUtilApi::class)
    override suspend fun getBaseData(
        sp24Id: String,
        username: String,
        password: String
    ): Response<IndiwareBaseData> {
        val response = httpClient.get {
            url(
                scheme = "https",
                host = "stundenplan24.de",
                path = "/$sp24Id/mobil/mobdaten/Klassen.xml"
            )
            basicAuth(username, password)
        }
        if (response.status != HttpStatusCode.OK) return response.toResponse()
        val xml: XML by lazy {
            XML {
                xmlVersion = XmlVersion.XML10
                xmlDeclMode = XmlDeclMode.Auto
                indentString = "  "
                repairNamespaces = true
                defaultPolicy {
                    unknownChildHandler = IGNORING_UNKNOWN_CHILD_HANDLER
                }
            }
        }
        val mobileClassBaseData = xml.decodeFromString(
            deserializer = MobdatenClassData.serializer(),
            string = response.bodyAsText()
        )

        return Response.Success(
            IndiwareBaseData(
                holidays = mobileClassBaseData
                    .holidays
                    .map {
                        val format = LocalDate.Format {
                            yearTwoDigits(2000)
                            monthNumber()
                            dayOfMonth()
                        }
                        LocalDate.parse(it, format)
                    },
                classes = mobileClassBaseData
                    .classes
                    .map { baseDataClass ->
                        IndiwareBaseData.Class(
                            name = baseDataClass.name.name,
                            lessonTimes = baseDataClass.lessonTimes
                                .map { baseDataClassLessonTime ->
                                    IndiwareBaseData.Class.LessonTime(
                                        start = LocalTime.parse(baseDataClassLessonTime.startTime),
                                        end = LocalTime.parse(baseDataClassLessonTime.endTime),
                                        lessonNumber = baseDataClassLessonTime.lessonNumber
                                    )
                                },
                            defaultLessons = baseDataClass.defaultLessons
                                .map { baseDataClassDefaultLesson ->
                                    IndiwareBaseData.Class.DefaultLesson(
                                        subject = baseDataClassDefaultLesson.defaultLesson.subjectName,
                                        teacher = baseDataClassDefaultLesson.defaultLesson.teacherName.ifBlank { null },
                                        defaultLessonNumber = "sp24.$sp24Id.${baseDataClassDefaultLesson.defaultLesson.defaultLessonNumber}",
                                        course = if (baseDataClassDefaultLesson.defaultLesson.courseName == null) null else baseDataClass.courses.first { it.course.courseName == baseDataClassDefaultLesson.defaultLesson.courseName }.let {
                                            IndiwareBaseData.Class.Course(
                                                name = it.course.courseName,
                                                teacher = it.course.courseTeacherName.ifBlank { null }
                                            )
                                        }
                                    )
                                }
                        )
                    },
                teachers = mobileClassBaseData
                    .classes
                    .flatMap { it.defaultLessons }
                    .map { it.defaultLesson.teacherName }
                    .distinct(),
                rooms = emptyList(),
                daysPerWeek = mobileClassBaseData.header.daysPerWeek.daysPerWeek,
                studentsHaveFullAccess = false,
                downloadMode = School.IndiwareSchool.SchoolDownloadMode.INDIWARE_MOBIL
            )
        )
    }
}