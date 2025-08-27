package plus.vplan.app.data.source.network

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.SchoolRepository

/**
 * This class is used to get the authentication for a school based on its aliases. For example, when updating a protected
 * resource like a group or a homework, the app may try to download the item without authentication first to get the
 * school id with which the item is associated. It will then use this class to get the authentication for the school
 * and use it to update the item in a second request.
 */
class SchoolAuthenticationProvider(
    private val schoolRepository: SchoolRepository
) {
    suspend fun getAuthenticationForSchool(schoolAliases: Set<Alias>): VppSchoolAuthentication? {
        val localSchoolId = schoolRepository.resolveAliasesToLocalId(schoolAliases.toList()) ?: return null
        val localSchool = schoolRepository.getByLocalId(localSchoolId).first() ?: return null
        if (localSchool !is School.AppSchool) return null
        return localSchool.buildSp24AppAuthentication()
    }
}