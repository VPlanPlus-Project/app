package plus.vplan.app.feature.settings.page.info.domain.usecase

class GetSystemInfoUseCase {
    operator fun invoke(): SystemInfo {
        val systemInfo = getSystemInfo()
        return systemInfo
    }
}

data class SystemInfo(
    val os: String,
    val osVersion: String,
    val manufacturer: String,
    val device: String
) {
    override fun toString(): String {
        return """
            OS: $os $osVersion
            Manufacturer: $manufacturer
            Device: $device
        """.trimIndent()
    }
}

expect fun getSystemInfo(): SystemInfo