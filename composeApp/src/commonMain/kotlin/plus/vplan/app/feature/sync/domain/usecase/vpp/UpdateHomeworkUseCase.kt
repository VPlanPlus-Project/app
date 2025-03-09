package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Duration.Companion.days

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke(allowNotifications: Boolean) {
        val ids = mutableSetOf<Int>()
        profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>().forEach { studentProfile ->
            val existingHomework = homeworkRepository.getByGroup(studentProfile.group).first().filterIsInstance<Homework.CloudHomework>().map { it.id }.toSet()
            ids.addAll(
                (homeworkRepository.download(
                    schoolApiAccess = studentProfile.getVppIdItem()?.buildSchoolApiAccess(studentProfile.getSchoolItem().id) ?: studentProfile.getSchool().getFirstValue()!!.getSchoolApiAccess()!!,
                    groupId = studentProfile.group,
                    subjectInstanceIds = studentProfile.subjectInstanceConfiguration.map { it.key },
                ) as? Response.Success)?.data.orEmpty().also {
                    if (allowNotifications) {
                        val newHomework = combine((it - existingHomework).ifEmpty { return@also }.map { id -> homeworkRepository.getById(id, false).filterIsInstance<CacheState.Done<Homework>>().map { homework -> homework.data } }) { list -> list.toList() }.first()
                            .filterIsInstance<Homework.CloudHomework>()
                            .filter { homework -> homework.createdBy != studentProfile.vppIdId && (homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) <= 2.days }

                        if (newHomework.size == 1) {
                            platformNotificationRepository.sendNotification(
                                title = "Neue Hausaufgabe",
                                category = studentProfile.name,
                                message = buildString {
                                    newHomework.first().let { homework ->
                                        append(homework.getCreatedBy().name)
                                        append(" hat eine neue Hausaufgabe ")
                                        if (homework.subjectInstance == null) append("für Klasse ${homework.group?.getFirstValue()?.name}")
                                        else append("für ${homework.subjectInstance?.getFirstValue()?.subject}")
                                        append(" erstellt, welche bis ")
                                        append(homework.dueTo.let { date ->
                                            (LocalDate.now() untilRelativeText date) ?: date.format(LocalDate.Format {
                                                dayOfWeek(shortDayOfWeekNames)
                                                chars(", ")
                                                dayOfMonth()
                                                chars(". ")
                                                monthName(shortMonthNames)
                                            })
                                        })
                                        append(" fällig ist.")
                                    }
                                },
                                onClickData = Json.encodeToString(
                                    StartTaskJson(
                                        type = "open",
                                        profileId = studentProfile.id.toString(),
                                        value = Json.encodeToString(
                                            StartTaskJson.StartTaskOpen(
                                                type = "homework",
                                                value = Json.encodeToString(
                                                    StartTaskJson.StartTaskOpen.Homework(
                                                        homeworkId = newHomework.first().id
                                                    )
                                                )
                                            )
                                        )
                                    ).also { Logger.d { "Task: $it" } }
                                )
                            )
                        }
                        if (newHomework.size > 1) {
                            platformNotificationRepository.sendNotification(
                                title = "Neue Hausaufgaben",
                                category = studentProfile.name,
                                message = buildString {
                                    append("Es gibt ${newHomework.size} neue Hausaufgaben für dich")
                                },
                                isLarge = false,
                            )
                        }
                    }
                }
            )
        }

        homeworkRepository.deleteById(
            homeworkRepository
                .getAll()
                .first()
                .asSequence()
                .filterIsInstance<CacheState.Done<Homework>>()
                .map { it.data }
                .filterIsInstance<Homework.CloudHomework>()
                .filter { it.id !in ids }
                .map { it.id }
                .toList()
        )
    }
}