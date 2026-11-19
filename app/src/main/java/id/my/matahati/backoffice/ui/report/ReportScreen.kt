package id.my.matahati.backoffice.ui.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.ReportItemDto
import id.my.matahati.backoffice.model.ReportSummaryDto
import id.my.matahati.backoffice.ui.dashboard.DatePresetFilterDialog
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDateFilterDialog by remember { mutableStateOf(false) }

    if (showDateFilterDialog) {
        DatePresetFilterDialog(
            currentPreset = uiState.selectedPreset,
            currentStart = uiState.startDate,
            currentEnd = uiState.endDate,
            onDismiss = { showDateFilterDialog = false },
            onPresetSelected = { preset, customStart, customEnd ->
                showDateFilterDialog = false
                viewModel.selectDatePreset(preset, customStart, customEnd)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Sub-header Tab Bar (Laporan Penjualan)
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            color = Color.White,
//            shadowElevation = 1.dp
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 24.dp),
//                horizontalArrangement = Arrangement.Start
//            ) {
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp, vertical = 14.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text(
//                            text = "Laporan Penjualan",
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MatahatiMaroon
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Box(
//                            modifier = Modifier
//                                .width(64.dp)
//                                .height(3.dp)
//                                .background(MatahatiMaroon, shape = RoundedCornerShape(2.dp))
//                        )
//                    }
//                }
//            }
//        }

        // Main 2-Column Content Area
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left Card: "Daftar Laporan" (Width 215.dp to keep 1-line text when selected)
            Card(
                modifier = Modifier
                    .width(215.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Daftar Laporan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ReportTypeSelectorItem(
                        title = ReportType.RINCIAN_PENJUALAN.label,
                        icon = Icons.Outlined.Description,
                        isSelected = uiState.selectedType == ReportType.RINCIAN_PENJUALAN,
                        onClick = { viewModel.selectType(ReportType.RINCIAN_PENJUALAN) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ReportTypeSelectorItem(
                        title = ReportType.ITEM_PENJUALAN.label,
                        icon = Icons.Outlined.Inventory2,
                        isSelected = uiState.selectedType == ReportType.ITEM_PENJUALAN,
                        onClick = { viewModel.selectType(ReportType.ITEM_PENJUALAN) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ReportTypeSelectorItem(
                        title = ReportType.LAPORAN_PELANGGAN.label,
                        icon = Icons.Outlined.Person,
                        isSelected = uiState.selectedType == ReportType.LAPORAN_PELANGGAN,
                        onClick = { viewModel.selectType(ReportType.LAPORAN_PELANGGAN) }
                    )
                }
            }

            // Right Panel: Main Report Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header & Date Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.selectedType.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showDateFilterDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Filter Tanggal",
                                    tint = MatahatiMaroon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.startDate.ifEmpty { "Pilih Tanggal" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MatahatiMaroon
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MatahatiMaroon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grand Total Dark Banner
                GrandTotalDarkBanner(
                    type = uiState.selectedType,
                    summary = uiState.summary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Loading & Error States
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MatahatiMaroon)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Memuat data laporan...", color = Color(0xFF64748B), fontSize = 14.sp)
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = uiState.errorMessage ?: "Terjadi kesalahan", color = Color(0xFFDC2626), fontSize = 14.sp)
                            Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon)) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                } else if (uiState.reports.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada data laporan untuk periode tanggal ini.",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Data Table based on selected report sub-type
                    when (uiState.selectedType) {
                        ReportType.RINCIAN_PENJUALAN -> SalesDetailTable(reports = uiState.reports)
                        ReportType.ITEM_PENJUALAN -> SalesItemTable(reports = uiState.reports)
                        ReportType.LAPORAN_PELANGGAN -> CustomerReportTable(reports = uiState.reports)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pagination Controls (Showing X to Y of Z results & Page Buttons)
                    PaginationControls(
                        currentPage = uiState.currentPage,
                        lastPage = uiState.lastPage,
                        perPage = uiState.perPage,
                        totalResults = uiState.totalResults,
                        onPageSelected = { viewModel.selectPage(it) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ReportTypeSelectorItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFFE0F2FE) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF475569)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GrandTotalDarkBanner(
    type: ReportType,
    summary: ReportSummaryDto?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "GRAND TOTAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (type) {
                    ReportType.RINCIAN_PENJUALAN -> {
                        BannerMetricCell("Total Penjualan", summary?.totalPenjualanFormatted ?: "Rp 0", Color(0xFF22C55E))
                        BannerMetricCell("Void / Refund", summary?.voidRefundFormatted ?: "Rp 0", Color(0xFFEF4444))
                        BannerMetricCell("Total Diskon", summary?.totalDiskonFormatted ?: "Rp 0", Color(0xFFEF4444))
                        BannerMetricCell("Penjualan Nett", summary?.penjualanNettFormatted ?: "Rp 0", Color(0xFF38BDF8))
                        BannerMetricCell("Modal Produk", summary?.modalProdukFormatted ?: "Rp 0", Color(0xFFF59E0B))
                        BannerMetricCell("Total Laba", summary?.totalLabaFormatted ?: "Rp 0", Color(0xFF38BDF8))
                        BannerMetricCell("Jml Transaksi", "${summary?.jmlTransaksi ?: 0}", Color.White)
                    }
                    ReportType.ITEM_PENJUALAN -> {
                        BannerMetricCell("Total Qty", "${summary?.totalQty ?: 0}", Color.White)
                        BannerMetricCell("Total Diskon Penjualan", summary?.totalDiskonFormatted ?: "Rp 0", Color(0xFFEF4444))
                        BannerMetricCell("Total Penjualan", summary?.totalPenjualanFormatted ?: "Rp 0", Color(0xFF38BDF8))
                        BannerMetricCell("Modal Produk", summary?.modalProdukFormatted ?: "Rp 0", Color(0xFFF59E0B))
                        BannerMetricCell("Total Laba", summary?.totalLabaFormatted ?: "Rp 0", Color(0xFF38BDF8))
                    }
                    ReportType.LAPORAN_PELANGGAN -> {
                        BannerMetricCell("Total Penjualan", summary?.totalPenjualanFormatted ?: "IDR 0", Color(0xFF38BDF8))
                        BannerMetricCell("Diskon", summary?.diskonFormatted ?: "IDR 0", Color(0xFFEF4444))
                        BannerMetricCell("Modal Produk", summary?.modalProdukFormatted ?: "IDR 0", Color(0xFFF59E0B))
                        BannerMetricCell("Laba", summary?.labaFormatted ?: "IDR 0", Color(0xFF38BDF8))
                        BannerMetricCell("Jml Transaksi", "${summary?.jmlTransaksi ?: 0}", Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerMetricCell(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun SalesDetailTable(reports: List<ReportItemDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("TANGGAL", Modifier.weight(1.1f))
                TableHeaderCell("WAKTU", Modifier.weight(1.3f))
                TableHeaderCell("NO. PESANAN", Modifier.weight(2.0f))
                TableHeaderCell("PAYMENT", Modifier.weight(1.0f))
                TableHeaderCell("STATUS", Modifier.weight(0.8f))
                TableHeaderCell("KASIR", Modifier.weight(0.9f))
                TableHeaderCell("PELANGGAN", Modifier.weight(1.6f), textAlign = TextAlign.Center)
                TableHeaderCell("TOTAL PENJUALAN", Modifier.weight(1.3f))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            reports.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableBodyCell(item.tanggalTransaksi, Modifier.weight(1.1f))
                    TableBodyCell(item.waktuTransaksi, Modifier.weight(1.3f))
                    TableBodyCell(item.cnotransaction, Modifier.weight(2.0f), isBold = true, color = Color(0xFF0F172A))
                    
                    Box(modifier = Modifier.weight(1.0f)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEFF6FF)) {
                            Text(
                                text = item.paymentMethod,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(0.8f)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                            Text(
                                text = item.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    TableBodyCell(item.kasir, Modifier.weight(0.9f))
                    TableBodyCell(item.pelanggan, Modifier.weight(1.6f), textAlign = TextAlign.Center)
                    TableBodyCell(item.totalPenjualanFormatted, Modifier.weight(1.3f), isBold = true, color = Color(0xFF0F172A))
                }

                if (index < reports.size - 1) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun SalesItemTable(reports: List<ReportItemDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("NO. PESANAN", Modifier.weight(2.0f))
                TableHeaderCell("TANGGAL", Modifier.weight(1.5f))
                TableHeaderCell("KASIR", Modifier.weight(1.0f))
                TableHeaderCell("ITEM", Modifier.weight(2.2f))
                TableHeaderCell("PELANGGAN", Modifier.weight(1.4f), textAlign = TextAlign.Center)
                TableHeaderCell("QTY", Modifier.weight(0.5f))
                TableHeaderCell("DISKON", Modifier.weight(0.9f))
                TableHeaderCell("TOTAL PENJUALAN", Modifier.weight(1.3f))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            reports.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableBodyCell(item.cnotransaction, Modifier.weight(2.0f), isBold = true, color = Color(0xFF0F172A))
                    TableBodyCell(item.waktuTransaksi, Modifier.weight(1.5f))
                    TableBodyCell(item.penjualanOleh, Modifier.weight(1.0f))
                    TableBodyCell(item.itemName, Modifier.weight(2.2f), isBold = true, color = Color(0xFF0F172A))
                    TableBodyCell(item.customerName, Modifier.weight(1.4f), textAlign = TextAlign.Center)
                    TableBodyCell("${item.qty}", Modifier.weight(0.5f), isBold = true)
                    TableBodyCell(item.diskonFormatted, Modifier.weight(0.9f))
                    TableBodyCell(item.totalPenjualanFormatted, Modifier.weight(1.3f), isBold = true, color = Color(0xFF2563EB))
                }

                if (index < reports.size - 1) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun CustomerReportTable(reports: List<ReportItemDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("PELANGGAN", Modifier.weight(1.4f))
                TableHeaderCell("TOTAL PENJUALAN", Modifier.weight(1.3f))
                TableHeaderCell("DISKON", Modifier.weight(1.1f))
                TableHeaderCell("MODAL PRODUK", Modifier.weight(1.1f))
                TableHeaderCell("LABA", Modifier.weight(1.3f))
                TableHeaderCell("JUMLAH TRANSAKSI", Modifier.weight(1.1f))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            reports.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableBodyCell(item.pelanggan, Modifier.weight(1.4f), isBold = true, color = Color(0xFF0F172A))
                    TableBodyCell(item.totalPenjualanFormatted, Modifier.weight(1.3f))
                    TableBodyCell(item.diskonFormatted, Modifier.weight(1.1f))
                    TableBodyCell(item.modalProdukFormatted, Modifier.weight(1.1f))
                    TableBodyCell(item.labaFormatted, Modifier.weight(1.3f), isBold = true, color = Color(0xFF0F172A))
                    TableBodyCell("${item.jmlTransaksi}", Modifier.weight(1.1f))
                }

                if (index < reports.size - 1) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF475569),
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun TableBodyCell(
    text: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    color: Color = Color(0xFF334155),
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    lastPage: Int,
    perPage: Int,
    totalResults: Int,
    onPageSelected: (Int) -> Unit
) {
    if (totalResults == 0) return

    val from = ((currentPage - 1) * perPage) + 1
    val to = minOf(currentPage * perPage, totalResults)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Showing $from to $to of $totalResults results",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (currentPage > 1) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = currentPage > 1) { onPageSelected(currentPage - 1) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "<",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPage > 1) Color(0xFF334155) else Color(0xFF94A3B8)
                    )
                }
            }

            // Page Number Buttons
            val pageWindow = calculatePageWindow(currentPage, lastPage)
            pageWindow.forEach { page ->
                val isSelected = page == currentPage
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) MatahatiMaroon else Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onPageSelected(page) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$page",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            // Next Button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (currentPage < lastPage) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = currentPage < lastPage) { onPageSelected(currentPage + 1) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPage < lastPage) Color(0xFF334155) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

private fun calculatePageWindow(current: Int, max: Int): List<Int> {
    if (max <= 5) return (1..max).toList()
    val start = (current - 2).coerceAtLeast(1)
    val end = (start + 4).coerceAtMost(max)
    val adjustedStart = (end - 4).coerceAtLeast(1)
    return (adjustedStart..end).toList()
}
