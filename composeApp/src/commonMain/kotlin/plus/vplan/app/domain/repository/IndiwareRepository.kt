package plus.vplan.app.domain.repository

import plus.vplan.app.domain.data.Response

interface IndiwareRepository {
    suspend fun checkCredentials(sp24Id: Int, username: String, password: String): Response<Boolean>
}