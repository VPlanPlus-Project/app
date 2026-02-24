package plus.vplan.app.network.besteschule

interface BesteSchuleApi {
    suspend fun checkValidity(token: String): Boolean
}