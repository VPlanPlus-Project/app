package plus.vplan.app

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.posthog.PostHog

actual fun getPlatform(): Platform = Platform.Android
actual fun capture(event: String, properties: Map<String, Any>?) {
    PostHog.capture(event, userProperties = properties)
}

actual fun isDebug(): Boolean = BuildConfig.APP_DEBUG
actual fun setPostHogProperty(key: String, value: String) {
    PostHog.register(key, value)
}

actual fun posthogIdentify(distinctId: String, userProperties: Map<String, Any>?, userPropertiesSetOnce: Map<String, Any>?) {
    PostHog.identify(distinctId, userProperties, userPropertiesSetOnce)
}

actual fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
    return PostHog.isFeatureEnabled(key, defaultValue)
}

actual fun firebaseIdentify(id: String) {
    Firebase.crashlytics.setUserId(id)
}