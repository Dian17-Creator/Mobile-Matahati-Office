package id.my.matahati.backoffice.ui.inventory

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.InventoryDetailData
import id.my.matahati.backoffice.model.InventoryItemDto
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import id.my.matahati.backoffice.ui.dashboard.DatePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class InventoryTab(val apiKey: String, val label: String) {
    STOK_MASUK("stok_masuk", "Stok Masuk"),
    STOK_KELUAR("stok_keluar", "Stok Keluar"),
    PRODUKSI_STOK("produksi_stok", "Produksi Stok"),
    STOK_OPNAME("stok_opname", "Stok Opname"),
    PERGERAKAN_STOK("pergerakan_stok", "Pergerakan Stok")
}

data class InventoryUiState(
    val selectedTab: InventoryTab = InventoryTab.STOK_MASUK,
    val selectedPreset: DatePreset = DatePreset.HARI_INI,
    val startDate: String = "",
    val endDate: String = "",
    val selectedOutletId: Int = 0,
    val items: List<InventoryItemDto> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val perPage: Int = 50,
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,

    // Detail State
    val isDetailOpen: Boolean = false,
    val selectedDetailNo: String? = null,
    val selectedDetailType: String = "stock_in",
    val selectedInventoryDetail: InventoryDetailData? = null,
    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null
)

class InventoryViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        val selectedId = repository.getSelectedOutletId()
        val (start, end) = calculatePresetDates(DatePreset.HARI_INI)
        _uiState.update {
            it.copy(
                selectedOutletId = selectedId,
                selectedPreset = DatePreset.HARI_INI,
                startDate = start,
                endDate = end
            )
        }
        fetchInventory(page = 1)
    }

    fun selectTab(tab: InventoryTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab, currentPage = 1, isDetailOpen = false) }
        fetchInventory(page = 1)
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
                endDate = end,
                currentPage = 1,
                isDetailOpen = false
            )
        }
        fetchInventory(page = 1)
    }

    fun selectPage(page: Int) {
        val state = _uiState.value
        if (page < 1 || page > state.lastPage || page == state.currentPage) return
        _uiState.update { it.copy(currentPage = page) }
        fetchInventory(page = page)
    }

    fun updateOutlet(outletId: Int) {
        _uiState.update { it.copy(selectedOutletId = outletId, currentPage = 1, isDetailOpen = false) }
        fetchInventory(page = 1)
    }

    fun refresh() {
        fetchInventory(page = _uiState.value.currentPage)
    }

    fun openDetail(type: String, no: String) {
        _uiState.update {
            it.copy(
                isDetailOpen = true,
                selectedDetailType = type,
                selectedDetailNo = no,
                isDetailLoading = true,
                detailErrorMessage = null,
                selectedInventoryDetail = null
            )
        }

        viewModelScope.launch {
            val result = repository.getInventoryDetail(type, no)
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        selectedInventoryDetail = response.data,
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
                            detailErrorMessage = exception.message ?: "Gagal memuat detail transaksi"
                        )
                    }
                }
            }
        }
    }

    fun closeDetail() {
        _uiState.update {
            it.copy(
                isDetailOpen = false,
                selectedDetailNo = null,
                selectedInventoryDetail = null
            )
        }
    }

    fun exportPdf(context: Context) {
        val state = _uiState.value
        val no = state.selectedDetailNo ?: return
        val type = state.selectedDetailType

        val prefix = when (type) {
            "stock_out", "stok_keluar" -> "Stok_Keluar"
            "produksi_stok", "production" -> "Produksi_Stok"
            "stok_opname", "opname" -> "Stok_Opname"
            else -> "Stok_Masuk"
        }
        val fileName = "${prefix}_#${no}.pdf"

        Toast.makeText(context, "Mengunduh PDF $fileName...", Toast.LENGTH_SHORT).show()

        viewModelScope.launch {
            val result = repository.downloadInventoryPdf(type, no)
            result.onSuccess { responseBody ->
                val saved = savePdfResponseBodyToDownloads(context, responseBody, fileName)
                if (saved) {
                    Log.d("InventoryPdf", "File $fileName berhasil diunduh dan disimpan ke Downloads")
                    Toast.makeText(context, "File $fileName berhasil disimpan di folder Downloads!", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("InventoryPdf", "Gagal menyimpan file $fileName ke folder Downloads")
                    Toast.makeText(context, "Gagal menyimpan $fileName ke folder Downloads.", Toast.LENGTH_LONG).show()
                }
            }.onFailure { exception ->
                Log.e("InventoryPdf", "Gagal mengunduh file PDF: ${exception.message}", exception)
                Toast.makeText(context, "Gagal mengunduh PDF: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePdfResponseBodyToDownloads(context: Context, body: ResponseBody, fileName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri == null) {
                    Log.e("InventoryPdf", "MediaStore resolver.insert mengembalikan null URI")
                    return false
                }

                resolver.openOutputStream(uri)?.use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } else {
                @Suppress("DEPRECATION")
                val targetFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                targetFile.outputStream().use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            }
        } catch (e: Exception) {
            Log.e("InventoryPdf", "Exception saat menyimpan file $fileName", e)
            false
        }
    }

    private fun fetchInventory(page: Int = _uiState.value.currentPage) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getInventory(
                tab = state.selectedTab.apiKey,
                startDate = state.startDate.ifEmpty { null },
                endDate = state.endDate.ifEmpty { null },
                nidOutlet = state.selectedOutletId,
                page = page,
                perPage = state.perPage
            )

            result.onSuccess { response ->
                val data = response.data
                val paginator = data?.inventory
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = paginator?.data ?: emptyList(),
                        currentPage = paginator?.currentPage ?: 1,
                        lastPage = paginator?.lastPage ?: 1,
                        perPage = paginator?.perPage ?: 50,
                        totalResults = paginator?.total ?: (data?.totalRecords ?: 0),
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
                            errorMessage = exception.message ?: "Gagal memuat data inventori"
                        )
                    }
                }
            }
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
            return InventoryViewModel(repository) as T
        }
    }
}
