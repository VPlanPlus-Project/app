package plus.vplan.app.core.data.assessment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.database.dao.AssessmentDao
import plus.vplan.app.core.database.model.database.DbAssessment
import plus.vplan.app.core.database.model.database.DbProfileAssessmentIndex
import plus.vplan.app.core.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.utils.Optional
import plus.vplan.app.network.vpp.assessment.AssessmentApi
import plus.vplan.app.network.vpp.assessment.AssessmentPatchRequest
import plus.vplan.app.network.vpp.assessment.AssessmentPostRequest
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import plus.vplan.app.core.model.Optional as ModelOptional

class AssessmentRepositoryImpl(
    private val assessmentDao: AssessmentDao,
    private val assessmentApi: AssessmentApi,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) : AssessmentRepository {

    override fun getAll(): Flow<List<Assessment>> {
        return assessmentDao.getAll().map { items -> items.mapNotNull { it.toModel() } }
    }

    override fun getAllForProfile(profile: Profile): Flow<List<Assessment>> {
        return assessmentDao.getByProfile(profile.id).map { items -> items.mapNotNull { it.toModel() } }
    }

    override fun getById(id: Int): Flow<Assessment?> {
        return assessmentDao.getById(id).map { it?.toModel() }
    }

    override fun getByDate(date: LocalDate): Flow<List<Assessment>> {
        return assessmentDao.getByDate(date).map { items -> items.mapNotNull { it.toModel() } }
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Assessment>> {
        return if (date == null) {
            assessmentDao.getByProfile(profileId).map { items -> items.mapNotNull { it.toModel() } }
        } else {
            assessmentDao.getByProfileAndDate(profileId, date).map { items -> items.mapNotNull { it.toModel() } }
        }
    }

    override suspend fun save(assessment: Assessment) {
        val creator = assessment.creator
        assessmentDao.upsert(
            DbAssessment(
                id = assessment.id,
                subjectInstanceId = assessment.subjectInstance.id,
                date = assessment.date,
                isPublic = assessment.isPublic,
                createdAt = assessment.createdAt.toInstant(TimeZone.currentSystemDefault()),
                createdBy = if (creator is AppEntity.VppId) creator.vppId.id else null,
                createdByProfile = if (creator is AppEntity.Profile) creator.profile.id else null,
                description = assessment.description,
                type = assessment.type.ordinal,
                cachedAt = assessment.cachedAt
            )
        )
    }

    override suspend fun delete(assessment: Assessment) {
        deleteById(assessment.id)
    }

    override suspend fun deleteById(id: Int) {
        assessmentDao.deleteById(listOf(id))
    }

    override suspend fun deleteAssessment(assessment: Assessment, profile: Profile.StudentProfile): Response.Error? {
        val activeVppId = profile.vppId?.asActive()
        if (assessment.id > 0 && activeVppId != null) {
            try {
                assessmentApi.deleteAssessment(activeVppId, assessment.id)
            } catch (e: Exception) {
                return Response.Error.Other(e.message ?: "Unknown error")
            }
        }
        deleteById(assessment.id)
        return null
    }

    override suspend fun sync(schoolApiAccess: VppSchoolAuthentication, subjectInstanceAliases: List<Alias>) {
        try {
            val assessments = assessmentApi.getAssessments(schoolApiAccess, subjectInstanceAliases)
            
            val dbAssessments = mutableListOf<DbAssessment>()
            val fileLinks = mutableListOf<FKAssessmentFile>()

            for (dto in assessments) {
                // Resolve subject instance alias
                val subjectInstanceAlias = Alias(AliasProvider.Vpp, dto.subject.id.toString(), 1)
                val subjectInstance = subjectInstanceRepository.getById(subjectInstanceAlias).first() ?: continue

                dbAssessments.add(
                    DbAssessment(
                        id = dto.id,
                        subjectInstanceId = subjectInstance.id,
                        date = LocalDate.parse(dto.date),
                        isPublic = dto.isPublic,
                        createdAt = Instant.fromEpochSeconds(dto.createdAt),
                        createdBy = dto.createdBy.id,
                        createdByProfile = null,
                        description = dto.description,
                        type = Assessment.Type.valueOf(dto.type).ordinal,
                        cachedAt = Clock.System.now()
                    )
                )

                // Add file links
                for (file in dto.files) {
                    fileLinks.add(FKAssessmentFile(dto.id, file.id))
                }
            }

            if (dbAssessments.isNotEmpty()) {
                assessmentDao.upsert(dbAssessments, fileLinks)
            }
        } catch (e: Exception) {
            // Log error but don't throw - sync should be resilient
        }
    }

    override suspend fun syncById(
        schoolApiAccess: VppSchoolAuthentication, 
        assessmentId: Int, 
        forceReload: Boolean
    ): Boolean {
        try {
            // Check if we already have fresh data (unless forceReload is true)
            if (!forceReload) {
                val existing = assessmentDao.getById(assessmentId).first()
                if (existing != null) {
                    val age = Clock.System.now() - existing.assessment.cachedAt
                    if (age.inWholeMinutes < 5) {
                        return true // Data is fresh enough
                    }
                }
            }
            
            val dto = assessmentApi.getAssessmentById(schoolApiAccess, assessmentId)
                ?: return false // Assessment not found on server
            
            // Resolve subject instance alias
            val subjectInstanceAlias = Alias(AliasProvider.Vpp, dto.subject.id.toString(), 1)
            val subjectInstance = subjectInstanceRepository.getById(subjectInstanceAlias).first()
                ?: return false // Subject instance not found
            
            val dbAssessment = DbAssessment(
                id = dto.id,
                subjectInstanceId = subjectInstance.id,
                date = LocalDate.parse(dto.date),
                isPublic = dto.isPublic,
                createdAt = Instant.fromEpochSeconds(dto.createdAt),
                createdBy = dto.createdBy.id,
                createdByProfile = null,
                description = dto.description,
                type = Assessment.Type.valueOf(dto.type).ordinal,
                cachedAt = Clock.System.now()
            )
            
            val fileLinks = dto.files.map { FKAssessmentFile(dto.id, it.id) }
            
            assessmentDao.upsert(listOf(dbAssessment), fileLinks)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun getIdForNewLocalAssessment(): Int {
        return (assessmentDao.getSmallestId() ?: 0) - 1
    }

    override suspend fun updateAssessmentMetadata(
        assessment: Assessment,
        date: ModelOptional<LocalDate>,
        type: ModelOptional<Assessment.Type>,
        isPublic: ModelOptional<Boolean>,
        content: ModelOptional<String>,
        profile: Profile.StudentProfile
    ) {
        val activeVppId = profile.vppId?.asActive()
        
        // Send API request if it's a cloud assessment and at least one field is being updated
        if (assessment.id > 0 && activeVppId != null) {
            if (date.isPresent() || type.isPresent() || isPublic.isPresent() || content.isPresent()) {
                try {
                    assessmentApi.updateAssessment(
                        activeVppId,
                        assessment.id,
                        AssessmentPatchRequest(
                            type = if (type is ModelOptional.Present) Optional.Defined(type.value.name) else Optional.Undefined(),
                            date = if (date is ModelOptional.Present) Optional.Defined(date.value.toString()) else Optional.Undefined(),
                            isPublic = if (isPublic is ModelOptional.Present) Optional.Defined(isPublic.value) else Optional.Undefined(),
                            content = if (content is ModelOptional.Present) Optional.Defined(content.value) else Optional.Undefined(),
                        )
                    )
                } catch (e: Exception) {
                    // Revert local changes on error
                    return
                }
            }
        }

        // Update local database
        if (date is ModelOptional.Present) {
            assessmentDao.updateDate(assessment.id, date.value)
        }
        if (type is ModelOptional.Present) {
            assessmentDao.updateType(assessment.id, type.value.ordinal)
        }
        if (isPublic is ModelOptional.Present) {
            assessmentDao.updateVisibility(assessment.id, isPublic.value)
        }
        if (content is ModelOptional.Present) {
            assessmentDao.updateContent(assessment.id, content.value)
        }
    }

    override suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        subjectInstanceId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int> {
        return try {
            val response = assessmentApi.createAssessment(
                vppId,
                AssessmentPostRequest(
                    subjectInstanceId = subjectInstanceId,
                    date = date.toString(),
                    isPublic = isPublic,
                    content = content,
                    type = type.name
                )
            )
            Response.Success(response.id)
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun linkFile(vppId: VppId.Active?, assessmentId: Int, fileId: Int): Response<Unit> {
        return try {
            if (assessmentId > 0 && vppId != null) {
                assessmentApi.linkFile(vppId, assessmentId, fileId)
            }
            assessmentDao.upsert(FKAssessmentFile(assessmentId, fileId))
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun unlinkFile(vppId: VppId.Active?, assessmentId: Int, fileId: Int): Response<Unit> {
        return try {
            if (assessmentId > 0 && vppId != null) {
                assessmentApi.unlinkFile(vppId, assessmentId, fileId)
            }
            assessmentDao.deleteFileAssessmentConnections(assessmentId, fileId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache() {
        assessmentDao.clearCache()
    }

    override suspend fun dropIndexForProfile(profileId: Uuid) {
        assessmentDao.dropAssessmentsIndexForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, assessmentIds: Collection<Int>) {
        assessmentDao.upsertAssessmentsIndex(
            assessmentIds.map { DbProfileAssessmentIndex(it, profileId) }
        )
    }

    private fun VppId.asActive(): VppId.Active? = this as? VppId.Active
}
