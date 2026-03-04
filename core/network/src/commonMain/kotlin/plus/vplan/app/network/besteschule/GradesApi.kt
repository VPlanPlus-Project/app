package plus.vplan.app.network.besteschule

interface GradesApi {
    suspend fun getAll(): List<GradesDto>
    suspend fun getAllForUser(userId: Int): List<GradesDto>
    suspend fun getById(id: Int): GradesDto?
}

data class GradesDto(
    val id: Int,
    val value: String,
    val schulverwalterUserId: Int,
    val givenAt: String,
    val read: Boolean,
    val subject: Subject,
    val teacher: Teacher,
    val collection: Collection,
) {
    data class Subject(
        val id: Int,
        val localId: String,
        val name: String,
    )

    data class Teacher(
        val id: Int,
        val localId: String,
        val forename: String,
        val lastname: String,
    )

    data class Collection(
        val id: Int,
        val type: String,
        val weighting: Float,
        val name: String,
        val givenAt: String,
        val subjectId: Int,
        val teacherId: Int,
        val intervalId: Int,
    )
}