package plus.vplan.app.domain.model

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.AliasedItem
import plus.vplan.lib.sp24.source.Authentication
import kotlin.uuid.Uuid

sealed interface School: AliasedItem<DataTag> {
    override val tags: Set<DataTag>
        get() = emptySet()

    val groups: List<Uuid>

    val name: String
    val cachedAt: Instant

    fun getSchoolApiAccess(): SchoolApiAccess?

    fun getVppSchoolId(): Int? {
        return aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull()
    }

    data class Sp24School(
        override val id: Uuid,
        override val name: String,
        override val groups: List<Uuid>,
        override val cachedAt: Instant,
        val sp24Id: String,
        val username: String,
        val password: String,
        val daysPerWeek: Int = 5,
        val credentialsValid: Boolean,
        override val aliases: Set<Alias>
    ) : School {
        override fun getSchoolApiAccess(): SchoolApiAccess {
            return SchoolApiAccess.IndiwareAccess(
                vppSchoolId = aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt(),
                sp24id = sp24Id,
                username = username,
                password = password
            )
        }

        fun getSp24LibAuthentication() = Authentication(
            indiwareSchoolId = sp24Id,
            username = username,
            password = password
        )
    }

    data class DefaultSchool(
        override val id: Uuid,
        override val name: String,
        override val groups: List<Uuid>,
        override val cachedAt: Instant,
        override val aliases: Set<Alias>
    ) : School {
        override fun getSchoolApiAccess(): SchoolApiAccess? = null
    }
}

sealed class SchoolApiAccess(
    val vppSchoolId: Int?
) {
    abstract fun authentication(builder: HttpRequestBuilder)
    class IndiwareAccess(
        vppSchoolId: Int?,
        val sp24id: String,
        val username: String,
        val password: String
    ) : SchoolApiAccess(vppSchoolId) {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.basicAuth("$username@$sp24id", password)
        }
    }

    class VppIdAccess(
        vppSchoolId: Int,
        val accessToken: String,
        val vppIdId: Int,
    ) : SchoolApiAccess(vppSchoolId) {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.bearerAuth(accessToken)
        }
    }
}