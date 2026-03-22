package plus.vplan.app.network.vpp.user

interface VppIdApi {
    suspend fun getById(id: Int): VppIdDto?
}

data class VppIdDto(
    val id: Int,
    val name: String
)