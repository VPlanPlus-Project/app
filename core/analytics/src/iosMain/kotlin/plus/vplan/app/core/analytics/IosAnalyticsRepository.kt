package plus.vplan.app.core.analytics

class IosAnalyticsRepository : AnalyticsRepository {
    override fun capture(event: String, properties: Map<String, Any>?) {
        // TODO: Implement PostHog for iOS
    }

    override fun setPostHogProperty(key: String, value: String) {
        // TODO: Implement PostHog for iOS
    }

    override fun posthogIdentify(
        distinctId: String,
        userProperties: Map<String, Any>?,
        userPropertiesSetOnce: Map<String, Any>?,
    ) {
        // TODO: Implement PostHog for iOS
    }

    override fun isFeatureEnabled(key: String, defaultValue: Boolean): Boolean {
        return defaultValue
    }

    override fun firebaseIdentify(id: String) {
        // TODO: Implement Firebase identify for iOS
    }
}
