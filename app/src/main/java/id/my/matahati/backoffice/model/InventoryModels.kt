package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: InventoryData? = null,
    @SerializedName("message") val message: String? = null
)

data class InventoryData(
    @SerializedName("tab") val tab: String = "stok_masuk",
    @SerializedName("total_records") val totalRecords: Int = 0,
    @SerializedName("inventory") val inventory: InventoryPaginator? = null
)

data class InventoryPaginator(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 50,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("data") val data: List<InventoryItemDto> = emptyList()
)

data class InventoryItemDto(
    @SerializedName("no") val no: String? = "-",
    @SerializedName("diterima_dari") val diterimaDari: String? = "-",
    @SerializedName("supplier") val supplier: String? = "-",
    @SerializedName("tanggal") val tanggal: String? = "-",
    @SerializedName("catatan") val catatan: String? = "-",
    @SerializedName("status") val status: String? = "DRAFT",
    @SerializedName("status_label") val statusLabel: String? = "Draf",
    @SerializedName("diterima_oleh") val diterimaOleh: String? = "-",
    @SerializedName("transfer_ke") val transferKe: String? = "-",
    @SerializedName("alasan") val alasan: String? = "-",
    @SerializedName("dibuat_oleh") val dibuatOleh: String? = "-",
    @SerializedName("diterima_dibuat_oleh") val diterimaDibuatOleh: String? = "-",
    @SerializedName("category_name") val categoryName: String? = "-",
    @SerializedName("cname") val cname: String? = "-",
    @SerializedName("qty_awal") val qtyAwal: Int = 0,
    @SerializedName("qty_masuk") val qtyMasuk: Int = 0,
    @SerializedName("qty_pengembalian") val qtyPengembalian: Int = 0,
    @SerializedName("qty_penjualan") val qtyPenjualan: Int = 0,
    @SerializedName("qty_keluar") val qtyKeluar: Int = 0,
    @SerializedName("qty_sisa") val qtySisa: Int = 0
)
