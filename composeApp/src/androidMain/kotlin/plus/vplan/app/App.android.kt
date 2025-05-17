package plus.vplan.app

import com.posthog.PostHog

actual fun getPlatform(): Platform = Platform.Android
actual fun capture(event: String, properties: Map<String, Any>?) {
    PostHog.capture(event, userProperties = properties)
}