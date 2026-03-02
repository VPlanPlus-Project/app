package plus.vplan.app.core.data.file

import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Response

sealed class FileOperationProgress {
    data object Idle : FileOperationProgress()
    data class Uploading(val progress: Float) : FileOperationProgress()
    data class Downloading(val progress: Float) : FileOperationProgress()
    data object GeneratingThumbnail : FileOperationProgress()
    data class Success(val file: File) : FileOperationProgress()
    data class Error(val error: Response.Error) : FileOperationProgress()
}
