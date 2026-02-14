package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSyncCoordinator
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleYearsRepository
import plus.vplan.app.domain.repository.besteschule.SyncResult
import kotlin.time.Clock

class BesteSchuleSyncCoordinatorImpl: BesteSchuleSyncCoordinator, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()
    private val besteSchuleApiRepository by inject<BesteSchuleApiRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()
    private val besteSchuleTeachersRepository by inject<BesteSchuleTeachersRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()
    private val besteSchuleCollectionsRepository by inject<BesteSchuleCollectionsRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleYearsRepository by inject<BesteSchuleYearsRepository>()

    override suspend fun syncYears(schulverwalterUserId: Int) {
        val user =
            vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(schulverwalterUserId)
                .first() ?: throw BesteSchuleSyncCoordinator.BesteSchuleUserDoesNotExistException()

        val years = besteSchuleYearsRepository.getYearsFromApi(user.schulverwalterAccessToken)
        if (years !is Response.Success) throw RuntimeException("Failed to get years: $years")

        val cachedModels = years.data.map {
            BesteSchuleYear(
                id = it.id,
                name = it.name,
                from = LocalDate.parse(it.from),
                to = LocalDate.parse(it.to),
                cachedAt = Clock.System.now(),
                intervalIds = emptySet(),
            )
        }.toSet()

        besteSchuleYearsRepository.addYearsToCache(cachedModels)
    }

    override suspend fun syncBesteSchule(schulverwalterUserId: Int, yearId: Int): SyncResult {
        val user =
            vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(schulverwalterUserId)
                .first() ?: throw BesteSchuleSyncCoordinator.BesteSchuleUserDoesNotExistException()

        val yearResponse = besteSchuleApiRepository.setYearForUser(user.schulverwalterAccessToken, yearId)
        if (yearResponse is Response.Error) throw RuntimeException("Failed to set year to $yearId: $yearResponse")

        val studentData = besteSchuleApiRepository.getStudentData(user.schulverwalterAccessToken, false)
        if (studentData !is Response.Success) throw RuntimeException("Failed to get student data: $studentData")

        val studentGrades = besteSchuleApiRepository.getStudentGradeData(user.schulverwalterAccessToken)
        if (studentGrades !is Response.Success) throw RuntimeException("Failed to get student grade data: $studentGrades")

        val subjects = studentData.data.subjects.map { apiSubject ->
            BesteSchuleSubject(
                id = apiSubject.id,
                shortName = apiSubject.shortName,
                fullName = apiSubject.fullName,
                cachedAt = Clock.System.now()
            )
        }.toSet()
        besteSchuleSubjectsRepository.addSubjectsToCache(subjects)

        val teachers = studentGrades.data
            .map { grade -> grade.teacher }
            .distinctBy { teacher -> teacher.id }
            .map { apiTeacher ->
                BesteSchuleTeacher(
                    id = apiTeacher.id,
                    localId = apiTeacher.shortName,
                    forename = apiTeacher.forename,
                    surname = apiTeacher.lastname,
                    cachedAt = Clock.System.now()
                )
            }
        besteSchuleTeachersRepository.addTeachersToCache(teachers)

        val intervals = studentData.data.intervals.map { api ->
            BesteSchuleInterval(
                id = api.id,
                name = api.name,
                type = BesteSchuleInterval.Type.fromString(api.type),
                from = LocalDate.parse(api.from),
                to = LocalDate.parse(api.to),
                includedIntervalId = api.includedIntervalId,
                yearId = api.yearId,
                linkedToSchulverwalterAccountIds = setOf(schulverwalterUserId),
                cachedAt = Clock.System.now()
            )
        }.toSet()
        besteSchuleIntervalsRepository.addIntervalsToCache(intervals)

        val collections = studentGrades.data
            .map { grade -> grade.collection }
            .distinctBy { collection -> collection.id }
            .map { api ->
                BesteSchuleCollection(
                    id = api.id,
                    name = api.name,
                    type = api.type,
                    subjectId = api.subjectId,
                    givenAt = LocalDate.parse(api.givenAt),
                    intervalId = api.intervalId,
                    teacherId = api.teacherId,
                    cachedAt = Clock.System.now()
                )
            }.toSet()
        besteSchuleCollectionsRepository.addCollectionsToCache(collections)

        val existingGrades = besteSchuleGradesRepository.getGradesFromCache(userId = schulverwalterUserId).first()
        val grades = studentGrades.data.map { apiGrade ->
            BesteSchuleGrade(
                id = apiGrade.id,
                value = apiGrade.cleanedValue,
                isOptional = apiGrade.isOptional,
                isSelectedForFinalGrade = existingGrades.find { it.id == apiGrade.id }?.isSelectedForFinalGrade ?: true,
                schulverwalterUserId = schulverwalterUserId,
                collectionId = apiGrade.collection.id,
                givenAt = LocalDate.parse(apiGrade.givenAt),
                cachedAt = Clock.System.now()
            )
        }
        besteSchuleGradesRepository.addGradesToCache(grades)

        return SyncResult(
            newGrades = grades.filter { grade -> grade.id !in existingGrades.map { it.id } }
        )
    }
}