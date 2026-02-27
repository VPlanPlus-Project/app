package plus.vplan.app.utils

expect fun readLatestClipboardValue(): String?
expect fun copyToClipboard(title: String, value: String)