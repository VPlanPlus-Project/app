package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

data class Holiday(
    val id: String,
    val date: LocalDate,
    val school: Cacheable<School>
): CachedItem<Holiday> {
    constructor(date: LocalDate, school: School) : this(id = "${school.id}/$date", date = date, school = Cacheable.Loaded(school))

    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Holiday>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !school.isConfigSatisfied(configuration.school, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val school: CacheableItemSource.FetchConfiguration<School> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Holiday>()
}