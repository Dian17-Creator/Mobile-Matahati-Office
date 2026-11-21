package id.my.matahati.backoffice.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.CategoryItemDto
import id.my.matahati.backoffice.model.CategoryOptionDto
import id.my.matahati.backoffice.model.CreateCategoryRequest
import id.my.matahati.backoffice.model.OutletOptionDto
import id.my.matahati.backoffice.model.ProductItemDto
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductSubTab(val apiKey: String, val label: String) {
    PRODUK("produk", "Produk"),
    KATEGORI("kategori", "Kategori")
}

data class ProductUiState(
    val selectedTab: ProductSubTab = ProductSubTab.PRODUK,
    val selectedCategoryId: Int? = null,
    val searchQuery: String = "",
    val hideEmptyStock: Boolean = false,
    val selectedOutletId: Int = 0,
    val products: List<ProductItemDto> = emptyList(),
    val categoriesOptions: List<CategoryOptionDto> = emptyList(),
    val categoriesList: List<CategoryItemDto> = emptyList(),
    val outletsOptions: List<OutletOptionDto> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val perPage: Int = 50,
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,

    // Form Tambah Kategori State
    val newCatName: String = "",
    val newCatOutletId: Int? = null,
    val newCatTrackStock: Boolean = true,
    val newCatShowPos: Boolean = true,
    val isSavingCategory: Boolean = false,
    val saveCategorySuccessMessage: String? = null,
    val saveCategoryErrorMessage: String? = null
)

class ProductViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val selectedId = repository.getSelectedOutletId()
        _uiState.update { it.copy(selectedOutletId = selectedId) }
        loadData()
    }

    fun selectTab(tab: ProductSubTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update {
            it.copy(
                selectedTab = tab,
                currentPage = 1,
                saveCategorySuccessMessage = null,
                saveCategoryErrorMessage = null
            )
        }
        loadData()
    }

    fun selectCategoryFilter(catId: Int?) {
        _uiState.update { it.copy(selectedCategoryId = catId, currentPage = 1) }
        fetchProducts(page = 1)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 1) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            if (_uiState.value.selectedTab == ProductSubTab.PRODUK) {
                fetchProducts(page = 1)
            } else {
                fetchCategories()
            }
        }
    }

    fun toggleHideEmptyStock(hide: Boolean) {
        _uiState.update { it.copy(hideEmptyStock = hide, currentPage = 1) }
        fetchProducts(page = 1)
    }

    fun selectPage(page: Int) {
        val state = _uiState.value
        if (page < 1 || page > state.lastPage || page == state.currentPage) return
        _uiState.update { it.copy(currentPage = page) }
        fetchProducts(page = page)
    }

    fun updateOutlet(outletId: Int) {
        _uiState.update { it.copy(selectedOutletId = outletId, currentPage = 1) }
        loadData()
    }

    fun refresh() {
        loadData()
    }

    // Form Handlers untuk Tambah Kategori
    fun onNewCatNameChange(name: String) {
        _uiState.update { it.copy(newCatName = name, saveCategoryErrorMessage = null) }
    }

    fun onNewCatOutletChange(outletId: Int?) {
        _uiState.update { it.copy(newCatOutletId = outletId) }
    }

    fun onNewCatTrackStockChange(track: Boolean) {
        _uiState.update { it.copy(newCatTrackStock = track) }
    }

    fun onNewCatShowPosChange(show: Boolean) {
        _uiState.update { it.copy(newCatShowPos = show) }
    }

    fun submitCreateCategory() {
        val state = _uiState.value
        if (state.newCatName.isBlank()) {
            _uiState.update { it.copy(saveCategoryErrorMessage = "Nama kategori wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isSavingCategory = true, saveCategorySuccessMessage = null, saveCategoryErrorMessage = null) }

        viewModelScope.launch {
            val req = CreateCategoryRequest(
                cname = state.newCatName.trim(),
                nidOutlet = state.newCatOutletId,
                ftrackStock = state.newCatTrackStock,
                fshowPos = state.newCatShowPos
            )

            val result = repository.storeCategory(req)

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isSavingCategory = false,
                        newCatName = "",
                        saveCategorySuccessMessage = response.message ?: "Kategori berhasil ditambahkan.",
                        saveCategoryErrorMessage = null
                    )
                }
                fetchCategories()
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update { it.copy(isSavingCategory = false, isUnauthorized = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isSavingCategory = false,
                            saveCategoryErrorMessage = exception.message ?: "Gagal menambahkan kategori."
                        )
                    }
                }
            }
        }
    }

    private fun loadData() {
        if (_uiState.value.selectedTab == ProductSubTab.PRODUK) {
            fetchProducts(page = _uiState.value.currentPage)
        } else {
            fetchCategories()
        }
    }

    private fun fetchProducts(page: Int = _uiState.value.currentPage) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getProducts(
                nidCategory = state.selectedCategoryId,
                search = state.searchQuery,
                hideEmpty = state.hideEmptyStock,
                nidOutlet = state.selectedOutletId,
                page = page,
                perPage = state.perPage
            )

            result.onSuccess { response ->
                val data = response.data
                val paginator = data?.products
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categoriesOptions = data?.categoriesOptions ?: emptyList(),
                        products = paginator?.data ?: emptyList(),
                        currentPage = paginator?.currentPage ?: 1,
                        lastPage = paginator?.lastPage ?: 1,
                        perPage = paginator?.perPage ?: 50,
                        totalResults = paginator?.total ?: (data?.totalRecords ?: 0),
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update { it.copy(isLoading = false, isUnauthorized = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gagal memuat produk"
                        )
                    }
                }
            }
        }
    }

    private fun fetchCategories() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getCategories(
                search = state.searchQuery,
                nidOutlet = state.selectedOutletId
            )

            result.onSuccess { response ->
                val data = response.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        outletsOptions = data?.outlets ?: emptyList(),
                        categoriesList = data?.categories ?: emptyList(),
                        totalResults = data?.totalRecords ?: 0,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update { it.copy(isLoading = false, isUnauthorized = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gagal memuat kategori"
                        )
                    }
                }
            }
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(repository) as T
        }
    }
}
