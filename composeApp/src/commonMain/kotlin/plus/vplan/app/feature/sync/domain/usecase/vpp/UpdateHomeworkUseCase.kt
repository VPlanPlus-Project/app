@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.StartTaskJson
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.getByProvider
import plus.vplan.app.core.model.CreationReason
import plus.vplan.app.core.model.Homework
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkEntity
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateProfileHomeworkIndexUseCase: UpdateProfileHomeworkIndexUseCase,
    private val vppIdRepository: VppIdRepository,
    private val groupRepository: GroupRepository,
    private val schoolRepository: SchoolRepository,
    private val homeworkPopulator: HomeworkPopulator,
) {
    private val logger = Logger.withTag("UpdateHomeworkUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val downloadedIds = mutableSetOf<Int>()
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        val existingIds = mutableSetOf<Int>()
        profiles.forEach forEachProfile@{ studentProfile ->
            val existingHomeworkIds = homeworkRepository.getByGroup(studentProfile.group).first().filterIsInstance<Homework.CloudHomework>().map { it.id }.toSet()
            existingIds.addAll(existingHomeworkIds)

            // require vpp provider for school
            studentProfile.school.aliases.getByProvider(AliasProvider.Vpp)?.value?.toInt() ?: run {
                logger.e { "No vpp provider for school ${studentProfile.school}" }
                return@forEachProfile
            }

            val enabledSubjectInstanceIds = studentProfile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds
                .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                .flatMap { it.aliases }

            logger.d { "Downloading homework for ${studentProfile.group.name}" }
            val downloaded = homeworkRepository.download(studentProfile.school.buildSp24AppAuthentication(), studentProfile.group.aliases.toList(), subjectInstanceAliases)

            if (downloaded !is Response.Success) {
                logger.e { "Failed to download homework for profile ${studentProfile.name} (${studentProfile.id}): $downloaded" }
                return@forEachProfile
            }

            val missingGroups = downloaded.data
                .mapNotNull { it.group?.id }
                .toSet()
                .filter { groupId ->
                    groupRepository.getByAlias(
                    setOf(
                        Alias(
                            provider = AliasProvider.Vpp,
                            value = groupId.toString(),
                            version = 1
                        )
                    )).first() == null
                }

            missingGroups.forEach { vppGroupId ->
                val vppIdAlias = Alias(
                    provider = AliasProvider.Vpp,
                    value = vppGroupId.toString(),
                    version = 1
                )
                val response = groupRepository.downloadByAlias(vppIdAlias)

                if (response !is Response.Success) throw IllegalStateException("Failed to download group $vppGroupId: $response")

                val sp24Alias = response.data.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@forEach

                val item = groupRepository.getByAlias(setOf(sp24Alias)).first()

                groupRepository.upsert(GroupDbDto(
                    id = item?.id,
                    schoolId = schoolRepository.resolveAliasToLocalId(Alias(
                        provider = AliasProvider.Vpp,
                        value = response.data.schoolId.toString(),
                        version = 1
                    ))!!,
                    name = item?.name ?: response.data.name,
                    aliases = (item?.aliases ?: response.data.aliases) + vppIdAlias,
                    creationReason = if (item == null) CreationReason.Cached else CreationReason.Persisted
                ))
            }

            val missingSubjectInstances = downloaded.data
                .mapNotNull { it.subjectInstance?.id }
                .toSet()
                .filter { subjectInstanceId ->
                    subjectInstanceRepository.getByAlias(Alias(
                        provider = AliasProvider.Vpp,
                        value = subjectInstanceId.toString(),
                        version = 1
                    )).first() == null
                }
            logger.d { "Missing subject instances: ${missingSubjectInstances.size}: $missingSubjectInstances" }

            missingSubjectInstances.forEach { vppSubjectInstanceId ->
                val vppAlias = Alias(
                    provider = AliasProvider.Vpp,
                    value = vppSubjectInstanceId.toString(),
                    version = 1
                )
                val response = subjectInstanceRepository.downloadByAlias(
                    vppAlias,
                    studentProfile.school.buildSp24AppAuthentication()
                )

                if (response !is Response.Success) throw IllegalStateException("Failed to download subject instance $vppSubjectInstanceId: $response")

                val sp24Alias = response.data.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@forEach
                val item = subjectInstanceRepository.getByAlias(setOf(sp24Alias)).first() ?: return@forEach

                subjectInstanceRepository.upsert(
                    SubjectInstanceDbDto(
                        id = item.id,
                        subject = item.subject,
                        course = item.courseId,
                        teacher = item.teacherId,
                        groups = item.groupIds,
                        aliases = item.aliases.toList() + vppAlias
                    )
                )
            }

            val missingVppIds = downloaded.data
                .map { it.createdBy.id }
                .filter { vppIdRepository.getByLocalId(it).first() == null }
            logger.d { "Missing vpp ids: ${missingVppIds.size}: $missingVppIds" }

            missingVppIds.forEach { vppId ->
                vppIdRepository.getById(vppId, ResponsePreference.Fresh).first { it !is CacheState.Loading }
            }


            downloaded.data.forEach { homeworkDto ->
                homeworkRepository.upsert(HomeworkEntity(
                    id = homeworkDto.id,
                    groupId = homeworkDto.group?.id?.let { groupRepository.getByAlias(setOf(Alias(
                        provider = AliasProvider.Vpp,
                        value = homeworkDto.group.id.toString(),
                        version = 1
                    ))).first()?.id },
                    createdAt = Instant.fromEpochSeconds(homeworkDto.createdAt),
                    subjectInstanceId = homeworkDto.subjectInstance?.id?.let {
                        subjectInstanceRepository.getByAlias(Alias(
                            provider = AliasProvider.Vpp,
                            value = homeworkDto.subjectInstance.id.toString(),
                            version = 1
                        )).first()?.id
                    },
                    dueTo = LocalDate.parse(homeworkDto.dueTo),
                    createdByVppId = homeworkDto.createdBy.id,
                    createdByProfileId = null,
                    isPublic = homeworkDto.isPublic,
                    cachedAt = Clock.System.now(),
                    tasks = homeworkDto.tasks.map { homeworkTaskDtoWrapper ->
                        HomeworkEntity.TaskEntity(
                            id = homeworkTaskDtoWrapper.value.id,
                            homeworkId = homeworkDto.id,
                            createdAt = Instant.fromEpochSeconds(homeworkDto.createdAt),
                            content = homeworkTaskDtoWrapper.value.content,
                            cachedAt = Clock.System.now()
                        )
                    }
                ))
            }

            downloadedIds.addAll(downloaded.data.map { it.id })
        }

        (existingIds.filter { it > 0 } - downloadedIds).forEach { deletionCandidate ->
            logger.d { "Testing deletionCandidate (ID: $deletionCandidate)" }
            val item = homeworkRepository.getById(deletionCandidate, true).filter { it !is CacheState.Loading }.first()
            if (item is CacheState.NotExisting) {
                homeworkRepository.deleteById(deletionCandidate)
                logger.d { "Deleted $deletionCandidate" }
            }
        }

        profiles.forEach { studentProfile ->
            logger.d { "Updating index for ${studentProfile.name}" }
            updateProfileHomeworkIndexUseCase(studentProfile)

            logger.d { "Checking if notifications should be sent" }
            run buildAndSendNotifications@{
                if (allowNotifications) {
                    logger.d { "Checking if new homework is created" }
                    val allowedSubjectInstanceVppIds = studentProfile.subjectInstanceConfiguration
                        .filterValues { it }
                        .keys

                    val newHomework = combine((downloadedIds - existingIds).ifEmpty { return@buildAndSendNotifications }
                        .map { id -> homeworkRepository.getById(id, false).filterIsInstance<CacheState.Done<Homework>>().map { homework -> homework.data } }) { list -> list.toList() }.first()
                        .filterIsInstance<Homework.CloudHomework>()
                        .filter { homework ->
                            homework.createdById != studentProfile.vppId?.id && (homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())) <= 2.days
                        }
                        .filter { it.subjectInstanceId == null || it.subjectInstanceId in allowedSubjectInstanceVppIds }

                    if (newHomework.size == 1) {
                        platformNotificationRepository.sendNotification(
                            title = "Neue Hausaufgabe",
                            category = studentProfile.name,
                            message = buildString {
                                newHomework.first().let { homework ->
                                    val homework = homeworkPopulator.populateSingle(homework).first() as PopulatedHomework.CloudHomework
                                    append(homework.createdByUser.name)
                                    append(" hat eine neue Hausaufgabe ")
                                    if (homework.subjectInstance == null) append("für Klasse ${homework.group?.name}")
                                    else append("für ${homework.subjectInstance.subject}")
                                    append(" erstellt, welche bis ")
                                    append(homework.homework.dueTo.let { date ->
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
        }
        logger.i { "Done" }
    }
}