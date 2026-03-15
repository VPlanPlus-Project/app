package plus.vplan.app.core.model.application.network

class ApiException(override val cause: Throwable?): RuntimeException("An API call failed.")