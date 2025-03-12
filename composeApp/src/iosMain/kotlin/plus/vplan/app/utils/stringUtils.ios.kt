package plus.vplan.app.utils

import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun String.sha256(): String {
    val input = this.utf8
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    digest.usePinned { digestPinned ->
        CC_SHA256(digestPinned.addressOf(0), input.size.convert(), digestPinned.addressOf(0))
    }
    val digestString = digest.joinToString(separator = "") { it -> it.toString(16) }
    return digestString.lowercase()
}