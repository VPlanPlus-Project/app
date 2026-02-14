package plus.vplan.app.feature.settings.page.info.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.VppIdRepository

class SendFeedbackWithSp24CredentialsUseCase(
    private val vppIdRepository: VppIdRepository,
    private val getFeedbackMetadataUseCase: GetFeedbackMetadataUseCase
) {
    suspend operator fun invoke(sp24Credentials: VppSchoolAuthentication.Sp24, email: String?, message: String?): Response<Unit> {
        return vppIdRepository.sendFeedback(
            access = sp24Credentials,
            content = buildString {
                appendLine(message)
                appendLine()
                appendLine(getFeedbackMetadataUseCase().first())
                appendLine()
                appendLine("Anonymous using stundenplan24.de access:")
                appendLine("School ID: ${sp24Credentials.sp24SchoolId}")
                appendLine("Username: ${sp24Credentials.username}")
                appendLine("Password: ${sp24Credentials.password}")
            },
            email = email
        )
    }
}