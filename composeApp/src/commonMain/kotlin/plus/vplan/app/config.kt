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