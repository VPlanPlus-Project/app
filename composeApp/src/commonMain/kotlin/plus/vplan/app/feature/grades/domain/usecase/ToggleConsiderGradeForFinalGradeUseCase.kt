package plus.vplan.app.feature.grades.domain.usecase

import plus.vplan.app.domain.repository.schulverwalter.GradeRepository

class ToggleConsiderGradeForFinalGradeUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(gradeId: Int, useForFinalGrade: Boolean) {
        gradeRepository.setConsiderForFinalGrade(gradeId, useForFinalGrade)
    }
}