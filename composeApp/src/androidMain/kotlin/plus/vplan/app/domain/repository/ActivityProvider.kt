package plus.vplan.app.domain.repository

import android.app.Activity

interface ActivityProvider {
    val currentActivity: Activity?
}