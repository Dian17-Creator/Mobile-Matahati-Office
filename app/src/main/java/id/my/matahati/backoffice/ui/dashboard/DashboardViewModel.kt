package id.my.matahati.backoffice.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.Outlet
import id.my.matahati.backoffice.model.User
import id.my.matahati.backoffice.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DashboardMenu(val title: String) {
    DASHBOARD("Dashboard"),
    PRODUK("Produk"),
    INVENTORI("Inventori"),
    PENJUALAN("Penjualan"),
    PEMBELIAN("Pembelian"),
    LAPORAN("Laporan"),
    PELANGGAN_SUPPLIER("Pelanggan & Supplier")
}

data class DashboardUiState(
    val user: User? = null,
    val selectedMenu: DashboardMenu = DashboardMenu.DASHBOARD,
    val selectedOutletId: Int = 0,
    val selectedOutletName: String = "Semua Outlet",
    val outlets: List<Outlet> = emptyList(),
    val isOutletLocked: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false
)

class DashboardViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val user = repository.getSavedUser()
        val selectedId = repository.getSelectedOutletId()
        val selectedName = repository.getSelectedOutletName()

        _uiState.update {
            it.copy(
                user = user,
                selectedOutletId = selectedId,
                selectedOutletName = selectedName,
                outlets = user?.outlets ?: emptyList(),
                isOutletLocked = user?.isOutletLocked ?: false
            )
        }
    }

    fun selectMenu(menu: DashboardMenu) {
        _uiState.update { it.copy(selectedMenu = menu) }
    }

    fun selectOutlet(outletId: Int, outletName: String) {
        repository.setSelectedOutlet(outletId, outletName)
        _uiState.update {
            it.copy(
                selectedOutletId = outletId,
                selectedOutletName = outletName
            )
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggingOut = false, isLoggedOut = true) }
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
