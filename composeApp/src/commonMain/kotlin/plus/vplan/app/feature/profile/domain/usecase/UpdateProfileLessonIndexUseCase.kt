@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.ExperimentalUuidApi

class UpdateProfileLessonIndexUseCase(
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(profile: Profile, forceVersion: String? = null) {
        val school = profile.getSchool().getFirstValue()!!

        substitutionPlanRepository.getSubstitutionPlanBySchool(school.id, forceVersion).first().let {
            val version = keyValueRepository.get(Keys.substitutionPlanVersion(school.id)).first()?.toIntOrNull() ?: -1
            substitutionPlanRepository.dropCacheForProfile(profile.id, "${school.id}_$version")
            substitutionPlanRepository.createCacheForProfile(profile.id, it.filter { lesson -> lesson.isRelevantForProfile(profile) }.map { it.id })
        }

        App.timetableSource.getBySchool(school.id).first()
            .filterIsInstance<CacheState.Done<Lesson.TimetableLesson>>()
            .map { it.data }
            .filter { it.isRelevantForProfile(profile) }
            .let {
                timetableRepository.dropCacheForProfile(profile.id)
                timetableRepository.createCacheForProfile(profile.id, it.map { lesson -> lesson.id })
            }
    }
}