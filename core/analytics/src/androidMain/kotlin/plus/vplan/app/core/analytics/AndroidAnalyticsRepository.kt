package plus.vplan.app.core.analytics

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.posthog.PostHog

class AndroidAnalyticsRepository(
    private val isDebug: Boolean,
) : AnalyticsRepository {

    override fun capture(event: String, properties: Map<String, Any>?) {
        if (isDebug) {
            Log.i("PostHog", buildString {
                appendLine("POSTHOG EVENT")
                appendLine("$event would have been sent with properties:")
                properties?.forEach { (key, value) ->
                    appendLine(" - $key: $value")
                } ?: appendLine(" - No properties")
                appendLine("This is a debug build, so the event was not sent.")
            })
            return
        }
        PostHog.capture(event, userProperties = properties)
    }

    override fun setPostHogProperty(key: String, value: String) {
        if (isDebug) {
            Log.i("PostHog", "POSTHOG PROPERTY: $key would have been set to $value. This is a debug build, so the property was not set.")
            return
        }
        PostHog.register(key, value)
    }

    override fun posthogIdentify(
        distinctId: String,
        userProperties: Map<String, Any>?,
        userPropertiesSetOnce: Map<String, Any>?,
    ) {
        if (isDebug) {
            Log.i("PostHog", buildString {
                appendLine("POSTHOG IDENTIFY")
                appendLine("Distinct ID: $distinctId")
                appendLine("User Properties:")
                userProperties?.forEach { (key, value) ->
                    appendLine(" - $key: $value")
                } ?: appendLine(" - No properties")
                appendLine("User Properties Set Once:")
                userPropertiesSetOnce?.forEach { (key, value) ->
                    appendLine(" - $key: $value")
                } ?: appendLine(" - No properties")
                appendLine("This is a debug build, so the identify was not sent.")
            })
            return
        }
        PostHog.identify(distinctId, userProperties, userPropertiesSetOnce)
    }

    override fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
        return PostHog.isFeatureEnabled(key, defaultValue)
    }

    override fun firebaseIdentify(id: String) {
        if (isDebug) {
            Log.i("Firebase", "FIREBASE IDENTIFY: User ID would have been set to $id. This is a debug build, so the ID was not set.")
            return
        }
        Firebase.crashlytics.setUserId(id)
    }
}
