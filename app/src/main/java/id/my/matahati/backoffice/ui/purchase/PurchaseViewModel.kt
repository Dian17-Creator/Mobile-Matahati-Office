package id.my.matahati.backoffice.ui.purchase

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
import id.my.matahati.backoffice.model.PurchaseCounts
import id.my.matahati.backoffice.model.PurchaseDetailData
import id.my.matahati.backoffice.model.PurchaseItemDto
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import id.my.matahati.backoffice.ui.dashboard.DatePreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

enum class PurchaseTab(val apiKey: String, val label: String) {
    BUTUH_DIPROSES("pending", "Butuh Diproses"),
    TELAH_DIPROSES("received", "Telah Diproses"),
    RETUR("return", "Retur"),
    DIBATALKAN("cancelled", "Dibatalkan")
}

data class PurchaseUiState(
    val selectedTab: PurchaseTab = PurchaseTab.BUTUH_DIPROSES,
    val searchQuery: String = "",
    val selectedPreset: DatePreset = DatePreset.HARI_INI,
    val startDate: String = "",
    val endDate: String = "",
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
        fetchPurchases()
    }

    fun selectTab(tab: PurchaseTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab, isDetailOpen = false) }
        fetchPurchases()
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

    fun updateDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        fetchPurchases()
    }

    fun fetchPurchases() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getPurchases(
                tab = state.selectedTab.apiKey,
                startDate = state.startDate.ifEmpty { null },
                endDate = state.endDate.ifEmpty { null },
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

    fun exportPdf(context: Context) {
        val detail = _uiState.value.selectedPurchaseDetail ?: return
        val id = detail.id
        val noPo = detail.cnoPurchase.ifEmpty { "PO_$id" }
        val fileName = "Purchase_Order_${noPo.replace("/", "_")}.pdf"

        Toast.makeText(context, "Mengunduh PDF $fileName...", Toast.LENGTH_SHORT).show()

        viewModelScope.launch {
            val result = repository.downloadPurchasePdf(id)
            result.onSuccess { responseBody ->
                val saved = savePdfResponseBodyToDownloads(context, responseBody, fileName)
                if (saved) {
                    Log.d("PurchasePdf", "File $fileName berhasil diunduh dan disimpan ke Downloads")
                    Toast.makeText(context, "File $fileName berhasil disimpan di folder Downloads!", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("PurchasePdf", "Gagal menyimpan file $fileName ke folder Downloads")
                    Toast.makeText(context, "Gagal menyimpan $fileName ke folder Downloads.", Toast.LENGTH_LONG).show()
                }
            }.onFailure { exception ->
                Log.e("PurchasePdf", "Gagal mengunduh file PDF: ${exception.message}", exception)
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
                    Log.e("PurchasePdf", "MediaStore resolver.insert mengembalikan null URI")
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
            Log.e("PurchasePdf", "Exception saat menyimpan file $fileName", e)
            false
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
            return PurchaseViewModel(repository) as T
        }
    }
}
