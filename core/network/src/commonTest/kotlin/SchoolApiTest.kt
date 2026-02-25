import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import plus.vplan.app.network.vpp.school.SchoolApi
import plus.vplan.app.network.vpp.school.SchoolApiImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SchoolApiTest : KoinTest {

    private val schoolApi by inject<SchoolApi>()

    @BeforeTest
    fun setup() {
        startKoin {
            modules(module {
                single { getHttpClient() }
                singleOf(::SchoolApiImpl).bind<SchoolApi>()
            })
        }
    }

    @Test
    fun `Test all schools`() = runBlocking {
        assertTrue(schoolApi.getAll().also { it.forEach { school -> println(school.name) } }.isNotEmpty())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }
}