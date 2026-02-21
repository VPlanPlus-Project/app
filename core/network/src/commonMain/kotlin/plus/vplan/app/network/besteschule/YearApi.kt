package plus.vplan.app.network.besteschule

interface YearApi {
    suspend fun getById(id: Int): YearDto?
    suspend fun getAll(): List<YearDto>
}

data class YearDto(
    val id: Int,
    val name: String,
    val from: String,
    val to: String,
)