package plus.vplan.app.network.vpp.assessment

import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication

interface AssessmentApi {
    /**
     * Downloads assessments for the given subject instance aliases
     * @param schoolApiAccess Authentication for the school
     * @param subjectInstanceAliases List of subject instance aliases to filter by (empty = all)
     * @return List of assessment DTOs
     */
    suspend fun getAssessments(
        schoolApiAccess: VppSchoolAuthentication,
        subjectInstanceAliases: List<Alias>
    ): List<ApiAssessmentDto>

    /**
     * Downloads a single assessment by ID
     * @param schoolApiAccess Authentication for the school
     * @param assessmentId The ID of the assessment to fetch
     * @return The assessment DTO or null if not found
     */
    suspend fun getAssessmentById(
        schoolApiAccess: VppSchoolAuthentication,
        assessmentId: Int
    ): ApiAssessmentDto?

    /**
     * Creates a new assessment online
     * @return The ID of the created assessment
     */
    suspend fun createAssessment(
        vppId: VppId.Active,
        request: AssessmentPostRequest
    ): AssessmentPostResponse

    /**
     * Updates an existing assessment
     */
    suspend fun updateAssessment(
        vppId: VppId.Active,
        assessmentId: Int,
        request: AssessmentPatchRequest
    )

    /**
     * Deletes an assessment
     */
    suspend fun deleteAssessment(
        vppId: VppId.Active,
        assessmentId: Int
    )

    /**
     * Links a file to an assessment
     */
    suspend fun linkFile(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    )

    /**
     * Unlinks a file from an assessment
     */
    suspend fun unlinkFile(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    )
}
