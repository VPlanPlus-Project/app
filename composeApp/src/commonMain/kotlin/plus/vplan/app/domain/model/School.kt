package plus.vplan.app.domain.model

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

sealed interface School : CachedItem<School> {
    val id: Int

    val groups: List<Cacheable<Group>>

    val name: String

    override fun getItemId(): String = this.id.toString()
    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<School>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.groups is Group.Fetch && groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
        }
        return true
    }

    data class Fetch(
        val groups: CacheableItemSource.FetchConfiguration<Group> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<School>()

    fun getSchoolApiAccess(): SchoolApiAccess

    data class IndiwareSchool(
        override val id: Int,
        override val name: String,
        override val groups: List<Cacheable<Group>>,
        val sp24Id: String,
        val username: String,
        val password: String,
        val daysPerWeek: Int = 5,
        val studentsHaveFullAccess: Boolean = false,
        val schoolDownloadMode: SchoolDownloadMode = SchoolDownloadMode.INDIWARE_WOCHENPLAN_6
    ) : School {
        enum class SchoolDownloadMode {
            INDIWARE_WOCHENPLAN_6, INDIWARE_MOBIL
        }

        override fun getSchoolApiAccess(): SchoolApiAccess {
            return SchoolApiAccess.IndiwareAccess(
                schoolId = id,
                sp24id = sp24Id,
                username = username,
                password = password
            )
        }
    }

    data class DefaultSchool(
        override val id: Int,
        override val name: String,
        override val groups: List<Cacheable<Group>>
    ) : School {
        override fun getSchoolApiAccess(): SchoolApiAccess {
            throw IllegalStateException("Default schools cannot be accessed via api")
        }
    }
}

sealed class SchoolApiAccess(
    val schoolId: Int
) {
    abstract fun authentication(builder: HttpRequestBuilder)
    class IndiwareAccess(
        schoolId: Int,
        val sp24id: String,
        val username: String,
        val password: String
    ) : SchoolApiAccess(schoolId) {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.basicAuth("$username@$sp24id", password)
        }
    }
}