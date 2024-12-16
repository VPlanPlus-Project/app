package plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model

enum class IndiwareInitStepState {
    NOT_STARTED,
    IN_PROGRESS,
    SUCCESS,
}

enum class IndiwareInitStepType {
    DATA_LOADED,
    DATA_UPDATED
}