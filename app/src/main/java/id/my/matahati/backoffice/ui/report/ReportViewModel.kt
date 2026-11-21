package id.my.matahati.backoffice.ui.report

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.my.matahati.backoffice.model.ReportItemDto
import id.my.matahati.backoffice.model.ReportSummaryDto
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.repository.UnauthorizedException
import id.my.matahati.backoffice.ui.dashboard.DatePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ReportType(val apiKey: String, val label: String) {
    RINCIAN_PENJUALAN("sales_detail", "Rincian Penjualan"),
    ITEM_PENJUALAN("sales_item", "Item Penjualan"),
    LAPORAN_PELANGGAN("customer", "Laporan Pelanggan")
}

data class ReportUiState(
    val selectedType: ReportType = ReportType.RINCIAN_PENJUALAN,
    val selectedPreset: DatePreset = DatePreset.HARI_INI,
    val startDate: String = "",
    val endDate: String = "",
    val selectedOutletId: Int = 0,
    val summary: ReportSummaryDto? = null,
    val reports: List<ReportItemDto> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val perPage: Int = 50,
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false
)

class ReportViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

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
        fetchReports(page = 1)
    }

    fun selectType(type: ReportType) {
        if (_uiState.value.selectedType == type) return
        _uiState.update { it.copy(selectedType = type, currentPage = 1) }
        fetchReports(page = 1)
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
                currentPage = 1
            )
        }
        fetchReports(page = 1)
    }

    fun selectPage(page: Int) {
        val state = _uiState.value
        if (page < 1 || page > state.lastPage || page == state.currentPage) return
        _uiState.update { it.copy(currentPage = page) }
        fetchReports(page = page)
    }

    fun updateOutlet(outletId: Int) {
        _uiState.update { it.copy(selectedOutletId = outletId, currentPage = 1) }
        fetchReports(page = 1)
    }

    fun refresh() {
        fetchReports(page = _uiState.value.currentPage)
    }

    fun exportExcel(context: Context) {
        val state = _uiState.value
        val type = state.selectedType.apiKey
        val startDate = state.startDate.ifEmpty { null }
        val endDate = state.endDate.ifEmpty { null }
        val nidOutlet = if (state.selectedOutletId > 0) state.selectedOutletId else null

        val fileName = when (state.selectedType) {
            ReportType.RINCIAN_PENJUALAN -> "Rincian_Penjualan_${state.startDate}_sd_${state.endDate}.xlsx"
            ReportType.ITEM_PENJUALAN -> "Item_Penjualan_${state.startDate}_sd_${state.endDate}.xlsx"
            ReportType.LAPORAN_PELANGGAN -> "Laporan_Pelanggan_${state.startDate}_sd_${state.endDate}.xlsx"
        }

        Toast.makeText(context, "Mengunduh $fileName...", Toast.LENGTH_SHORT).show()

        viewModelScope.launch {
            val result = repository.downloadReportExcel(type, startDate, endDate, nidOutlet)
            result.onSuccess { responseBody ->
                val saved = saveResponseBodyToDownloads(context, responseBody, fileName)
                if (saved) {
                    Log.d("ReportExport", "File $fileName berhasil diunduh dan disimpan ke Downloads")
                    Toast.makeText(context, "File $fileName berhasil disimpan di folder Downloads!", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("ReportExport", "Gagal menyimpan file $fileName ke folder Downloads")
                    Toast.makeText(context, "Gagal menyimpan $fileName ke folder Downloads.", Toast.LENGTH_LONG).show()
                }
            }.onFailure { exception ->
                Log.e("ReportExport", "Gagal mengunduh file Excel: ${exception.message}", exception)
                Toast.makeText(context, "Gagal mengunduh file Excel: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveResponseBodyToDownloads(context: Context, body: ResponseBody, fileName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri == null) {
                    Log.e("ReportExport", "MediaStore resolver.insert mengembalikan null URI")
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
            Log.e("ReportExport", "Exception saat menyimpan file $fileName", e)
            false
        }
    }

    private fun fetchReports(page: Int = _uiState.value.currentPage) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getReports(
                type = state.selectedType.apiKey,
                startDate = state.startDate.ifEmpty { null },
                endDate = state.endDate.ifEmpty { null },
                nidOutlet = state.selectedOutletId,
                page = page
            )

            result.onSuccess { response ->
                val data = response.data
                val paginator = data?.reports
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = data?.summary,
                        reports = paginator?.data ?: emptyList(),
                        currentPage = paginator?.currentPage ?: 1,
                        lastPage = paginator?.lastPage ?: 1,
                        perPage = paginator?.perPage ?: 50,
                        totalResults = paginator?.total ?: 0,
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
                            errorMessage = exception.message ?: "Gagal memuat laporan"
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
            return ReportViewModel(repository) as T
        }
    }
}
