package id.my.matahati.backoffice.repository

import com.google.gson.Gson
import id.my.matahati.backoffice.data.local.SessionManager
import id.my.matahati.backoffice.data.remote.ApiService
import id.my.matahati.backoffice.model.ErrorResponse
import id.my.matahati.backoffice.model.LoginRequest
import id.my.matahati.backoffice.model.LoginResponse
import id.my.matahati.backoffice.model.User

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    private val gson = Gson()

    suspend fun login(cemail: String, cpassword: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(cemail = cemail, cpassword = cpassword))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success && loginResponse.data != null) {
                    val token = loginResponse.data.token
                    val user = loginResponse.data.user ?: User()
                    sessionManager.saveSession(token, user)
                    Result.success(loginResponse)
                } else {
                    val msg = if (loginResponse.message.isNotBlank()) loginResponse.message else "Login gagal"
                    Result.failure(Exception(msg))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun checkSession(): Boolean {
        if (!sessionManager.isLoggedIn()) return false
        return try {
            val response = apiService.getMe()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val meUser = body.data?.user
                val token = sessionManager.getToken()
                if (meUser != null && token != null) {
                    sessionManager.saveSession(token, meUser)
                }
                true
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                }
                false
            }
        } catch (e: Exception) {
            sessionManager.isLoggedIn()
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout()
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    fun getSavedUser(): User? {
        return sessionManager.getUser()
    }

    fun getSelectedOutletId(): Int {
        return sessionManager.getSelectedOutletId()
    }

    fun getSelectedOutletName(): String {
        return sessionManager.getSelectedOutletName()
    }

    fun setSelectedOutlet(outletId: Int, outletName: String) {
        sessionManager.setSelectedOutletId(outletId, outletName)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "Terjadi kesalahan pada server"
        return try {
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            if (errorResponse != null && errorResponse.message.isNotBlank()) {
                errorResponse.message
            } else {
                "Login gagal (HTTP Error)"
            }
        } catch (e: Exception) {
            "Gagal memproses respon server"
        }
    }
}
