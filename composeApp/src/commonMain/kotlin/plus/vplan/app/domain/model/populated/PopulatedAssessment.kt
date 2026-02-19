package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.VppIdRepository

@Immutable
@Stable
sealed class PopulatedAssessment {
    abstract val assessment: Assessment
    abstract val subjectInstance: SubjectInstance
    abstract val files: List<File>
    abstract val createdBy: AppEntity

    data class CloudAssessment(
        override val assessment: Assessment,
        override val subjectInstance: SubjectInstance,
        override val files: List<File>,
        override val createdBy: AppEntity.VppId,
        val createdByUser: VppId,
    ) : PopulatedAssessment()

    data class LocalAssessment(
        override val assessment: Assessment,
        override val subjectInstance: SubjectInstance,
        override val files: List<File>,
        override val createdBy: AppEntity.Profile,
        val createdByProfile: Profile.StudentProfile
    ) : PopulatedAssessment()
}

class AssessmentPopulator : KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()
    private val profileRepository by inject<ProfileRepository>()
    private val vppIdRepository by inject<VppIdRepository>()
    private val fileRepository by inject<FileRepository>()

    fun populateSingle(assessment: Assessment): Flow<PopulatedAssessment> {
        val subjectInstance = subjectInstanceRepository.getByLocalId(assessment.subjectInstanceId)
        val profile =
            (assessment.creator as? AppEntity.Profile)?.id?.let { profileRepository.getById(it) }
                ?: flowOf(null)

        val vppId =
            (assessment.creator as? AppEntity.VppId)?.id?.let { vppIdRepository.getByLocalId(it) }
                ?: flowOf(null)

        val files =
            if (assessment.fileIds.isEmpty()) flowOf(emptyList())
            else combine(assessment.fileIds.map {
                fileRepository.getById(it, false).filterIsInstance<CacheState.Done<File>>()
                    .map { it.data }
            }) { it.toList() }

        return combine(
            subjectInstance,
            profile,
            vppId,
            files,
        ) { subjectInstance, profile, vppId, files ->
            when (assessment.creator) {
                is AppEntity.VppId -> PopulatedAssessment.CloudAssessment(
                    assessment = assessment,
                    subjectInstance = subjectInstance!!,
                    files = files,
                    createdBy = AppEntity.VppId(assessment.creator.id),
                    createdByUser = vppId!!
                )
                is AppEntity.Profile -> PopulatedAssessment.LocalAssessment(
                    assessment = assessment,
                    subjectInstance = subjectInstance!!,
                    files = files,
                    createdBy = AppEntity.Profile(assessment.creator.id),
                    createdByProfile = profile as Profile.StudentProfile
                )
            }
        }
    }
}