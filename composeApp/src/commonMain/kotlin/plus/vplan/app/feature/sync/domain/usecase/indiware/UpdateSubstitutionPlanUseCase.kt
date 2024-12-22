package plus.vplan.app.feature.sync.domain.usecase.indiware

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.utils.latest

class UpdateSubstitutionPlanUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository
) {

    suspend operator fun invoke(indiwareSchool: School.IndiwareSchool, date: LocalDate): Response.Error? {

        val teachers = teacherRepository.getBySchool(indiwareSchool.id).latest()
        val rooms = roomRepository.getBySchool(indiwareSchool.id).latest()

        val substitutionPlanResponse = indiwareRepository.getSubstitutionPlan(
            sp24Id = indiwareSchool.sp24Id,
            username = indiwareSchool.username,
            password = indiwareSchool.password,
            date = date,
            teacherNames = teachers.map { it.name },
            roomNames = rooms.map { it.name }
        )

        if (substitutionPlanResponse is Response.Error) return substitutionPlanResponse
        if (substitutionPlanResponse !is Response.Success) throw IllegalStateException("substitutionPlanResponse is not successful: $substitutionPlanResponse")

        val substitutionPlan = substitutionPlanResponse.data

        // TODO: Implement

        return null
    }
}