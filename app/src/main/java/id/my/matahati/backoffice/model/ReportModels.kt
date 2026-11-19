package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: ReportData? = null
)

data class ReportData(
    @SerializedName("type") val type: String = "sales_detail",
    @SerializedName("summary") val summary: ReportSummaryDto? = null,
    @SerializedName("reports") val reports: ReportPaginator? = null
)

data class ReportSummaryDto(
    @SerializedName("total_penjualan") val totalPenjualan: Double = 0.0,
    @SerializedName("total_penjualan_formatted") val totalPenjualanFormatted: String = "Rp 0",
    @SerializedName("void_refund") val voidRefund: Double = 0.0,
    @SerializedName("void_refund_formatted") val voidRefundFormatted: String = "Rp 0",
    @SerializedName("total_diskon") val totalDiskon: Double = 0.0,
    @SerializedName("total_diskon_formatted") val totalDiskonFormatted: String = "Rp 0",
    @SerializedName("penjualan_nett") val penjualanNett: Double = 0.0,
    @SerializedName("penjualan_nett_formatted") val penjualanNettFormatted: String = "Rp 0",
    @SerializedName("modal_produk") val modalProduk: Double = 0.0,
    @SerializedName("modal_produk_formatted") val modalProdukFormatted: String = "Rp 0",
    @SerializedName("total_laba") val totalLaba: Double = 0.0,
    @SerializedName("total_laba_formatted") val totalLabaFormatted: String = "Rp 0",
    @SerializedName("diskon") val diskon: Double = 0.0,
    @SerializedName("diskon_formatted") val diskonFormatted: String = "Rp 0",
    @SerializedName("laba") val laba: Double = 0.0,
    @SerializedName("laba_formatted") val labaFormatted: String = "Rp 0",
    @SerializedName("jml_transaksi") val jmlTransaksi: Int = 0,
    @SerializedName("total_qty") val totalQty: Int = 0
)

data class ReportPaginator(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 15,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("data") val data: List<ReportItemDto> = emptyList()
)

data class ReportItemDto(
    // Common / Sales Detail fields
    @SerializedName("tanggal_transaksi") val tanggalTransaksi: String = "-",
    @SerializedName("waktu_transaksi") val waktuTransaksi: String = "-",
    @SerializedName("cnotransaction") val cnotransaction: String = "-",
    @SerializedName("payment_method") val paymentMethod: String = "-",
    @SerializedName("status") val status: String = "-",
    @SerializedName("kasir") val kasir: String = "-",
    @SerializedName("pelanggan") val pelanggan: String = "-",
    @SerializedName("total_penjualan") val totalPenjualan: Double = 0.0,
    @SerializedName("total_penjualan_formatted") val totalPenjualanFormatted: String = "Rp 0",

    // Sales Item fields
    @SerializedName("penjualan_oleh") val penjualanOleh: String = "-",
    @SerializedName("item_name") val itemName: String = "-",
    @SerializedName("customer_name") val customerName: String = "-",
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("diskon") val diskon: Double = 0.0,
    @SerializedName("diskon_formatted") val diskonFormatted: String = "Rp 0",

    // Customer Report fields
    @SerializedName("modal_produk") val modalProduk: Double = 0.0,
    @SerializedName("modal_produk_formatted") val modalProdukFormatted: String = "Rp 0",
    @SerializedName("laba") val laba: Double = 0.0,
    @SerializedName("laba_formatted") val labaFormatted: String = "Rp 0",
    @SerializedName("jml_transaksi") val jmlTransaksi: Int = 0
)
