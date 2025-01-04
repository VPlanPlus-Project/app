package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

/**
 * @param id The school id and the week number concatenated with a "/", e.g. "67/13" (13th week of current school year)
 */
data class Week(
    val id: String,
    val calendarWeek: Int,
    val start: LocalDate,
    val end: LocalDate,
    val weekType: String,
    val weekIndex: Int,
    val school: Cacheable<School>
): CachedItem<Week> {
    constructor(
        calendarWeek: Int,
        start: LocalDate,
        end: LocalDate,
        weekType: String,
        weekIndex: Int,
        school: School
    ) : this(
        id = school.id.toString() + "/" + calendarWeek.toString(),
        calendarWeek = calendarWeek,
        start = start,
        end = end,
        weekType = weekType,
        weekIndex = weekIndex,
        school = Cacheable.Loaded(school)
    )

    override fun getItemId(): String = this.id
    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Week>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !this.school.isConfigSatisfied(configuration.school, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val school: CacheableItemSource.FetchConfiguration<School> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Week>()
}