package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.Uuid

class CreateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(
        text: String,
        isPublic: Boolean?,
        date: LocalDate,
        defaultLesson: DefaultLesson,
        type: Assessment.Type,
        selectedFiles: List<AttachedFile>
    ): Boolean {
        val profile = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().first().let { App.profileSource.getById(Uuid.parseHex(it)).getFirstValue() as? Profile.StudentProfile } ?: return false
        val id: Int

        val assessment: Assessment
//        val files: List<Homework.HomeworkFile>
        if (profile.getVppIdItem() is VppId.Active) {
            val result = assessmentRepository.createAssessmentOnline(
                vppId = profile.getVppIdItem() as VppId.Active,
                date = date,
                type = type,
                defaultLessonId = defaultLesson.id,
                isPublic = isPublic ?: false,
                content = text
            )
            if (result !is Response.Success) return false
            assessment = Assessment(
                id = result.data,
                creator = AppEntity.VppId(profile.getVppIdItem()!!.id),
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                date = date,
                isPublic = isPublic ?: false,
                defaultLessonId = defaultLesson.id,
                description = text,
                type = type
            )
        } else {
            id = assessmentRepository.getIdForNewLocalAssessment() - 1
            assessment = Assessment(
                id = id,
                creator = AppEntity.Profile(profile.id),
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                date = date,
                isPublic = false,
                defaultLessonId = defaultLesson.id,
                description = text,
                type = type
            )
        }

        assessmentRepository.upsert(listOf(assessment))
        return true
    }
}