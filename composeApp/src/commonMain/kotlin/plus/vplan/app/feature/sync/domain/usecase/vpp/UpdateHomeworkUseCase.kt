@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import plus.vplan.app.StartTaskJson
import plus.vplan.app.capture
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.data.getByProvider
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.toLocalDateTime
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase
) {
    private val logger = Logger.withTag("UpdateHomeworkUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val downloadedIds = mutableSetOf<Int>()
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        val existingIds = mutableSetOf<Int>()
        profiles.forEach forEachProfile@{ studentProfile ->
            val group = studentProfile.group.getFirstValue() ?: run {
                val errorMessage = "Group not found for profile ${studentProfile.name} (${studentProfile.id})"
                capture("error", mapOf(
                    "location" to "UpdateHomeworkUseCase",
                    "message" to errorMessage
                ))
                logger.e { errorMessage }
                return@forEachProfile
            }
            val existingHomeworkIds = homeworkRepository.getByGroup(group).first().filterIsInstance<Homework.CloudHomework>().map { it.id }.toSet()
            existingIds.addAll(existingHomeworkIds)
            val school = studentProfile.getSchool().getFirstValue()

            // require vpp provider for school
            school?.aliases?.getByProvider(AliasProvider.Vpp)?.value?.toInt() ?: run {
                logger.e { "No vpp provider for school $school" }
                return@forEachProfile
            }

            val enabledSubjectInstanceIds = studentProfile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds
                .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                .flatMap { it.aliases }

            logger.d { "Downloading homework for ${group.name}" }
            val downloaded = homeworkRepository.download(school.buildSp24AppAuthentication(), group.aliases.toList(), subjectInstanceAliases)
            if (downloaded !is Response.Success) {
                logger.e { "Failed to download homework for profile ${studentProfile.name} (${studentProfile.id}): $downloaded" }
                return@forEachProfile
            }

            downloadedIds.addAll(downloaded.data)
        }

        profiles.forEach { studentProfile ->
            updateProfileHomeworkIndexUseCase(studentProfile)

            run buildAndSendNotifications@{
                if (allowNotifications) {
                    logger.d { "Checking if new homework is created" }
                    val allowedSubjectInstanceVppIds = studentProfile.subjectInstanceConfiguration
                        .filterValues { it }
                        .mapKeys { subjectInstanceRepository.getByLocalId(it.key).first() }
                        .keys
                        .filterNotNull()
                        .mapNotNull { it.aliases.firstOrNull { alias -> alias.provider == AliasProvider.Vpp }?.value?.toInt() }

                    val newHomework = combine((downloadedIds - existingIds).ifEmpty { return@buildAndSendNotifications }.map { id -> homeworkRepository.getById(id, false).filterIsInstance<CacheState.Done<Homework>>().map { homework -> homework.data } }) { list -> list.toList() }.first()
                        .filterIsInstance<Homework.CloudHomework>()
                        .filter { homework -> homework.createdBy != studentProfile.vppIdId && (homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) <= 2.days }
                        .filter { it.subjectInstanceId == null || it.subjectInstanceId in allowedSubjectInstanceVppIds }

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
        }
    }
}