package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

data class PurchaseListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PurchaseListData? = null
)

data class PurchaseListData(
    @SerializedName("tab") val tab: String = "pending",
    @SerializedName("counts") val counts: PurchaseCounts? = null,
    @SerializedName("purchases") val purchases: PurchasePaginator? = null
)

data class PurchaseCounts(
    @SerializedName("pending") val pending: Int = 0,
    @SerializedName("received") val received: Int = 0,
    @SerializedName("return") val returnCount: Int = 0,
    @SerializedName("cancelled") val cancelled: Int = 0
)

data class PurchasePaginator(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 15,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("data") val data: List<PurchaseItemDto> = emptyList()
)

data class PurchaseItemDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cno_purchase") val cnoPurchase: String = "",
    @SerializedName("cno_reference") val cnoReference: String = "-",
    @SerializedName("dpurchase") val dpurchase: String? = null,
    @SerializedName("dpurchase_formatted") val dpurchaseFormatted: String = "-",
    @SerializedName("supplier_name") val supplierName: String = "-",
    @SerializedName("outlet_name") val outletName: String = "-",
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    @SerializedName("total_amount_formatted") val totalAmountFormatted: String = "Rp 0",
    @SerializedName("items_count") val itemsCount: Int = 0,
    @SerializedName("payment_status") val paymentStatus: String = "UNPAID",
    @SerializedName("payment_status_label") val paymentStatusLabel: String = "Belum",
    @SerializedName("dpayment") val dpayment: String? = null,
    @SerializedName("dpayment_formatted") val dpaymentFormatted: String = "-",
    @SerializedName("cstatus") val cstatus: String = "PENDING",
    @SerializedName("cstatus_label") val cstatusLabel: String = "Proses"
)

data class PurchaseDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PurchaseDetailData? = null
)

data class PurchaseDetailData(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cno_purchase") val cnoPurchase: String = "",
    @SerializedName("cno_reference") val cnoReference: String = "-",
    @SerializedName("cdoc_type") val cdocType: String = "PURCHASE",
    @SerializedName("cstatus") val cstatus: String = "PENDING",
    @SerializedName("status_label") val statusLabel: String = "Proses",
    @SerializedName("payment_status") val paymentStatus: String = "UNPAID",
    @SerializedName("payment_status_label") val paymentStatusLabel: String = "Belum Bayar",
    @SerializedName("created_by") val createdBy: String = "Admin",
    @SerializedName("dpurchase_formatted") val dpurchaseFormatted: String = "-",
    @SerializedName("outlet_name") val outletName: String = "-",
    @SerializedName("supplier") val supplier: SupplierInfoDto? = null,
    @SerializedName("receipt_info") val receiptInfo: ReceiptInfoDto? = null,
    @SerializedName("note") val note: String = "-",
    @SerializedName("due_date") val dueDate: DueDateInfoDto? = null,
    @SerializedName("items_count") val itemsCount: Int = 0,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    @SerializedName("total_amount_formatted") val totalAmountFormatted: String = "Rp 0",
    @SerializedName("items") val items: List<PurchaseDetailItemDto> = emptyList()
)

data class SupplierInfoDto(
    @SerializedName("name") val name: String = "-",
    @SerializedName("pic_contact") val picContact: String = "-",
    @SerializedName("phone_email") val phoneEmail: String = "-",
    @SerializedName("address") val address: String = "-"
)

data class ReceiptInfoDto(
    @SerializedName("dreceived_formatted") val dreceivedFormatted: String = "-",
    @SerializedName("no_receipt_sj") val noReceiptSj: String = "-",
    @SerializedName("received_by") val receivedBy: String = "-",
    @SerializedName("stock_added") val stockAdded: Boolean = false
)

data class DueDateInfoDto(
    @SerializedName("due_date_formatted") val dueDateFormatted: String = "Tidak ditentukan"
)

data class PurchaseDetailItemDto(
    @SerializedName("no") val no: Int = 1,
    @SerializedName("product_name") val productName: String = "-",
    @SerializedName("category_unit") val categoryUnit: String = "-",
    @SerializedName("item_type") val itemType: String = "Produk",
    @SerializedName("current_stock") val currentStock: String = "0 PCS",
    @SerializedName("qty_buy") val qtyBuy: String = "+0 PCS",
    @SerializedName("buy_price") val buyPrice: Double = 0.0,
    @SerializedName("buy_price_formatted") val buyPriceFormatted: String = "Rp 0",
    @SerializedName("total_price") val totalPrice: Double = 0.0,
    @SerializedName("total_price_formatted") val totalPriceFormatted: String = "Rp 0"
)
