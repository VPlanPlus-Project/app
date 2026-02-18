package plus.vplan.app.domain.model.populated

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository

data class PopulatedHomework(
    val homework: Homework,
    val tasks: List<Homework.HomeworkTask>,
    val files: List<File>,
    val subjectInstance: SubjectInstance?
)

class HomeworkPopulator: KoinComponent {
    private val homeworkRepository by inject<HomeworkRepository>()
    private val fileRepository by inject<FileRepository>()
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()

    fun populateSingle(homework: Homework): Flow<PopulatedHomework> {
        val subjectInstance = homework.subjectInstanceId?.let {
            subjectInstanceRepository.findByAlias(Alias(AliasProvider.Vpp, it.toString(), 1), false, true)
                .filterIsInstance<AliasState.Done<SubjectInstance>>()
                .map { it.data }
        } ?: flowOf(null)

        val tasks = combine(
            homework.taskIds.map { taskId ->
                homeworkRepository.getTaskById(taskId)
                    .filterIsInstance<CacheState.Done<Homework.HomeworkTask>>()
                    .map { it.data }
            }
        ) { it.toList() }

        val files = combine(
            homework.fileIds.map { fileId ->
                fileRepository.getById(fileId, false)
                    .filterIsInstance<CacheState.Done<File>>()
                    .map { it.data }
            }
        ) { it.toList() }

        return combine(
            tasks,
            files,
            subjectInstance
        ) { tasks, files, subjectInstance ->
            PopulatedHomework(
                homework = homework,
                tasks = tasks,
                files = files,
                subjectInstance = subjectInstance
            )
        }
    }
}