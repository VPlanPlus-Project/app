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
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.domain.model.populated.AssessmentPopulator
import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileAssessmentIndexUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class UpdateAssessmentsUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateProfileAssessmentIndexUseCase: UpdateProfileAssessmentIndexUseCase,
    private val vppIdRepository: VppIdRepository,
    private val assessmentPopulator: AssessmentPopulator
) {
    private val logger = Logger.withTag("UpdateAssessmentUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val downloadedIds = mutableSetOf<Int>()
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        val existingIds = mutableSetOf<Int>()

        profiles.forEach forEachProfile@{ studentProfile ->
            val existingAssessmentIds = assessmentRepository.getAll().first().map { it.id }.toSet()
            existingIds.addAll(existingAssessmentIds)

            studentProfile.school.aliases.getByProvider(AliasProvider.Vpp)?.value?.toInt() ?: run {
                logger.e { "No vpp provider for school ${studentProfile.school}" }
                return@forEachProfile
            }

            val enabledSubjectInstanceIds = studentProfile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds
                .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                .flatMap { it.aliases }

            logger.d { "Downloading assessments for ${studentProfile.name}" }
            val downloaded = assessmentRepository.download(
                studentProfile.vppId?.buildVppSchoolAuthentication() ?: studentProfile.school.buildSp24AppAuthentication(),
                subjectInstanceAliases
            )

            if (downloaded !is Response.Success) {
                logger.e { "Failed to download assessments for profile ${studentProfile.name} (${studentProfile.id}): $downloaded" }
                return@forEachProfile
            }

            val missingSubjectInstances = downloaded.data
                .mapNotNull { it.subject.id }
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

                if (response !is Response.Success) {
                    logger.e { "Failed to download subject instance $vppSubjectInstanceId: $response" }
                    return@forEach
                }

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

            downloaded.data.forEach { assessmentDto ->
                assessmentRepository.upsertLocally(
                    assessmentId = assessmentDto.id,
                    subjectInstanceId = subjectInstanceRepository.getByAlias(Alias(
                        provider = AliasProvider.Vpp,
                        value = assessmentDto.subject.id.toString(),
                        version = 1
                    )).first()?.id ?: return@forEach,
                    date = LocalDate.parse(assessmentDto.date),
                    isPublic = assessmentDto.isPublic,
                    createdAt = Instant.fromEpochSeconds(assessmentDto.createdAt),
                    createdBy = assessmentDto.createdBy.id,
                    createdByProfile = null,
                    description = assessmentDto.description,
                    type = Assessment.Type.valueOf(assessmentDto.type),
                    associatedFileIds = assessmentDto.files.map { it.id }
                )
            }

            downloadedIds.addAll(downloaded.data.map { it.id })
        }

        (existingIds.filter { it > 0 } - downloadedIds).forEach { deletionCandidate ->
            logger.d { "Testing deletionCandidate (ID: $deletionCandidate)" }
            val item = assessmentRepository.getById(deletionCandidate, true).filter { it !is CacheState.Loading }.first()
            if (item is CacheState.NotExisting) {
                assessmentRepository.deleteById(deletionCandidate)
                logger.d { "Deleted $deletionCandidate" }
            }
        }

        profiles.forEach { studentProfile ->
            logger.d { "Updating index for ${studentProfile.name}" }
            updateProfileAssessmentIndexUseCase(studentProfile)

            logger.d { "Checking if notifications should be sent" }
            run buildAndSendNotifications@{
                if (allowNotifications) {
                    logger.d { "Checking if new assessments are created" }
                    val allowedSubjectInstanceIds = studentProfile.subjectInstanceConfiguration
                        .filterValues { it }
                        .keys

                    val newAssessments = combine(
                        (downloadedIds - existingIds).ifEmpty { return@buildAndSendNotifications }
                            .map { id ->
                                assessmentRepository.getById(id, false)
                                    .filterIsInstance<CacheState.Done<Assessment>>()
                                    .map { assessment -> assessment.data }
                            })
                    { list ->
                        assessmentPopulator.populateMultiple(list.toList(), PopulationContext.Profile(studentProfile)).first()
                    }.first()
                        .filter { assessment ->
                            assessment is PopulatedAssessment.CloudAssessment && assessment.createdByUser.id != studentProfile.vppId?.id && (assessment.assessment.createdAt until Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())) < 2.days
                        }
                        .filter { assessment -> allowedSubjectInstanceIds.contains(assessment.subjectInstance.id) }

                    if (newAssessments.isEmpty()) return@buildAndSendNotifications

                    if (newAssessments.size == 1) {
                        val message = buildString {
                            newAssessments.first().let { assessment ->
                                append((assessment as PopulatedAssessment.CloudAssessment).createdByUser.name)
                                append(" hat eine neue Leistungserhebung in ")
                                append(assessment.subjectInstance.subject ?: "wined Fach")
                                append(" für ")
                                (assessment.assessment.date untilRelativeText LocalDate.now())?.let { append(it) } ?: append(assessment.assessment.date.format(LocalDate.Format {
                                    dayOfWeek(shortDayOfWeekNames)
                                    chars(", ")
                                    day(padding = Padding.ZERO)
                                    chars(". ")
                                    monthName(shortMonthNames)
                                }))
                                append(" erstellt.")
                            }
                        }
                        platformNotificationRepository.sendNotification(
                            title = "Neue Leistungserhebung",
                            category = studentProfile.name,
                            message = message,
                            largeText = "$message\n${newAssessments.first().assessment.description}",
                            isLarge = true,
                            onClickData = Json.encodeToString(
                                StartTaskJson(
                                    type = "open",
                                    profileId = studentProfile.id.toString(),
                                    value = Json.encodeToString(
                                        StartTaskJson.StartTaskOpen(
                                            type = "assessment",
                                            value = Json.encodeToString(
                                                StartTaskJson.StartTaskOpen.Assessment(
                                                    assessmentId = newAssessments.first().assessment.id
                                                )
                                            )
                                        )
                                    )
                                ).also { Logger.d { "Task: $it" } }
                            )
                        )
                    } else {
                        platformNotificationRepository.sendNotification(
                            title = "Neue Leistungserhebungen",
                            category = studentProfile.name,
                            message = buildString {
                                append("Es gibt ${newAssessments.size} neue Leistungserhebungen für dich")
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
