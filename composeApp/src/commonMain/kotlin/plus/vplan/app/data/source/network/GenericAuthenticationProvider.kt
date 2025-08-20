package plus.vplan.app.data.source.network

import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.model.VppSchoolAuthentication

class GenericAuthenticationProvider(
    private val schoolAuthenticationProvider: SchoolAuthenticationProvider,
    private val vppIdAuthenticationProvider: VppIdAuthenticationProvider
) {
    suspend fun getAuthentication(options: AuthenticationOptions): VppSchoolAuthentication? {
        val vppIdAuthentication = options.users.orEmpty()
            .firstNotNullOfOrNull { vppIdAuthenticationProvider.getAuthenticationForVppId(it) }
        if (vppIdAuthentication != null) return vppIdAuthentication

        val vppSchoolAlias = Alias(AliasProvider.Vpp, options.toString(), 1)
        return schoolAuthenticationProvider.getAuthenticationForSchool(setOf(vppSchoolAlias))
    }
}