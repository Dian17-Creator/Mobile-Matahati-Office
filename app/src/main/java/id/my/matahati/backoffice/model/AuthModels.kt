package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("cemail") val cemail: String,
    @SerializedName("cpassword") val cpassword: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: LoginData? = null
)

data class LoginData(
    @SerializedName("token") val token: String = "",
    @SerializedName("token_type") val tokenType: String = "Bearer",
    @SerializedName("user") val user: User? = null
)

data class User(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role_name") val roleName: String = "",
    @SerializedName("role_owner") val roleOwner: Boolean = false,
    @SerializedName("role_spv") val roleSpv: Boolean = false,
    @SerializedName("role_cashier") val roleCashier: Boolean = false,
    @SerializedName("role_captain") val roleCaptain: Boolean = false,
    @SerializedName("is_super_admin") val isSuperAdmin: Boolean = false,
    @SerializedName("is_outlet_locked") val isOutletLocked: Boolean = false,
    @SerializedName("default_outlet_id") val defaultOutletId: Int? = null,
    @SerializedName("default_outlet_name") val defaultOutletName: String? = null,
    @SerializedName("outlets") val outlets: List<Outlet> = emptyList()
)

data class Outlet(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class ErrorResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class MeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MeData? = null
)

data class MeData(
    @SerializedName("user") val user: User? = null
)
