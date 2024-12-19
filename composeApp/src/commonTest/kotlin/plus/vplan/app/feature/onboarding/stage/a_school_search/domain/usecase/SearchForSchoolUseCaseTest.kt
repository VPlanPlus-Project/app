package plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.runBlocking
import org.koin.test.KoinTest
import plus.vplan.app.data.repository.FakeSchoolRepository
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import kotlin.test.Test

class SearchForSchoolUseCaseTest : KoinTest {

    private val exampleSchools = listOf(
        OnlineSchool(
            id = 1,
            name = "Testschule",
            sp24Id = 10000000
        ),
        OnlineSchool(
            id = 2,
            name = "Goethe-Gesamtschule Berlin",
            sp24Id = 10000001
        ),
        OnlineSchool(
            id = 3,
            name = "153. Oberschule der Stadt Dresden",
            sp24Id = 10000002
        )
    )

    @Test
    fun `Test offline search`() {
        runBlocking {
            val schoolRepository = FakeSchoolRepository(true)
            val useCase = SearchForSchoolUseCase(schoolRepository)
            assertThat(useCase(""))
                .isInstanceOf(Response.Error.OnlineError.ConnectionError::class)
        }
    }

    @Test
    fun `Test empty search`() {
        runBlocking {
            val schoolRepository = FakeSchoolRepository(false)
            val useCase = SearchForSchoolUseCase(schoolRepository)
            val result = useCase("")
            assertThat(result).isInstanceOf(Response.Success::class)
            assertThat(result)
                .transform { (it as Response.Success).data }
                .isEqualTo(emptyList())
        }
    }

    @Test
    fun `Test search for school by normal name`() {
        runBlocking {
            val schoolRepository = FakeSchoolRepository(
                simulateNetworkOutage = false,
                simulateOnlineSchools = exampleSchools
            )
            val useCase = SearchForSchoolUseCase(schoolRepository)
            val result = useCase("test")
            assertThat(result).isInstanceOf(Response.Success::class)
            assertThat(result)
                .transform { (it as Response.Success).data }
                .containsOnly(exampleSchools.first { it.id == 1 })
        }
    }

    @Test
    fun `Test search for school by sp24 id`() {
        runBlocking {
            val schoolRepository = FakeSchoolRepository(
                simulateNetworkOutage = false,
                simulateOnlineSchools = exampleSchools
            )
            val useCase = SearchForSchoolUseCase(schoolRepository)
            val result = useCase("10000002")
            assertThat(result).isInstanceOf(Response.Success::class)
            assertThat(result)
                .transform { (it as Response.Success).data }
                .containsOnly(exampleSchools.first { it.id == 3 })
        }
    }

    @Test
    fun `Test search for school by short name`() {
        runBlocking {
            val schoolRepository = FakeSchoolRepository(
                simulateNetworkOutage = false,
                simulateOnlineSchools = exampleSchools
            )
            val useCase = SearchForSchoolUseCase(schoolRepository)
            val result = useCase("ggb")
            assertThat(result).isInstanceOf(Response.Success::class)
            assertThat(result)
                .transform { (it as Response.Success).data }
                .containsOnly(exampleSchools.first { it.id == 2 })
        }
    }
}