package id.my.matahati.backoffice.data.remote

import id.my.matahati.backoffice.model.DashboardResponse
import id.my.matahati.backoffice.model.ErrorResponse
import id.my.matahati.backoffice.model.LoginRequest
import id.my.matahati.backoffice.model.LoginResponse
import id.my.matahati.backoffice.model.MeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/office/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/office/me")
    suspend fun getMe(): Response<MeResponse>

    @POST("api/office/logout")
    suspend fun logout(): Response<ErrorResponse>

    @GET("api/office/dashboard")
    suspend fun getDashboard(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("nid_outlet") nidOutlet: Int? = null
    ): Response<DashboardResponse>
}
