package id.my.matahati.backoffice.data.remote

import id.my.matahati.backoffice.model.ErrorResponse
import id.my.matahati.backoffice.model.LoginRequest
import id.my.matahati.backoffice.model.LoginResponse
import id.my.matahati.backoffice.model.MeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("office/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("office/me")
    suspend fun getMe(): Response<MeResponse>

    @POST("office/logout")
    suspend fun logout(): Response<ErrorResponse>
}
