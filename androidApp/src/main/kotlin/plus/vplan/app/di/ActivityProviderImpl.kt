package plus.vplan.app.di

import android.app.Activity
import plus.vplan.app.domain.repository.ActivityProvider

object ActivityProviderImpl : ActivityProvider {
    override var currentActivity: Activity? = null
}