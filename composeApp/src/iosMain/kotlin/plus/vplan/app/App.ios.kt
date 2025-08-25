package plus.vplan.app

actual fun capture(event: String, properties: Map<String, Any>?) {
    TODO("Not yet implemented")
}

actual fun isDebug(): Boolean {
    TODO("Not yet implemented")
}

actual fun setPostHogProperty(key: String, value: String) {
}

actual fun posthogIdentify(distinctId: String, userProperties: Map<String, Any>?, userPropertiesSetOnce: Map<String, Any>?) {
}

actual fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
    TODO("Not yet implemented")
}

actual fun firebaseIdentify(id: String) {
}