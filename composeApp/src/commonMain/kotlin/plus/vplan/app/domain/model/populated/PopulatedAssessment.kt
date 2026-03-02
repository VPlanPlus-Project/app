@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.File
import plus.vplan.app.domain.repository.FileRepository

@Immutable
@Stable
sealed class PopulatedAssessment {
    abstract val assessment: Assessment
    abstract val files: List<File>

    data class CloudAssessment(
        override val assessment: Assessment,
        override val files: List<File>,
    ) : PopulatedAssessment()

    data class LocalAssessment(
        override val assessment: Assessment,
        override val files: List<File>,
    ) : PopulatedAssessment()
}

class AssessmentPopulator : KoinComponent {
    private val fileRepository by inject<FileRepository>()

    fun populateMultiple(
        assessments: List<Assessment>,
    ): Flow<List<PopulatedAssessment>> {
        if (assessments.isEmpty()) return flowOf(emptyList())

        // Per-assessment file flows – built once, not rebuilt on every subjectInstances emit
        val fileFlows: List<Flow<List<File>>> = assessments.map { assessment ->
            if (assessment.fileIds.isEmpty()) flowOf(emptyList())
            else combine(assessment.fileIds.map { fileRepository.getById(it, false) }) { arr ->
                arr.filterIsInstance<CacheState.Done<File>>().map { it.data }
            }
        }

        val filesCombined: Flow<List<List<File>>> =
            if (fileFlows.size == 1) fileFlows[0].map { listOf(it) }
            else combine(fileFlows) { it.toList() }

        return filesCombined.map { filesPerAssessment ->
            assessments.mapIndexed { i, assessment ->
                val files = filesPerAssessment[i]
                when (assessment.creator) {
                    is AppEntity.VppId -> PopulatedAssessment.CloudAssessment(
                        assessment = assessment,
                        files = files,
                    )
                    is AppEntity.Profile -> PopulatedAssessment.LocalAssessment(
                        assessment = assessment,
                        files = files,
                    )
                }
            }
        }.distinctUntilChanged()
    }

    fun populateSingle(assessment: Assessment): Flow<PopulatedAssessment> {
        val files =
            if (assessment.fileIds.isEmpty()) flowOf(emptyList())
            else combine(assessment.fileIds.map {
                fileRepository.getById(it, false).filterIsInstance<CacheState.Done<File>>()
                    .map { it.data }
            }) { it.toList() }

        return files.map { files ->
            when (assessment.creator) {
                is AppEntity.VppId -> PopulatedAssessment.CloudAssessment(
                    assessment = assessment,
                    files = files,
                )
                is AppEntity.Profile -> PopulatedAssessment.LocalAssessment(
                    assessment = assessment,
                    files = files,
                )
            }
        }
    }
}