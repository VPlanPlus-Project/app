package plus.vplan.app.feature.homework.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.extension
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.utils.getBitmapFromBytes
import plus.vplan.app.utils.getDataFromPdf
import kotlin.uuid.Uuid

class NewHomeworkViewModel(
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val isVppIdBannerAllowedUseCase: IsVppIdBannerAllowedUseCase,
    private val hideVppIdBannerUseCase: HideVppIdBannerUseCase,
    private val createHomeworkUseCase: CreateHomeworkUseCase
) : ViewModel() {
    var state by mutableStateOf(NewHomeworkState())
        private set

    init {
        viewModelScope.launch {
            combine(
                getCurrentProfileUseCase(),
                isVppIdBannerAllowedUseCase()
            ) { currentProfile, canShowVppIdBanner ->
                state.copy(
                    currentProfile = (currentProfile as? Profile.StudentProfile).also {
                        it?.getGroupItem()
                        it?.getDefaultLessons()?.onEach { defaultLesson ->
                            defaultLesson.getTeacherItem()
                            defaultLesson.getCourseItem()
                        }
                    },
                    isPublic = if ((currentProfile as? Profile.StudentProfile)?.vppId == null) null else true,
                    canShowVppIdBanner = canShowVppIdBanner
                )
            }.collect { state = it }
        }
    }

    fun onEvent(event: NewHomeworkEvent) {
        viewModelScope.launch {
            when (event) {
                is NewHomeworkEvent.AddTask -> state = state.copy(tasks = state.tasks.plus(Uuid.random() to event.task))
                is NewHomeworkEvent.UpdateTask -> state = state.copy(tasks = state.tasks.plus(event.taskId to event.task))
                is NewHomeworkEvent.RemoveTask -> state = state.copy(tasks = state.tasks.minus(event.taskId))
                is NewHomeworkEvent.SelectDefaultLesson -> state = state.copy(selectedDefaultLesson = event.defaultLesson.also {
                    it?.getCourseItem()
                    it?.getTeacherItem()
                    it?.getGroupItems()
                })
                is NewHomeworkEvent.SelectDate -> state = state.copy(selectedDate = event.date)
                is NewHomeworkEvent.SetVisibility -> state = state.copy(isPublic = event.isPublic)
                is NewHomeworkEvent.AddFile -> {
                    val bytes = event.file.readBytes()
                    val size = event.file.getSize() ?: 0L
                    val name = event.file.name
                    val file = when (event.file.extension) {
                        "pdf" -> {
                            val data = getDataFromPdf(bytes)
                            File.Document(
                                platformFile = event.file,
                                bitmap = data?.firstPage,
                                size = size,
                                name = name,
                                pages = data?.pages ?: 0
                            )
                        }
                        "jpg", "jpeg", "png" -> {
                            val bitmap = getBitmapFromBytes(bytes)
                            File.Image(
                                platformFile = event.file,
                                bitmap = bitmap,
                                size = size,
                                width = bitmap?.width ?: 0,
                                height = bitmap?.height ?: 0,
                                name = name,
                                rotation = 0
                            )
                        }
                        else -> {
                            val bitmap = getBitmapFromBytes(bytes)
                            File.Other(
                                platformFile = event.file,
                                bitmap = bitmap,
                                size = size,
                                name = name
                            )
                        }
                    }
                    state = state.copy(files = state.files + file)
                }
                is NewHomeworkEvent.UpdateFile -> {
                    state = state.copy(files = state.files.map { file -> if (file.platformFile.path.hashCode() == event.file.platformFile.path.hashCode()) event.file else file })
                }
                is NewHomeworkEvent.RemoveFile -> {
                    state = state.copy(files = state.files.filter { it.platformFile.path.hashCode() != event.file.platformFile.path.hashCode() })
                }
                is NewHomeworkEvent.HideVppIdBanner -> hideVppIdBannerUseCase()
                is NewHomeworkEvent.Save -> {
                    if (state.tasks.isEmpty()) return@launch
                    if (state.currentProfile == null) return@launch
                    if (state.selectedDate == null) return@launch
                    createHomeworkUseCase(state.tasks.values.toList(), state.isPublic, state.selectedDate!!, state.selectedDefaultLesson, state.files)
                }
            }
        }
    }
}

data class NewHomeworkState(
    val tasks: Map<Uuid, String> = emptyMap(),
    val currentProfile: Profile.StudentProfile? = null,
    val selectedDefaultLesson: DefaultLesson? = null,
    val selectedDate: LocalDate? = null,
    val isPublic: Boolean? = null,
    val files: List<File> = emptyList(),
    val canShowVppIdBanner: Boolean = false
)

abstract class File {
    abstract val platformFile: PlatformFile
    abstract val name: String
    abstract val bitmap: ImageBitmap?
    abstract val size: Long

    data class Document(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String,
        val pages: Int
    ) : File() {
        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Document {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name, pages = pages)
        }
    }

    data class Image(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String,
        val rotation: Int,
        val width: Int,
        val height: Int
    ) : File() {
        val widthWithRotation: Int
            get() = if (rotation % 2 == 0) width else height
        val heightWithRotation: Int
            get() = if (rotation % 2 == 0) height else width

        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Image {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name, rotation = rotation, width = width, height = height)
        }
    }

    data class Other(
        override val platformFile: PlatformFile,
        override val bitmap: ImageBitmap?,
        override val size: Long,
        override val name: String
    ) : File() {
        override fun copyBase(platformFile: PlatformFile, bitmap: ImageBitmap?, size: Long, name: String): Other {
            return copy(platformFile = platformFile, bitmap = bitmap, size = size, name = name)
        }

        constructor(platformFile: PlatformFile) : this(platformFile, null, platformFile.getSize() ?: 0L, platformFile.name)
    }

    abstract fun copyBase(
        platformFile: PlatformFile = this.platformFile,
        bitmap: ImageBitmap? = this.bitmap,
        size: Long = this.size,
        name: String = this.name
    ): File
}

sealed class NewHomeworkEvent {
    data class AddTask(val task: String) : NewHomeworkEvent()
    data class UpdateTask(val taskId: Uuid, val task: String) : NewHomeworkEvent()
    data class RemoveTask(val taskId: Uuid) : NewHomeworkEvent()

    data class SelectDefaultLesson(val defaultLesson: DefaultLesson?) : NewHomeworkEvent()
    data class SelectDate(val date: LocalDate) : NewHomeworkEvent()

    data class SetVisibility(val isPublic: Boolean) : NewHomeworkEvent()

    data class AddFile(val file: PlatformFile) : NewHomeworkEvent()
    data class UpdateFile(val file: File) : NewHomeworkEvent()
    data class RemoveFile(val file: File) : NewHomeworkEvent()

    data object HideVppIdBanner : NewHomeworkEvent()
    data object Save : NewHomeworkEvent()
}