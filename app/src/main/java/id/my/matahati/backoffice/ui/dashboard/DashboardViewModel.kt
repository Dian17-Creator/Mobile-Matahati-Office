package id.my.matahati.backoffice.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.DashboardData
import id.my.matahati.backoffice.model.Outlet
import id.my.matahati.backoffice.model.User
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class DashboardMenu(val title: String) {
    DASHBOARD("Dashboard"),
    PRODUK("Produk"),
    INVENTORI("Inventori"),
    PENJUALAN("Penjualan"),
    PEMBELIAN("Pembelian"),
    LAPORAN("Laporan"),
    PELANGGAN_SUPPLIER("Pelanggan & Supplier")
}

enum class DatePreset(val label: String) {
    HARI_INI("Hari Ini"),
    KEMARIN("Kemarin"),
    TUJUH_HARI_LALU("7 Hari Terakhir"),
    BULAN_INI("Bulan Ini"),
    BULAN_LALU("Bulan Lalu"),
    CUSTOM("Custom")
}

data class DashboardUiState(
    val user: User? = null,
    val selectedMenu: DashboardMenu = DashboardMenu.DASHBOARD,
    val selectedOutletId: Int = 0,
    val selectedOutletName: String = "Semua Outlet",
    val outlets: List<Outlet> = emptyList(),
    val isOutletLocked: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,

    // Real-data dashboard states
    val isSidebarVisible: Boolean = true,
    val isLoading: Boolean = false,
    val dashboardData: DashboardData? = null,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,
    val selectedPreset: DatePreset = DatePreset.HARI_INI,
    val startDate: String = "",
    val endDate: String = "",
    val expandedCategoryNames: Set<String> = emptySet()
)

class DashboardViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val user = repository.getSavedUser()
        val selectedId = repository.getSelectedOutletId()
        val selectedName = repository.getSelectedOutletName()

        val (start, end) = calculatePresetDates(DatePreset.HARI_INI)

        _uiState.update {
            it.copy(
                user = user,
                selectedOutletId = selectedId,
                selectedOutletName = selectedName,
                outlets = user?.outlets ?: emptyList(),
                isOutletLocked = user?.isOutletLocked ?: false,
                selectedPreset = DatePreset.HARI_INI,
                startDate = start,
                endDate = end
            )
        }

        fetchDashboardData()
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }
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
        fetchDashboardData()
    }

    fun selectDatePreset(preset: DatePreset, customStart: String? = null, customEnd: String? = null) {
        val (start, end) = if (preset == DatePreset.CUSTOM && !customStart.isNullOrEmpty() && !customEnd.isNullOrEmpty()) {
            Pair(customStart, customEnd)
        } else {
            calculatePresetDates(preset)
        }

        _uiState.update {
            it.copy(
                selectedPreset = preset,
                startDate = start,
                endDate = end
            )
        }
        fetchDashboardData()
    }

    fun toggleCategoryExpand(categoryName: String) {
        _uiState.update { state ->
            val current = state.expandedCategoryNames.toMutableSet()
            if (current.contains(categoryName)) {
                current.remove(categoryName)
            } else {
                current.add(categoryName)
            }
            state.copy(expandedCategoryNames = current)
        }
    }

    fun refreshDashboardData() {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getDashboard(
                startDate = currentState.startDate.ifEmpty { null },
                endDate = currentState.endDate.ifEmpty { null },
                nidOutlet = currentState.selectedOutletId
            )

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dashboardData = response.data,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                if (exception is UnauthorizedException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isUnauthorized = true,
                            isLoggedOut = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gagal memuat data dashboard"
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggingOut = false, isLoggedOut = true) }
        }
    }

    private fun calculatePresetDates(preset: DatePreset): Pair<String, String> {
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        return when (preset) {
            DatePreset.HARI_INI -> Pair(todayStr, todayStr)
            DatePreset.KEMARIN -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(cal.time)
                Pair(yesterdayStr, yesterdayStr)
            }
            DatePreset.TUJUH_HARI_LALU -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                val startStr = dateFormat.format(cal.time)
                Pair(startStr, todayStr)
            }
            DatePreset.BULAN_INI -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startStr = dateFormat.format(cal.time)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                val endStr = dateFormat.format(cal.time)
                Pair(startStr, endStr)
            }
            DatePreset.BULAN_LALU -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startStr = dateFormat.format(cal.time)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                val endStr = dateFormat.format(cal.time)
                Pair(startStr, endStr)
            }
            DatePreset.CUSTOM -> Pair(todayStr, todayStr)
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
