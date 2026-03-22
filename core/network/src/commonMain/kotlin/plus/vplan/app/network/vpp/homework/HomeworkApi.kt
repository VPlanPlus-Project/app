package plus.vplan.app.network.vpp.homework

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.utils.Optional

interface HomeworkApi {
    suspend fun getHomeworkItems(
        access: VppSchoolAuthentication,
        filterGroups: List<String>? = null,
        filterSubjectInstances: List<String>? = null
    ): List<ApiHomeworkDto>

    suspend fun getHomeworkById(
        vppSchoolAuthentication: VppSchoolAuthentication,
        homeworkId: Int
    ): ApiHomeworkDto?

    suspend fun createHomework(
        vppId: VppId.Active,
        request: HomeworkPostRequest
    ): HomeworkPostResponse

    suspend fun updateHomework(
        vppId: VppId.Active,
        homeworkId: Int,
        request: HomeworkPatchRequest
    )

    suspend fun deleteHomework(
        vppId: VppId.Active,
        homeworkId: Int
    )

    suspend fun addTask(
        vppId: VppId.Active,
        homeworkId: Int,
        content: String
    ): Int

    suspend fun updateTask(
        vppId: VppId.Active,
        homeworkId: Int,
        taskId: Int,
        content: String? = null,
        isDone: Boolean? = null
    )

    suspend fun deleteTask(
        vppId: VppId.Active,
        homeworkId: Int,
        taskId: Int
    )

    suspend fun linkFile(
        vppId: VppId.Active,
        homeworkId: Int,
        fileId: Int
    )

    suspend fun unlinkFile(
        vppId: VppId.Active,
        homeworkId: Int,
        fileId: Int
    )
}

@Serializable
data class ApiHomeworkDto(
    @SerialName("id") val id: Int,
    @SerialName("created_by") val createdBy: EntityIdDto,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("due_to") val dueTo: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("group") val group: EntityIdDto?,
    @SerialName("subject_instance") val subjectInstance: EntityIdDto?,
    @SerialName("tasks") val tasks: List<ApiHomeworkTaskDtoWrapper>,
    @SerialName("files") val files: List<EntityIdDto>,
)

@Serializable
data class EntityIdDto(
    @SerialName("id") val id: Int
)

@Serializable
data class ApiHomeworkTaskDtoWrapper(
    @SerialName("value") val value: ApiHomeworkTaskDto
)

@Serializable
data class ApiHomeworkTaskDto(
    @SerialName("id") val id: Int,
    @SerialName("content") val content: String,
    @SerialName("done") val done: Boolean?
)

@Serializable
data class HomeworkPostRequest(
    @SerialName("subject_instance") val subjectInstance: Int? = null,
    @SerialName("group") val groupId: Int? = null,
    @SerialName("due_to") val dueTo: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("tasks") val tasks: List<String>,
)

@Serializable
data class HomeworkPostResponse(
    @SerialName("id") val id: Int,
    @SerialName("tasks") val tasks: List<HomeworkPostResponseItem>,
)

@Serializable
data class HomeworkPostResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("description_hash") val descriptionHash: String,
)

@Serializable
data class HomeworkPatchRequest(
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("subject_instance_id") val subjectInstanceId: Optional<Int?> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("group_id") val groupId: Optional<Int?> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("due_to") val dueTo: Optional<String> = Optional.Undefined(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("is_public") val isPublic: Optional<Boolean> = Optional.Undefined(),
)

@Serializable
data class HomeworkTaskUpdateDoneStateRequest(
    @SerialName("is_done") val isDone: Boolean
)

@Serializable
data class HomeworkTaskUpdateContentRequest(
    @SerialName("content") val content: String
)

@Serializable
data class HomeworkAddTaskRequest(
    @SerialName("task") val task: String
)

@Serializable
data class HomeworkFileLinkRequest(
    @SerialName("file_id") val fileId: Int
)
