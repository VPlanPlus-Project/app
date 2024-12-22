package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbIndiwareHasTimetableInWeek
import plus.vplan.app.data.source.indiware.model.MobdatenClassData
import plus.vplan.app.data.source.indiware.model.SPlan
import plus.vplan.app.data.source.indiware.model.VPlan
import plus.vplan.app.data.source.indiware.model.WplanBaseData
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.IndiwareSubstitutionPlan
import plus.vplan.app.domain.repository.IndiwareTimeTable
import plus.vplan.app.utils.splitWithKnownValuesBySpace

class IndiwareRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
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
        val mobileDataResponse = httpClient.get {
            url(
                scheme = "https",
                host = "stundenplan24.de",
                path = "/$sp24Id/mobil/mobdaten/Klassen.xml"
            )
            basicAuth(username, password)
        }
        if (mobileDataResponse.status != HttpStatusCode.OK) return mobileDataResponse.toResponse()
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
            string = mobileDataResponse.bodyAsText()
        )

        val weeks = mutableListOf<IndiwareBaseData.Week>()

        val wplanBaseDataResponse = httpClient.get {
            url(
                scheme = "https",
                host = "stundenplan24.de",
                path = "/$sp24Id/wplan/wdatenk/SPlanKl_Basis.xml"
            )
            basicAuth(username, password)
        }
        if (wplanBaseDataResponse.status == HttpStatusCode.OK) {
            val wplanBaseData = xml.decodeFromString(
                deserializer = WplanBaseData.serializer(),
                string = wplanBaseDataResponse.bodyAsText()
            )
            weeks.removeAll { it.calendarWeek in wplanBaseData.schoolWeeks.map { data -> data.calendarWeek } }
            weeks.addAll(
                wplanBaseData.schoolWeeks
                    .map { schoolWeek ->
                        val format = LocalDate.Format {
                            dayOfMonth()
                            char('.')
                            monthNumber()
                            char('.')
                            year()
                        }
                        IndiwareBaseData.Week(
                            calendarWeek = schoolWeek.calendarWeek,
                            start = LocalDate.parse(schoolWeek.start, format),
                            end = LocalDate.parse(schoolWeek.end, format),
                            weekType = schoolWeek.weekType,
                            weekIndex = schoolWeek.weekIndex
                        )
                    }
            )
        }

        val lessonTimeFormat = LocalTime.Format {
            hour(Padding.NONE)
            char(':')
            minute()
        }

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
                                        start = LocalTime.parse(baseDataClassLessonTime.startTime.trim(), lessonTimeFormat),
                                        end = LocalTime.parse(baseDataClassLessonTime.endTime.trim(), lessonTimeFormat),
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
                downloadMode = School.IndiwareSchool.SchoolDownloadMode.INDIWARE_MOBIL,
                weeks = weeks.ifEmpty { null }
            )
        )
    }

    @OptIn(ExperimentalXmlUtilApi::class)
    override suspend fun getTimetable(
        sp24Id: String,
        username: String,
        password: String,
        week: Week,
        roomNames: List<String>
    ): Response<IndiwareTimeTable> {
        val hasTimetableInWeek = vppDatabase.indiwareDao.getHasTimetableInWeek(week.id)
        if (hasTimetableInWeek == false) return Response.Error.OnlineError.NotFound
        return saveRequest {
            val response = httpClient.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/$sp24Id/wplan/wdatenk/SPlanKl_Sw${week.weekIndex}.xml"
                )
                basicAuth(username, password)
            }
            if (response.status == HttpStatusCode.NotFound) {
                if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date !in week.start..week.end)
                    vppDatabase.indiwareDao.upsert(DbIndiwareHasTimetableInWeek(week.id, false))
            }
            if (response.status != HttpStatusCode.OK) return response.toResponse()
            vppDatabase.indiwareDao.upsert(DbIndiwareHasTimetableInWeek(week.id, true))
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
            val splan = xml.decodeFromString(
                deserializer = SPlan.serializer(),
                string = response.bodyAsText()
            )
            return Response.Success(
                IndiwareTimeTable(
                    classes = splan.classes.map { timetableClass ->
                        IndiwareTimeTable.Class(
                            name = timetableClass.name.name,
                            lessons = timetableClass.lessons.map { timetableLesson ->
                                IndiwareTimeTable.Class.Lesson(
                                    dayOfWeek = DayOfWeek(timetableLesson.dayOfWeek.value),
                                    lessonNumber = timetableLesson.lessonNumber.value,
                                    subject = timetableLesson.subject.value,
                                    teacher = timetableLesson.teacher.value.split(","),
                                    room = timetableLesson.room.value.splitWithKnownValuesBySpace(roomNames),
                                )
                            }
                        )
                    }
                )
            )
        }
    }

    @OptIn(ExperimentalXmlUtilApi::class)
    override suspend fun getSubstitutionPlan(
        sp24Id: String,
        username: String,
        password: String,
        date: LocalDate,
        teacherNames: List<String>,
        roomNames: List<String>
    ): Response<IndiwareSubstitutionPlan> {
        return saveRequest {
            val response = httpClient.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/$sp24Id/mobil/mobdaten/PlanKl${date.format(LocalDate.Format { 
                        year()
                        monthNumber()
                        dayOfMonth()
                    })}.xml"
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

            val substitutionPlan = xml.decodeFromString(
                deserializer = VPlan.serializer(),
                string = response.bodyAsText()
            )
            return Response.Success(
                data = IndiwareSubstitutionPlan(
                    date = date,
                    info = substitutionPlan.info.mapNotNull { it.value.ifBlank { null } }.ifEmpty { null }?.joinToString("\n"),
                    classes = substitutionPlan.classes.map { substitutionPlanClass ->
                        IndiwareSubstitutionPlan.Class(
                            name = substitutionPlanClass.name.name,
                            lessons = substitutionPlanClass.lessons.map { substitutionPlanLesson ->
                                IndiwareSubstitutionPlan.Class.Lesson(
                                    lessonNumber = substitutionPlanLesson.lessonNumber.value,
                                    subject = substitutionPlanLesson.subject.value,
                                    subjectChanged = (substitutionPlanLesson.subject.changed ?: "").isNotBlank(),
                                    room = substitutionPlanLesson.room.value.splitWithKnownValuesBySpace(roomNames),
                                    roomChanged = (substitutionPlanLesson.room.changed ?: "").isNotBlank(),
                                    teacher = substitutionPlanLesson.teacher.value.splitWithKnownValuesBySpace(teacherNames),
                                    teacherChanged = (substitutionPlanLesson.teacher.changed ?: "").isNotBlank(),
                                    info = substitutionPlanLesson.info.value.ifBlank { null },
                                    start = LocalTime.parse(substitutionPlanLesson.start.value),
                                    end = LocalTime.parse(substitutionPlanLesson.end.value),
                                    defaultLessonNumber = substitutionPlanLesson.defaultLessonNumber?.value?.toIntOrNull()
                                )
                            }
                        )
                    }
                )
            )
        }
    }
}