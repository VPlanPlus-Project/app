package plus.vplan.app

/**
 * Log every SQL transaction with its queries and parameters. See the implementation of the Room
 * Builder.
 * @see plus.vplan.app.di.platformModule
 */
const val LOG_DATABASE_QUERIES: Boolean = false

/**
 * Log HTTP Requests, this includes the method, full path with parameters and request headers.
 * If the server sends an answer, its status code and headers will be logged as well.
 * @see plus.vplan.app.di.appModule
 */
const val LOG_HTTP_REQUESTS: Boolean = true

@Suppress("unused")
private val productionConfiguration = WebConfig(
    authUrl = "https://auth.vplan.plus",
    appApiUrl = "https://vplan.plus/api/app",
    apiUrl = "https://vplan.plus/api"
)

@Suppress("unused")
private val developmentConfiguration = WebConfig(
    authUrl = "https://auth.development.vplan.plus",
    appApiUrl = "https://app.development.vplan.plus/api/app",
    apiUrl = "https://api.development.vplan.plus/api"
)

@Suppress("unused")
private val werkbankConfiguration = WebConfig(
    authUrl = "https://auth.vpp.werkbank.space",
    appApiUrl = "https://vpp.werkbank.space/api/app",
    apiUrl = "https://vpp.local.vocus.dev/api"
)

val currentConfiguration = productionConfiguration

data class WebConfig(
    val authUrl: String,
    val appApiUrl: String,
    @Deprecated("Use appApiUrl instead") val apiUrl: String
)