package plus.vplan.app

actual fun capture(event: String, properties: Map<String, Any>?) {}

actual fun isDebug(): Boolean = false

actual fun setPostHogProperty(key: String, value: String) {
}

actual fun posthogIdentify(distinctId: String, userProperties: Map<String, Any>?, userPropertiesSetOnce: Map<String, Any>?) {
}

actual fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
    return true
}

actual fun firebaseIdentify(id: String) {
}