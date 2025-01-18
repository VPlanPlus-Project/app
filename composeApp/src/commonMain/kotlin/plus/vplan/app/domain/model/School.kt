package plus.vplan.app.domain.model

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import plus.vplan.app.domain.cache.Item

sealed interface School: Item {
    val id: Int
    override fun getEntityId(): String = id.toString()

    val groups: List<Int>

    val name: String

    fun getSchoolApiAccess(): SchoolApiAccess

    data class IndiwareSchool(
        override val id: Int,
        override val name: String,
        override val groups: List<Int>,
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
        override val groups: List<Int>
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

    class VppIdAccess(
        schoolId: Int,
        val accessToken: String
    ) : SchoolApiAccess(schoolId) {
        override fun authentication(builder: HttpRequestBuilder) {
            builder.bearerAuth(accessToken)
        }
    }
}