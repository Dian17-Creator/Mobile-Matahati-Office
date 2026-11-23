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

// --- Inventory Detail Models ---
data class InventoryDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: InventoryDetailData? = null,
    @SerializedName("message") val message: String? = null
)

data class InventoryDetailData(
    @SerializedName("type") val type: String = "",
    @SerializedName("cno_ref") val cnoRef: String = "-",
    @SerializedName("ctype") val ctype: String = "IN",
    @SerializedName("dmutation") val dmutation: String = "-",
    @SerializedName("dmutation_full") val dmutationFull: String = "-",
    @SerializedName("diterima_dari") val diterimaDari: String = "-",
    @SerializedName("transfer_ke") val transferKe: String = "-",
    @SerializedName("outlet_name") val outletName: String = "Matahati POS",
    @SerializedName("user_email") val userEmail: String = "",
    @SerializedName("creason") val creason: String = "-",
    @SerializedName("cnote") val cnote: String = "-",
    @SerializedName("cstatus") val cstatus: String = "DRAFT",
    @SerializedName("status_label") val statusLabel: String = "Draf",
    @SerializedName("posted_by") val postedBy: String = "-",
    @SerializedName("total_qty") val totalQty: Int = 0,
    @SerializedName("total_cost") val totalCost: Double = 0.0,
    @SerializedName("total_cost_formatted") val totalCostFormatted: String = "Rp 0",
    @SerializedName("total_product_qty") val totalProductQty: Double = 0.0,
    @SerializedName("total_product_qty_formatted") val totalProductQtyFormatted: String = "0",
    @SerializedName("total_items_count") val totalItemsCount: Int = 0,
    @SerializedName("total_system_qty") val totalSystemQty: Int = 0,
    @SerializedName("total_actual_qty") val totalActualQty: Int = 0,
    @SerializedName("total_diff_qty") val totalDiffQty: Int = 0,
    @SerializedName("products") val products: List<ProductionProductDto> = emptyList(),
    @SerializedName("materials") val materials: List<ProductionMaterialDto> = emptyList(),
    @SerializedName("details") val details: List<InventoryDetailItemDto> = emptyList()
)

data class ProductionProductDto(
    @SerializedName("no") val no: Int = 1,
    @SerializedName("product_name") val productName: String = "-",
    @SerializedName("qty") val qty: Double = 0.0,
    @SerializedName("qty_formatted") val qtyFormatted: String = "0",
    @SerializedName("unit") val unit: String = "GRAM",
    @SerializedName("materials_count") val materialsCount: Int = 0,
    @SerializedName("materials") val materials: List<ProductionMaterialDto> = emptyList()
)

data class ProductionMaterialDto(
    @SerializedName("no") val no: Int = 1,
    @SerializedName("item_name") val itemName: String = "-",
    @SerializedName("tipe") val tipe: String = "BAHAN BAKU",
    @SerializedName("cost") val cost: Double = 0.0,
    @SerializedName("cost_formatted") val costFormatted: String = "Rp 0",
    @SerializedName("qty_deducted") val qtyDeducted: Double = 0.0,
    @SerializedName("qty_deducted_formatted") val qtyDeductedFormatted: String = "-0",
    @SerializedName("unit") val unit: String = "GRAM",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("subtotal_formatted") val subtotalFormatted: String = "Rp 0"
)

data class InventoryDetailItemDto(
    @SerializedName("no") val no: Int = 1,
    @SerializedName("tipe") val tipe: String = "Produk",
    @SerializedName("item_name") val itemName: String = "-",
    @SerializedName("product_name") val productName: String = "",
    @SerializedName("item_type") val itemType: String = "",
    @SerializedName("unit") val unit: String = "",
    @SerializedName("category_unit_line") val categoryUnitLine: String = "",
    @SerializedName("category_name") val categoryName: String = "PRODUK",
    @SerializedName("current_stock") val currentStock: Int = 0,
    @SerializedName("qty") val qty: Int = 0,
    @SerializedName("qty_system") val qtySystem: Int = 0,
    @SerializedName("qty_actual") val qtyActual: Int = 0,
    @SerializedName("qty_diff") val qtyDiff: Int = 0,
    @SerializedName("qty_diff_formatted") val qtyDiffFormatted: String = "0",
    @SerializedName("cost") val cost: Double = 0.0,
    @SerializedName("cost_formatted") val costFormatted: String = "Rp 0",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("subtotal_formatted") val subtotalFormatted: String = "Rp 0"
)
