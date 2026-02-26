package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.repository.HomeworkRepository

class EditHomeworkSubjectInstanceUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(homework: PopulatedHomework, subjectInstance: SubjectInstance?, profile: Profile.StudentProfile) {
        var group = if (subjectInstance == null) profile.group else null
        var subjectInstance = subjectInstance

        if (subjectInstance != null && subjectInstance.aliases.none { it.provider == AliasProvider.Vpp }) {
            subjectInstance = subjectInstanceRepository.getById(subjectInstance.aliases.first()).first() ?: return
            if (subjectInstance.aliases.none { it.provider == AliasProvider.Vpp }) return
        }

        if (group != null && group.aliases.none { it.provider == AliasProvider.Vpp }) {
            group = groupRepository.getById(group.aliases.first(), forceUpdate = true).first() ?: return
            if (group.aliases.none { it.provider == AliasProvider.Vpp }) return
        }
        homeworkRepository.editHomeworkSubjectInstance(homework, subjectInstance, group, profile)
    }
}