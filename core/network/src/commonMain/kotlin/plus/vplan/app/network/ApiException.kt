package plus.vplan.app.network

class ApiException(override val cause: Throwable?): RuntimeException("An API call failed.")