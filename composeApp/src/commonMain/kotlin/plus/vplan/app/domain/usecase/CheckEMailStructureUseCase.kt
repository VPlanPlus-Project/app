package plus.vplan.app.domain.usecase

class CheckEMailStructureUseCase {
    private val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    operator fun invoke(input: String): Boolean {
        return regex.matches(input)
    }
}