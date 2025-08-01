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
import plus.vplan.app.data.source.database.model.database.DbIndiwareTimetableMetadata
import plus.vplan.app.data.source.indiware.model.MobdatenClassData
import plus.vplan.app.data.source.indiware.model.SPlan
import plus.vplan.app.data.source.indiware.model.VPlan
import plus.vplan.app.data.source.indiware.model.WplanBaseData
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.IndiwareSubstitutionPlan
import plus.vplan.app.domain.repository.IndiwareTimeTable
import plus.vplan.app.utils.parseOrNull
import plus.vplan.app.utils.sha256
import plus.vplan.app.utils.splitWithKnownValuesBySpace
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.IndiwareClient
import plus.vplan.lib.sp24.source.TestConnectionResult
import plus.vplan.lib.sp24.source.extension.LessonTime

class IndiwareRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : IndiwareRepository {

    val clients = mutableMapOf<Authentication, IndiwareClient>()

    override suspend fun checkCredentials(
        authentication: Authentication
    ): Response<Boolean> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        val result = client.testConnection()
        if (result is TestConnectionResult.NotFound) return Response.Error.OnlineError.NotFound
        if (result is TestConnectionResult.Unauthorized) return Response.Success(false)
        if (result is TestConnectionResult.Success) return Response.Success(true)
        return Response.Error.Other(result.toString())
    }

    override suspend fun downloadLessonTimes(
        authentication: Authentication,
        contextWeekIndex: Int?
    ): plus.vplan.lib.sp24.source.Response<List<LessonTime>> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        return client.lessonTime.getLessonTime(contextWeekIndex)
    }

    override suspend fun getWPlanSplan(
        authentication: Authentication,
        weekIndex: Int
    ): plus.vplan.lib.sp24.source.Response<SPlanStudentData> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        return client.getSPlanDataStudent(authentication, schoolWeekIndex = weekIndex)
    }

    @OptIn(ExperimentalXmlUtilApi::class)
    override suspend fun getBaseData(authentication: Authentication): Response<IndiwareBaseData> {
        safeRequest(onError = { return it }) {
            val mobileDataResponse = httpClient.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/mobil/mobdaten/Klassen.xml"
                )
                basicAuth(authentication.username, authentication.password)
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
                    path = "/${authentication.indiwareSchoolId}/wplan/wdatenk/SPlanKl_Basis.xml"
                )
                basicAuth(authentication.username, authentication.password)
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
                                name = baseDataClass.name.name
                            )
                        },
                    rooms = emptyList(),
                    daysPerWeek = mobileClassBaseData.header.daysPerWeek.daysPerWeek,
                    studentsHaveFullAccess = false,
                    weeks = weeks.ifEmpty { null }
                )
            )
        }
        return Response.Error.Cancelled
    }

    @OptIn(ExperimentalXmlUtilApi::class)
    override suspend fun getTimetable(
        sp24Id: String,
        username: String,
        password: String,
        week: Week,
        roomNames: List<String>
    ): Response<IndiwareTimeTable> {
        val hasTimetableInWeek = vppDatabase.indiwareDao.getHasTimetableInWeek(week.id, sp24Id)
        if (hasTimetableInWeek?.hasData == false) return Response.Error.OnlineError.NotFound
        safeRequest(onError = { return it }) {
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
                    vppDatabase.indiwareDao.upsert(DbIndiwareTimetableMetadata(sp24Id, week.id, false, null))
            }

            val rawHash = response.bodyAsText().sha256()

            if (response.status != HttpStatusCode.OK) return response.toResponse()
            vppDatabase.indiwareDao.upsert(DbIndiwareTimetableMetadata(sp24Id, week.id, true, rawHash))
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
                    hasChangedToPrevious = rawHash != hasTimetableInWeek?.rawHash,
                    classes = splan.classes.map { timetableClass ->
                        IndiwareTimeTable.Class(
                            name = timetableClass.name.name,
                            lessons = timetableClass.lessons.map { timetableLesson ->
                                IndiwareTimeTable.Class.Lesson(
                                    dayOfWeek = DayOfWeek(timetableLesson.dayOfWeek.value),
                                    weekType = timetableLesson.weekType?.value,
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
        return Response.Error.Cancelled
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
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/$sp24Id/mobil/mobdaten/PlanKl${date.format(LocalDate.Format {
                        year()
                        monthNumber(Padding.ZERO)
                        dayOfMonth(Padding.ZERO)
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
                                    subject = substitutionPlanLesson.subject.value.let {
                                        if (it == "---") return@let null
                                        else return@let it
                                    },
                                    subjectChanged = (substitutionPlanLesson.subject.changed ?: "").isNotBlank(),
                                    room = substitutionPlanLesson.room.value.splitWithKnownValuesBySpace(roomNames),
                                    roomChanged = (substitutionPlanLesson.room.changed ?: "").isNotBlank(),
                                    teacher = substitutionPlanLesson.teacher.value.splitWithKnownValuesBySpace(teacherNames),
                                    teacherChanged = (substitutionPlanLesson.teacher.changed ?: "").isNotBlank(),
                                    info = substitutionPlanLesson.info.value.ifBlank { null },
                                    start = LocalTime.parseOrNull(substitutionPlanLesson.start.value),
                                    end = LocalTime.parseOrNull(substitutionPlanLesson.end.value),
                                    subjectInstanceNumber = substitutionPlanLesson.subjectInstanceNumber?.value?.toIntOrNull()
                                )
                            }
                        )
                    }
                )
            )
        }
        return Response.Error.Cancelled
    }

    override suspend fun getSp24Client(
        authentication: Authentication,
        withCache: Boolean
    ): IndiwareClient {
        if (withCache) return IndiwareClient(
            authentication = authentication,
            client = httpClient,
            enableInternalCache = true
        )
        return clients.getOrPut(authentication) {
            IndiwareClient(
                authentication = authentication,
                client = httpClient,
                enableInternalCache = false
            )
        }
    }
}