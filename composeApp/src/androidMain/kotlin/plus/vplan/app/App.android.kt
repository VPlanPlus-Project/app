package plus.vplan.app

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.posthog.PostHog

actual fun getPlatform(): Platform = Platform.Android
actual fun capture(event: String, properties: Map<String, Any>?) {
    if (isDebug()) {
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

actual fun isDebug(): Boolean = AndroidMainBuildConfig.APP_DEBUG
actual fun setPostHogProperty(key: String, value: String) {
    if (isDebug()) {
        Log.i("PostHog", "POSTHOG PROPERTY: $key would have been set to $value. This is a debug build, so the property was not set.")
        return
    }
    PostHog.register(key, value)
}

actual fun posthogIdentify(distinctId: String, userProperties: Map<String, Any>?, userPropertiesSetOnce: Map<String, Any>?) {
    if (isDebug()) {
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

actual fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
    return PostHog.isFeatureEnabled(key, defaultValue)
}

actual fun firebaseIdentify(id: String) {
    if (isDebug()) {
        Log.i("Firebase", "FIREBASE IDENTIFY: User ID would have been set to $id. This is a debug build, so the ID was not set.")
        return
    }
    Firebase.crashlytics.setUserId(id)
}

actual val versionCode: Int = AndroidMainBuildConfig.APP_VERSION_CODE
actual val versionName: String = AndroidMainBuildConfig.APP_VERSION