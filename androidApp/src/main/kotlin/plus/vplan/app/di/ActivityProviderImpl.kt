package plus.vplan.app.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import plus.vplan.app.core.platform.ActivityProvider

class ActivityProviderImpl(application: Application) : ActivityProvider,
    Application.ActivityLifecycleCallbacks {

    override var currentActivity: Activity? = null
        private set

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    // Remaining callbacks are no-ops
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
