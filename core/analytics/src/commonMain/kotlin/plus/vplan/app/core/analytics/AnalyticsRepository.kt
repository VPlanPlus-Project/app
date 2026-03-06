package plus.vplan.app.core.analytics

interface AnalyticsRepository {
    fun capture(event: String, properties: Map<String, Any>? = null)
    fun captureError(location: String, message: String) {
        capture("error", mapOf("location" to location, "message" to message))
    }
    fun setPostHogProperty(key: String, value: String)
    fun posthogIdentify(
        distinctId: String,
        userProperties: Map<String, Any>?,
        userPropertiesSetOnce: Map<String, Any>?,
    )
    fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean
    fun firebaseIdentify(id: String)
}
