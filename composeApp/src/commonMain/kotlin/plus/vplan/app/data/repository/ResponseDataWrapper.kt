package plus.vplan.app.data.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ResponseDataWrapper<T>(
    @SerialName("data") val data: T
) {
    companion object {
        val jsonParser = Json {
            ignoreUnknownKeys = true
        }
        inline fun <reified T> fromJson(json: String?): T? {
            if (json == null) return null
            val wrapper = try {
                jsonParser.decodeFromString<ResponseDataWrapper<T>>(json)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return wrapper.data
        }
    }
}