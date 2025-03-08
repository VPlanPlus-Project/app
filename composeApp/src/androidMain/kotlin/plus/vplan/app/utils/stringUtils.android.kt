package plus.vplan.app.utils

import java.security.MessageDigest

actual fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
        .lowercase()
}