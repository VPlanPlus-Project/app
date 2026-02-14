package plus.vplan.app.data.source.network

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.VppSchoolAuthentication

class GenericAuthenticationProvider(
    private val schoolAuthenticationProvider: SchoolAuthenticationProvider,
    private val vppIdAuthenticationProvider: VppIdAuthenticationProvider
) {
    suspend fun getAuthentication(options: AuthenticationOptions): VppSchoolAuthentication? {
        val vppIdAuthentication = options.users.orEmpty()
            .firstNotNullOfOrNull { vppIdAuthenticationProvider.getAuthenticationForVppId(it) }
        if (vppIdAuthentication != null) return vppIdAuthentication

        val schoolAliases = options.schoolIds.orEmpty().map { Alias(AliasProvider.Vpp, it.toString(), 1) }.toSet()
        return schoolAuthenticationProvider.getAuthenticationForSchool(schoolAliases)
    }
}