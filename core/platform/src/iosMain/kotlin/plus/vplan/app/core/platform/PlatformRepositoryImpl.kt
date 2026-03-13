package plus.vplan.app.core.platform

import plus.vplan.app.core.model.application.AppPlatform

class PlatformRepositoryImpl : PlatformRepository {
    override fun getPlatform(): AppPlatform = AppPlatform.iOS
}
