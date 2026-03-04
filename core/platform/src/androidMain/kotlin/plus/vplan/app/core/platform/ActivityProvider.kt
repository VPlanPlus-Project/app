package plus.vplan.app.core.platform

import android.app.Activity

interface ActivityProvider {
    val currentActivity: Activity?
}
