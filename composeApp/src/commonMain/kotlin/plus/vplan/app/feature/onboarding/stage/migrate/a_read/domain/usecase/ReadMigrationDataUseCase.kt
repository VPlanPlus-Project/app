package plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase

import kotlinx.io.bytestring.decodeToByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.model.Migration
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ReadMigrationDataUseCase {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    @OptIn(ExperimentalEncodingApi::class)
    operator fun invoke(base64: String): MigrationDataReadResult {
        val migrationData = try {
            val text = Base64.decodeToByteString(base64).decodeToString()
            json.decodeFromString<Migration>(text)
        } catch (e: Exception) {
            if (e is IllegalArgumentException || e is SerializationException) return MigrationDataReadResult.InvalidSequence
            throw e
        }

        return MigrationDataReadResult.Valid(migrationData)
    }
}

sealed class MigrationDataReadResult {
    object InvalidSequence : MigrationDataReadResult()
    data class Valid(val data: Migration) : MigrationDataReadResult()
}