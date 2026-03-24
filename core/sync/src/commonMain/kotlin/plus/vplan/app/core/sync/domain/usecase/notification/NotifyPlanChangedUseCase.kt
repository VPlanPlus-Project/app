package plus.vplan.app.core.sync.domain.usecase.notification

import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.regularDateFormat
import plus.vplan.app.core.utils.date.untilRelativeText

class NotifyPlanChangedUseCase(
    private val platformNotificationRepository: NotificationRepository,
) {
    private val logger = Logger.withTag("NotifyPlanChangedUseCase")

    suspend operator fun invoke(
        profile: Profile,
        date: LocalDate,
        day: Day,
        changedLessons: List<Lesson>
    ) {
        logger.d { "Sending notification for ${profile.name} with changed lessons: $changedLessons" }
        platformNotificationRepository.sendNotification(
            title = "Neuer Plan (${(LocalDate.now().untilRelativeText(date)) ?: regularDateFormat.format(date)})",
            message = "Es gibt ${changedLessons.size} Änderungen für dich",
            largeText = buildString {
                changedLessons.forEachIndexed { i, lesson ->
                    if (i > 0) append("\n")
                    append(lesson.lessonNumber)
                    append(". ")
                    append(lesson.subject ?: "Entfall")
                    if (lesson.teachers.isNotEmpty()) {
                        append(" mit ")
                        append(lesson.teachers.joinToString(", ") { it.name })
                    }
                    if (lesson.rooms.orEmpty().isNotEmpty()) {
                        append(" in ")
                        append(lesson.rooms.orEmpty().joinToString(", ") { it.name })
                    }
                }
                if (day.info != null) append("\n\nℹ\uFE0F ${day.info}")
            }.dropLastWhile { it == '\n' }.dropWhile { it == '\n' },
            category = profile.name,
            isLarge = true,
            onClickData = Json.encodeToString(
                StartTaskJson(
                    type = "navigate_to",
                    profileId = profile.id.toString(),
                    value = Json.encodeToString(
                        StartTaskJson.StartTaskNavigateTo(
                            screen = "calendar",
                            value = Json.encodeToString(
                                StartTaskJson.StartTaskNavigateTo.StartTaskCalendar(
                                    date = date.toString()
                                )
                            )
                        )
                    )
                )
            ).also { Logger.d { "Task: $it" } },
        )
    }

    fun shouldSendNotification(result: UpdateSubstitutionPlanUseCase.Result.Success, profile: Profile): Boolean {
        val profileResult = result.profileResult.filterKeys { p -> p.id == profile.id }.values.firstOrNull() ?: return false

        if (profileResult.changedLessons.isEmpty()) return false
        if (result.day.date < LocalDate.now()) return false
        if (result.day.date > LocalDate.now()) return true
        if (profileResult.newLessons.maxOf { it.lessonTime?.end ?: LocalTime(0, 0) } < LocalTime.now()) return false
        return true
    }
}