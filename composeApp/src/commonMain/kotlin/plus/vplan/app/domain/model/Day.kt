package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

data class Day(
    val id: String,
    val date: LocalDate,
    val school: Cacheable<School>,
    val week: Cacheable<Week>?,
    val info: String?,
    val dayType: DayType,
    val timetable: List<Cacheable<Lesson>>,
    val substitutionPlan: List<Cacheable<Lesson>>,
    val nextSchoolDay: Cacheable<Day>?
): CachedItem<Day> {
    enum class DayType {
        REGULAR, WEEKEND, HOLIDAY, UNKNOWN
    }

    companion object {
        fun buildId(school: School, date: LocalDate) = "${school.id}/$date"
    }

    override fun getItemId(): String = this.id
    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Day>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true

        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !this.school.isConfigSatisfied(configuration.school, allowLoading)) return false
            if (configuration.week is Week.Fetch && this.week?.isConfigSatisfied(configuration.week, allowLoading) == false) return false
            if (configuration.timetable is Lesson.Fetch && this.timetable.any { !it.isConfigSatisfied(configuration.timetable, allowLoading)}) return false
            if (configuration.substitutionPlan is Lesson.Fetch && this.substitutionPlan.any { !it.isConfigSatisfied(configuration.substitutionPlan, allowLoading)}) return false
            if (configuration.nextSchoolDay is Fetch && this.nextSchoolDay?.isConfigSatisfied(configuration.nextSchoolDay, allowLoading) == false) return false
        }

        return true
    }

    data class Fetch(
        val school: CacheableItemSource.FetchConfiguration<School> = Ignore(),
        val week: CacheableItemSource.FetchConfiguration<Week> = Ignore(),
        val timetable: CacheableItemSource.FetchConfiguration<Lesson> = Ignore(),
        val substitutionPlan: CacheableItemSource.FetchConfiguration<Lesson> = Ignore(),
        val nextSchoolDay: CacheableItemSource.FetchConfiguration<Day> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Day>()
}