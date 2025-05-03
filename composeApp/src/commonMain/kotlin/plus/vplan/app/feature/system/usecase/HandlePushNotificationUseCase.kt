package plus.vplan.app.feature.system.usecase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue

class HandlePushNotificationUseCase {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    suspend operator fun invoke(topic: String, payload: String) {
        when (topic) {
            "HOMEWORK_UPDATE" -> {
                val data = json.decodeFromString<HomeworkUpdate>(payload)
                data.homeworkIds.forEach {
                    App.homeworkSource.getById(it, forceUpdate = true).getFirstValue()
                }
            }
            "ASSESSMENT_UPDATE" -> {
                val data = json.decodeFromString<AssessmentUpdate>(payload)
                data.assessmentIds.forEach {
                    App.assessmentSource.getById(it, forceUpdate = true).getFirstValue()
                }
            }
        }
    }
}

@Serializable
private data class HomeworkUpdate(
    @SerialName("homework_ids") val homeworkIds: List<Int>
)

@Serializable
private data class AssessmentUpdate(
    @SerialName("assessment_ids") val assessmentIds: List<Int>
)