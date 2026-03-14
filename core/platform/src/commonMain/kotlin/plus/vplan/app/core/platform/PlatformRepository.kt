package plus.vplan.app.core.platform

import plus.vplan.app.core.model.application.AppPlatform

interface PlatformRepository {
    fun getPlatform(): AppPlatform
}
