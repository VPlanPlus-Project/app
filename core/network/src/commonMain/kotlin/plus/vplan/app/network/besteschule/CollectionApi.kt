package plus.vplan.app.network.besteschule

interface CollectionApi {
    suspend fun getAll(): List<CollectionDto>
    suspend fun getById(id: Int): CollectionDto
}

data class CollectionDto(
    val id: Int,
    val type: String,
    val weighting: Float,
    val name: String,
    val givenAt: String,
    val interval: IntervalDto,
    val subject: SubjectDto,
    val teacher: TeacherDto,
) {
    data class SubjectDto(
        val id: Int,
        val localId: String,
        val name: String,
    )

    data class TeacherDto(
        val id: Int,
        val localId: String,
        val forename: String,
        val lastname: String,
    )
}