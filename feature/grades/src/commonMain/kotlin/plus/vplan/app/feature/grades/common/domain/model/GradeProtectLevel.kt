package plus.vplan.app.feature.grades.common.domain.model

enum class GradeProtectLevel {
    /**
     * Uses the biometric authentication methods provided by the users device like fingerprint and face ID if available.
     */
    Biometric,

    /**
     * Does not protect grades
     */
    None
}