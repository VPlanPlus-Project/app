package plus.vplan.app.network.vpp.school

import plus.vplan.app.core.model.Alias


interface SchoolApi {
    suspend fun getAll(): List<SchoolDto>
    suspend fun getByAlias(alias: Alias): SchoolDto?
}

data class SchoolDto(
    val id: Int,
    val name: String,
    val aliases: List<Alias>
) {
    data class Alias(
        val type: String,
        val value: String,
        val version: Int,
    )
}