package plus.vplan.app.network.besteschule

interface IntervalApi {
    suspend fun getById(id: Int): IntervalDto?
    suspend fun getAll(): List<IntervalDto>
}

data class IntervalDto(
    val id: Int,
    val name: String,
    val type: String,
    val from: String,
    val to: String,
    val includedIntervalId: Int?,
    val yearId: Int,
)