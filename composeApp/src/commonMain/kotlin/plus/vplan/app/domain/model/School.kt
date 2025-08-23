@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.model

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasedItem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class School(): AliasedItem<DataTag> {

    abstract val name: String
    abstract val cachedAt: Instant
    override val tags: Set<DataTag> = emptySet()

    data class CachedSchool(
        override val id: Uuid,
        override val name: String,
        override val aliases: Set<Alias>,
        override val cachedAt: Instant,
    ) : School()

    data class AppSchool(
        override val id: Uuid,
        override val name: String,
        override val aliases: Set<Alias>,
        override val cachedAt: Instant,
        val groupIds: List<Uuid>,
        val sp24Id: String,
        val username: String,
        val password: String,
        val daysPerWeek: Int = 5,
        val credentialsValid: Boolean,
    ) : School() {
        fun buildSp24AppAuthentication(): VppSchoolAuthentication.Sp24 {
            return VppSchoolAuthentication.Sp24(
                sp24SchoolId = sp24Id,
                username = username,
                password = password
            )
        }
    }
}

sealed class VppSchoolAuthentication() {

    abstract val identifier: String
    abstract fun authentication(builder: HttpRequestBuilder)
    class Sp24(
        val sp24SchoolId: String,
        val username: String,
        val password: String,
    ) : VppSchoolAuthentication() {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.basicAuth("$username@$sp24SchoolId", password)
        }

        override val identifier: String = "sp24.$sp24SchoolId.1"
    }

    class Vpp(
        vppSchoolId: Int,
        val vppIdId: Int,
        val vppIdToken: String
    ): VppSchoolAuthentication() {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.bearerAuth(vppIdToken)
        }

        override val identifier: String = vppSchoolId.toString()
    }
}