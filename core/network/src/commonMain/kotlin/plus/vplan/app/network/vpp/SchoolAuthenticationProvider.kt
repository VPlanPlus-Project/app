package plus.vplan.app.network.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.database.dao.SchoolDao
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.VppSchoolAuthentication

/**
 * This class is used to get the authentication for a school based on its aliases. For example, when updating a protected
 * resource like a group or a homework, the app may try to download the item without authentication first to get the
 * school id with which the item is associated. It will then use this class to get the authentication for the school
 * and use it to update the item in a second request.
 */
class SchoolAuthenticationProvider(
    private val schoolDao: SchoolDao,
) {
    suspend fun getAuthenticationForSchool(schoolAliases: Set<Alias>): VppSchoolAuthentication? {
        val localId = schoolAliases
            .firstNotNullOfOrNull { alias ->
                schoolDao.getIdByAlias(value = alias.value, provider = alias.provider, version = alias.version).first()
            } ?: return null

        val school = schoolDao.findById(localId).first()?.toModel() as? School.AppSchool ?: return null

        return school.buildSp24AppAuthentication()
    }
}