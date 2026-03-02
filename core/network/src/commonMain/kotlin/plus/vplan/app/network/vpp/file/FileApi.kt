package plus.vplan.app.network.vpp.file

import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication

/**
 * API for file operations (upload, download, rename, delete).
 */
interface FileApi {
    /**
     * Upload a file to the server.
     * 
     * @param vppId The VppId to authenticate with
     * @param fileName The name of the file
     * @param fileBytes The file content
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return The server-assigned file ID
     * @throws NetworkRequestUnsuccessfulException if the request fails
     */
    suspend fun uploadFile(
        vppId: VppId.Active,
        fileName: String,
        fileBytes: ByteArray,
        onProgress: (Float) -> Unit
    ): Int
    
    /**
     * Download a file from the server.
     * 
     * @param fileId The file ID to download
     * @param schoolApiAccess Authentication credentials
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return The file content as bytes
     * @throws NetworkRequestUnsuccessfulException if the request fails
     */
    suspend fun downloadFile(
        fileId: Int,
        schoolApiAccess: VppSchoolAuthentication,
        onProgress: (Float) -> Unit
    ): ByteArray
    
    /**
     * Rename a file on the server.
     * 
     * @param fileId The file ID to rename
     * @param newName The new file name
     * @param vppId The VppId to authenticate with
     * @throws NetworkRequestUnsuccessfulException if the request fails
     */
    suspend fun renameFile(
        fileId: Int,
        newName: String,
        vppId: VppId.Active
    )
    
    /**
     * Delete a file from the server.
     * 
     * @param fileId The file ID to delete
     * @param vppId The VppId to authenticate with
     * @throws NetworkRequestUnsuccessfulException if the request fails
     */
    suspend fun deleteFile(
        fileId: Int,
        vppId: VppId.Active
    )
}
