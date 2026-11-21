package id.my.matahati.backoffice.repository

import android.util.Log
import com.google.gson.Gson
import id.my.matahati.backoffice.data.local.SessionManager
import id.my.matahati.backoffice.data.remote.ApiService
import id.my.matahati.backoffice.model.CategoryListResponse
import id.my.matahati.backoffice.model.CreateCategoryRequest
import id.my.matahati.backoffice.model.DashboardResponse
import id.my.matahati.backoffice.model.ErrorResponse
import id.my.matahati.backoffice.model.LoginRequest
import id.my.matahati.backoffice.model.LoginResponse
import id.my.matahati.backoffice.model.ProductListResponse
import id.my.matahati.backoffice.model.PurchaseDetailResponse
import id.my.matahati.backoffice.model.PurchaseListResponse
import id.my.matahati.backoffice.model.ReportResponse
import id.my.matahati.backoffice.model.User
import okhttp3.ResponseBody

class UnauthorizedException(message: String) : Exception(message)

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

    fun getToken(): String? = sessionManager.getToken()

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

    suspend fun getDashboard(
        startDate: String?,
        endDate: String?,
        nidOutlet: Int?
    ): Result<DashboardResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val response = apiService.getDashboard(startDate, endDate, outletParam)
            if (response.isSuccessful && response.body() != null) {
                val dashboardRes = response.body()!!
                if (dashboardRes.success) {
                    Result.success(dashboardRes)
                } else {
                    Result.failure(Exception(if (dashboardRes.message.isNotBlank()) dashboardRes.message else "Gagal memuat data dashboard"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getPurchases(
        tab: String?,
        startDate: String?,
        endDate: String?,
        nidOutlet: Int?,
        search: String?,
        page: Int? = 1
    ): Result<PurchaseListResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val searchParam = if (search.isNullOrBlank()) null else search
            val response = apiService.getPurchases(tab, startDate, endDate, outletParam, searchParam, page)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (res.message.isNotBlank()) res.message else "Gagal memuat daftar pembelian"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getPurchaseDetail(id: Int): Result<PurchaseDetailResponse> {
        return try {
            val response = apiService.getPurchaseDetail(id)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (res.message.isNotBlank()) res.message else "Gagal memuat detail pembelian"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getReports(
        type: String?,
        startDate: String?,
        endDate: String?,
        nidOutlet: Int?,
        page: Int? = 1
    ): Result<ReportResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val response = apiService.getReports(type, startDate, endDate, outletParam, page)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (res.message.isNotBlank()) res.message else "Gagal memuat laporan"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun downloadReportExcel(
        type: String?,
        startDate: String?,
        endDate: String?,
        nidOutlet: Int?
    ): Result<ResponseBody> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val response = apiService.downloadReportExcel(type, startDate, endDate, outletParam)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val rawError = response.errorBody()?.string()
                Log.e("ReportExport", "API Download Error HTTP ${response.code()}: $rawError")
                val errorMsg = parseErrorMessage(rawError)
                Result.failure(Exception("HTTP ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("ReportExport", "API Download Exception", e)
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getInventory(
        tab: String?,
        startDate: String?,
        endDate: String?,
        nidOutlet: Int?,
        page: Int? = 1,
        perPage: Int? = 50
    ): Result<id.my.matahati.backoffice.model.InventoryResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val response = apiService.getInventory(tab, startDate, endDate, outletParam, page, perPage)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (!res.message.isNullOrBlank()) res.message else "Gagal memuat data inventori"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getInventoryDetail(
        type: String?,
        no: String
    ): Result<id.my.matahati.backoffice.model.InventoryDetailResponse> {
        return try {
            val response = apiService.getInventoryDetail(type, no)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (!res.message.isNullOrBlank()) res.message else "Gagal memuat detail inventori"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun downloadInventoryPdf(
        type: String?,
        no: String
    ): Result<ResponseBody> {
        return try {
            val response = apiService.downloadInventoryPdf(type, no)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val rawError = response.errorBody()?.string()
                Log.e("InventoryPdf", "API Download PDF Error HTTP ${response.code()}: $rawError")
                val errorMsg = parseErrorMessage(rawError)
                Result.failure(Exception("HTTP ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("InventoryPdf", "API Download PDF Exception", e)
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getProducts(
        nidCategory: Int?,
        search: String?,
        hideEmpty: Boolean,
        nidOutlet: Int?,
        page: Int? = 1,
        perPage: Int? = 50
    ): Result<ProductListResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val categoryParam = if (nidCategory != null && nidCategory > 0) nidCategory else null
            val searchParam = if (search.isNullOrBlank()) null else search
            val hideEmptyParam = if (hideEmpty) 1 else 0

            val response = apiService.getProducts(categoryParam, searchParam, hideEmptyParam, outletParam, page, perPage)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (!res.message.isNullOrBlank()) res.message else "Gagal memuat produk"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun getCategories(
        search: String?,
        nidOutlet: Int?
    ): Result<CategoryListResponse> {
        return try {
            val outletParam = if (nidOutlet != null && nidOutlet > 0) nidOutlet else null
            val searchParam = if (search.isNullOrBlank()) null else search

            val response = apiService.getCategories(searchParam, outletParam)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (!res.message.isNullOrBlank()) res.message else "Gagal memuat kategori"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
    }

    suspend fun storeCategory(request: CreateCategoryRequest): Result<ErrorResponse> {
        return try {
            val response = apiService.storeCategory(request)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.success) {
                    Result.success(res)
                } else {
                    Result.failure(Exception(if (!res.message.isNullOrBlank()) res.message else "Gagal menyimpan kategori"))
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clearSession()
                    Result.failure(UnauthorizedException("Sesi Anda telah berakhir, silakan login kembali"))
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Terjadi kesalahan koneksi internet"))
        }
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
