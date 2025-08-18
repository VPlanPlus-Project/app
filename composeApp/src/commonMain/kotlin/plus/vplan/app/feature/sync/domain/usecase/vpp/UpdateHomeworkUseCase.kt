package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.capture
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.getByProvider
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase
) {
    private val logger = Logger.withTag("UpdateHomeworkUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val ids = mutableSetOf<Int>()
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
            val vppSchoolId = school?.aliases?.getByProvider(AliasProvider.Vpp)?.value?.toInt() ?: run {
                logger.e { "No vpp provider for school $school" }
                return@forEachProfile
            }
            val enabledSubjectInstanceIds = studentProfile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds
                .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                .flatMap { it.aliases }

            val downloaded = homeworkRepository.download(school.buildSp24AppAuthentication(), group.aliases.toList(), subjectInstanceAliases)

            downloaded.hashCode()

            TODO()
//            ids.addAll(
//                (homeworkRepository.download(
//                    schoolApiAccess = studentProfile.getVppIdItem()?.buildVppSchoolAuthentication(vppSchoolId) ?: studentProfile.getSchool().getFirstValue()!!.getVppSchoolAuthentication()!!,
//                    groupId = studentProfile.groupId,
//                    subjectInstanceIds = studentProfile.subjectInstanceConfiguration.map { it.key },
//                ) as? Response.Success)?.data.orEmpty().also {
//                    if (allowNotifications) {
//                        val newHomework = combine((it - existingHomework).ifEmpty { return@also }.map { id -> homeworkRepository.getById(id, false).filterIsInstance<CacheStateOld.Done<Homework>>().map { homework -> homework.data } }) { list -> list.toList() }.first()
//                            .filterIsInstance<Homework.CloudHomework>()
//                            .filter { homework -> homework.createdBy != studentProfile.vppIdId && (homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) <= 2.days }
//                            .filter { it.subjectInstanceId == null || it.subjectInstanceId in studentProfile.subjectInstanceConfiguration.filterValues { it }.keys }
//
//                        if (newHomework.size == 1) {
//                            platformNotificationRepository.sendNotification(
//                                title = "Neue Hausaufgabe",
//                                category = studentProfile.name,
//                                message = buildString {
//                                    newHomework.first().let { homework ->
//                                        append(homework.getCreatedBy().name)
//                                        append(" hat eine neue Hausaufgabe ")
//                                        if (homework.subjectInstance == null) append("für Klasse ${homework.group?.getFirstValue()?.name}")
//                                        else append("für ${homework.subjectInstance?.getFirstValueOld()?.subject}")
//                                        append(" erstellt, welche bis ")
//                                        append(homework.dueTo.let { date ->
//                                            (LocalDate.now() untilRelativeText date) ?: date.format(LocalDate.Format {
//                                                dayOfWeek(shortDayOfWeekNames)
//                                                chars(", ")
//                                                dayOfMonth()
//                                                chars(". ")
//                                                monthName(shortMonthNames)
//                                            })
//                                        })
//                                        append(" fällig ist.")
//                                    }
//                                },
//                                onClickData = Json.encodeToString(
//                                    StartTaskJson(
//                                        type = "open",
//                                        profileId = studentProfile.id.toString(),
//                                        value = Json.encodeToString(
//                                            StartTaskJson.StartTaskOpen(
//                                                type = "homework",
//                                                value = Json.encodeToString(
//                                                    StartTaskJson.StartTaskOpen.Homework(
//                                                        homeworkId = newHomework.first().id
//                                                    )
//                                                )
//                                            )
//                                        )
//                                    ).also { Logger.d { "Task: $it" } }
//                                )
//                            )
//                        }
//                        if (newHomework.size > 1) {
//                            platformNotificationRepository.sendNotification(
//                                title = "Neue Hausaufgaben",
//                                category = studentProfile.name,
//                                message = buildString {
//                                    append("Es gibt ${newHomework.size} neue Hausaufgaben für dich")
//                                },
//                                isLarge = false,
//                            )
//                        }
//                    }
//                }
//            )
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

        profiles.forEach {
            updateProfileHomeworkIndexUseCase(it)
        }
    }
}