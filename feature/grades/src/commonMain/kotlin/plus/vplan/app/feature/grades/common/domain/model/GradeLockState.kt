package plus.vplan.app.feature.grades.common.domain.model

enum class GradeLockState(val canAccess: Boolean) {
    Locked(false), Unlocked(true), NotConfigured(true)
}