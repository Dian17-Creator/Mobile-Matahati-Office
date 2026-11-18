package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: DashboardData? = null
)

data class DashboardData(
    @SerializedName("filter") val filter: DashboardFilter? = null,
    @SerializedName("summary") val summary: DashboardSummary? = null,
    @SerializedName("top_groups") val topGroups: List<TopGroup> = emptyList(),
    @SerializedName("max_group_qty") val maxGroupQty: Double = 0.0
)

data class DashboardFilter(
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("date_label") val dateLabel: String? = null,
    @SerializedName("selected_outlet_id") val selectedOutletId: Int? = null,
    @SerializedName("active_outlet_name") val activeOutletName: String? = null,
    @SerializedName("is_outlet_locked") val isOutletLocked: Boolean = false
)

data class DashboardSummary(
    @SerializedName("pelanggan_baru") val pelangganBaru: Int = 0,
    @SerializedName("pesanan") val pesanan: Int = 0,
    @SerializedName("pendapatan") val pendapatan: Double = 0.0,
    @SerializedName("pengeluaran") val pengeluaran: Double = 0.0,
    @SerializedName("pengembalian") val pengembalian: Double = 0.0,
    @SerializedName("rata_rata_penjualan") val rataRataPenjualan: Double = 0.0
)

data class TopGroup(
    @SerializedName("category_name") val categoryName: String = "",
    @SerializedName("total_qty") val totalQty: Double = 0.0,
    @SerializedName("percentage") val percentage: Double = 0.0,
    @SerializedName("products") val products: List<TopGroupProduct> = emptyList()
)

data class TopGroupProduct(
    @SerializedName("product_name") val productName: String = "",
    @SerializedName("total_qty") val totalQty: Double = 0.0
)
