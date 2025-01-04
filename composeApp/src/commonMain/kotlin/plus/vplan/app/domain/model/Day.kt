package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

data class Day(
    val id: String,
    val date: LocalDate,
    val school: Cacheable<School>,
    val week: Cacheable<Week>,
    val info: String?
): CachedItem<Day> {
    constructor(
        date: LocalDate,
        school: School,
        week: Week,
        info: String?
    ) : this("${school.id}/$date", date, Cacheable.Loaded(school), Cacheable.Loaded(week), info)

    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Day>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !school.isConfigSatisfied(configuration.school, allowLoading)) return false
            if (configuration.week is Week.Fetch && !week.isConfigSatisfied(configuration.week, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val school: CacheableItemSource.FetchConfiguration<School> = Ignore(),
        val week: CacheableItemSource.FetchConfiguration<Week> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Day>()
}

sealed interface SchoolDay{
    val id: String
    val date: LocalDate
    val school: School?
    val nextRegularSchoolDay: LocalDate?

    data class NormalDay(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?,
        val week: Week,
        val info: String?,
        val lessons: List<Lesson>
    ) : SchoolDay

    data class Unknown(
        override val date: LocalDate,
    ) : SchoolDay {
        override val id: String = "no_id"
        override val nextRegularSchoolDay: LocalDate? = null
        override val school: School? = null
    }

    data class Holiday(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?
    ) : SchoolDay

    data class Weekend(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?
    ) : SchoolDay
}