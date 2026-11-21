package id.my.matahati.backoffice.model

import com.google.gson.annotations.SerializedName

// --- Product List Response ---
data class ProductListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ProductListData? = null,
    @SerializedName("message") val message: String? = null
)

data class ProductListData(
    @SerializedName("total_records") val totalRecords: Int = 0,
    @SerializedName("categories_options") val categoriesOptions: List<CategoryOptionDto> = emptyList(),
    @SerializedName("products") val products: ProductPaginator? = null
)

data class ProductPaginator(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 50,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("data") val data: List<ProductItemDto> = emptyList()
)

data class ProductItemDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cname") val cname: String = "-",
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("category_name") val categoryName: String = "-",
    @SerializedName("outlet_name") val outletName: String = "-",
    @SerializedName("nprice") val nprice: Double = 0.0,
    @SerializedName("nprice_formatted") val npriceFormatted: String = "Rp 0",
    @SerializedName("nprice_online") val npriceOnline: Double = 0.0,
    @SerializedName("nprice_online_formatted") val npriceOnlineFormatted: String = "Rp 0",
    @SerializedName("nprice_buy") val npriceBuy: Double = 0.0,
    @SerializedName("nprice_buy_formatted") val npriceBuyFormatted: String = "Rp 0",
    @SerializedName("nqty") val nqty: Int = 0,
    @SerializedName("csatuan") val csatuan: String = "PCS",
    @SerializedName("cstatus") val cstatus: String = "ACTIVE"
)

data class CategoryOptionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cname") val cname: String = "Semua Kategori"
)

// --- Category List Response & Form ---
data class CategoryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CategoryListData? = null,
    @SerializedName("message") val message: String? = null
)

data class CategoryListData(
    @SerializedName("total_records") val totalRecords: Int = 0,
    @SerializedName("outlets") val outlets: List<OutletOptionDto> = emptyList(),
    @SerializedName("categories") val categories: List<CategoryItemDto> = emptyList()
)

data class CategoryItemDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cname") val cname: String = "-",
    @SerializedName("product_count") val productCount: Int = 0,
    @SerializedName("ftrack_stock") val ftrackStock: Boolean = true,
    @SerializedName("fshow_pos") val fshowPos: Boolean = true,
    @SerializedName("nid_outlet") val nidOutlet: Int? = null,
    @SerializedName("outlet_name") val outletName: String = "Semua Outlet",
    @SerializedName("badge_label") val badgeLabel: String? = null
)

data class OutletOptionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cname") val cname: String = "Semua Outlet"
)

data class CreateCategoryRequest(
    @SerializedName("cname") val cname: String,
    @SerializedName("nid_outlet") val nidOutlet: Int? = null,
    @SerializedName("ftrack_stock") val ftrackStock: Boolean = true,
    @SerializedName("fshow_pos") val fshowPos: Boolean = true
)
