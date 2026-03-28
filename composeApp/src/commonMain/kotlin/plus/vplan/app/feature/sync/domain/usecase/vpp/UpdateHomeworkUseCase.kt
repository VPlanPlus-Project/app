package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.shortDayOfWeekNames
import plus.vplan.app.core.utils.date.shortMonthNames
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.date.untilRelativeText
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: NotificationRepository,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase,
) {
    private val logger = Logger.withTag("UpdateHomeworkUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        
        // Track homework IDs before and after sync for each profile
        val existingIdsByProfile = mutableMapOf<Profile.StudentProfile, Set<Int>>()
        val downloadedIdsByProfile = mutableMapOf<Profile.StudentProfile, Set<Int>>()
        
        profiles.forEach forEachProfile@{ studentProfile ->
            // Track existing homework before sync
            val existingHomeworkIds = homeworkRepository.getByGroup(studentProfile.group)
                .first()
                .filterIsInstance<Homework.CloudHomework>()
                .map { it.id }
                .toSet()
            existingIdsByProfile[studentProfile] = existingHomeworkIds

            // Sync homework for this profile's VPP ID
            logger.d { "Syncing homework for ${studentProfile.group.name}" }
            try {
                homeworkRepository.sync(studentProfile)
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync homework for profile ${studentProfile.name} (${studentProfile.id})" }
                return@forEachProfile
            }

            // Track downloaded homework after sync
            val downloadedHomeworkIds = homeworkRepository.getByGroup(studentProfile.group)
                .first()
                .filterIsInstance<Homework.CloudHomework>()
                .map { it.id }
                .toSet()
            downloadedIdsByProfile[studentProfile] = downloadedHomeworkIds
        }

        // Clean up deleted homework (homework that existed before but not after sync)
        val allExistingIds = existingIdsByProfile.values.flatten().toSet()
        val allDownloadedIds = downloadedIdsByProfile.values.flatten().toSet()
        
        (allExistingIds.filter { it > 0 } - allDownloadedIds).forEach { deletionCandidate ->
            logger.d { "Testing deletionCandidate (ID: $deletionCandidate)" }
            val item = homeworkRepository.getById(deletionCandidate).first()
            if (item == null) {
                homeworkRepository.deleteById(deletionCandidate)
                logger.d { "Deleted $deletionCandidate" }
            }
        }

        // Update indices and send notifications
        profiles.forEach { studentProfile ->
            logger.d { "Updating index for ${studentProfile.name}" }
            updateProfileHomeworkIndexUseCase(studentProfile)

            logger.d { "Checking if notifications should be sent" }
            run buildAndSendNotifications@{
                if (!allowNotifications) return@buildAndSendNotifications
                
                val existingIds = existingIdsByProfile[studentProfile] ?: emptySet()
                val downloadedIds = downloadedIdsByProfile[studentProfile] ?: emptySet()
                val newHomeworkIds = downloadedIds - existingIds
                
                if (newHomeworkIds.isEmpty()) return@buildAndSendNotifications

                logger.d { "Checking if new homework is created" }
                val allowedSubjectInstanceIds = studentProfile.subjectInstanceConfiguration
                    .filterValues { it }
                    .keys
                    .map { it.id }

                val newHomework = combine(newHomeworkIds.map { id -> 
                    homeworkRepository.getById(id).map { homework -> homework }
                }) { list -> list.toList().filterNotNull() }
                    .first()
                    .filterIsInstance<Homework.CloudHomework>()
                    .filter { homework ->
                        homework.creator.vppId.id != studentProfile.vppId?.id &&
                        (homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) until 
                         Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) <= 2.days
                    }
                    .filter { it.subjectInstance == null || it.subjectInstance!!.id in allowedSubjectInstanceIds }

                if (newHomework.size == 1) {
                    platformNotificationRepository.sendNotification(
                        title = "Neue Hausaufgabe",
                        category = studentProfile.name,
                        message = buildString {
                            newHomework.first().let { homework ->
                                append(homework.creator.vppId.name)
                                append(" hat eine neue Hausaufgabe ")
                                if (homework.subjectInstance == null) append("für Klasse ${homework.group?.name}")
                                else append("für ${homework.subjectInstance!!.subject}")
                                append(" erstellt, welche bis ")
                                append(homework.dueTo.let { date ->
                                    (LocalDate.now() untilRelativeText date) ?: date.format(LocalDate.Format {
                                        dayOfWeek(shortDayOfWeekNames)
                                        chars(", ")
                                        day(padding = Padding.ZERO)
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
        logger.i { "Done" }
    }
}
