package id.my.matahati.backoffice.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.PurchaseCounts
import id.my.matahati.backoffice.model.PurchaseDetailData
import id.my.matahati.backoffice.model.PurchaseItemDto
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PurchaseTab(val apiKey: String, val label: String) {
    BUTUH_DIPROSES("pending", "Butuh Diproses"),
    TELAH_DIPROSES("received", "Telah Diproses"),
    RETUR("return", "Retur"),
    DIBATALKAN("cancelled", "Dibatalkan")
}

data class PurchaseUiState(
    val selectedTab: PurchaseTab = PurchaseTab.BUTUH_DIPROSES,
    val searchQuery: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val selectedOutletId: Int = 0,
    val counts: PurchaseCounts = PurchaseCounts(),
    val purchases: List<PurchaseItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,

    // Detail State
    val isDetailOpen: Boolean = false,
    val isDetailLoading: Boolean = false,
    val selectedPurchaseDetail: PurchaseDetailData? = null,
    val detailErrorMessage: String? = null
)

class PurchaseViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val selectedId = repository.getSelectedOutletId()
        _uiState.update { it.copy(selectedOutletId = selectedId) }
        fetchPurchases()
    }

    fun selectTab(tab: PurchaseTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab, isDetailOpen = false) }
        fetchPurchases()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            fetchPurchases()
        }
    }

    fun updateOutlet(outletId: Int) {
        _uiState.update { it.copy(selectedOutletId = outletId) }
        fetchPurchases()
    }

    fun updateDateRange(start: String?, end: String?) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        fetchPurchases()
    }

    fun fetchPurchases() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getPurchases(
                tab = state.selectedTab.apiKey,
                startDate = state.startDate,
                endDate = state.endDate,
                nidOutlet = state.selectedOutletId,
                search = state.searchQuery
            )

            result.onSuccess { response ->
                val data = response.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        counts = data?.counts ?: PurchaseCounts(),
                        purchases = data?.purchases?.data ?: emptyList(),
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isUnauthorized = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gagal memuat daftar pembelian"
                        )
                    }
                }
            }
        }
    }

    fun openDetail(id: Int) {
        _uiState.update {
            it.copy(
                isDetailOpen = true,
                isDetailLoading = true,
                detailErrorMessage = null,
                selectedPurchaseDetail = null
            )
        }

        viewModelScope.launch {
            val result = repository.getPurchaseDetail(id)
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        selectedPurchaseDetail = response.data,
                        detailErrorMessage = null
                    )
                }
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            isUnauthorized = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            detailErrorMessage = exception.message ?: "Gagal memuat detail pembelian"
                        )
                    }
                }
            }
        }
    }

    fun closeDetail() {
        _uiState.update { it.copy(isDetailOpen = false, selectedPurchaseDetail = null) }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PurchaseViewModel(repository) as T
        }
    }
}
