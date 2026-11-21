package id.my.matahati.backoffice.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.InventoryDetailData
import id.my.matahati.backoffice.model.InventoryItemDto
import id.my.matahati.backoffice.ui.dashboard.DatePresetFilterDialog
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
        // Sub-header Tab Bar (5 Equal Flush Tabs: Stok Masuk, Stok Keluar, Produksi Stok, Stok Opname, Pergerakan Stok)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InventoryTab.entries.forEach { tab ->
                        val isSelected = uiState.selectedTab == tab
                        val tabBg = if (isSelected) Color(0xFFFFF1F2) else Color.White
                        val textColor = if (isSelected) MatahatiMaroon else Color(0xFF334155)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(tabBg)
                                .clickable { viewModel.selectTab(tab) }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Full-width Bottom Indicator Line
                Row(modifier = Modifier.fillMaxWidth()) {
                    InventoryTab.entries.forEach { tab ->
                        val isSelected = uiState.selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (isSelected) MatahatiMaroon else Color(0xFFE2E8F0))
                        )
                    }
                }
            }
        }

        // Main Content Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (uiState.isDetailOpen) {
                InventoryDetailContent(
                    uiState = uiState,
                    onBack = { viewModel.closeDetail() },
                    onExportPdf = { viewModel.exportPdf(context) },
                    onRefresh = {
                        uiState.selectedDetailNo?.let { no ->
                            viewModel.openDetail(uiState.selectedDetailType, no)
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Title & Date Filter Pill Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val titleText = when (uiState.selectedTab) {
                                        InventoryTab.STOK_MASUK -> "Daftar Stok Masuk"
                                        InventoryTab.STOK_KELUAR -> "Daftar Stok Keluar"
                                        InventoryTab.PRODUKSI_STOK -> "Daftar Produksi Stok"
                                        InventoryTab.STOK_OPNAME -> "Opname Stok"
                                        InventoryTab.PERGERAKAN_STOK -> "Pergerakan Stok"
                                    }
                                    val countText = when (uiState.selectedTab) {
                                        InventoryTab.STOK_MASUK -> "${uiState.totalResults} Stok Masuk"
                                        InventoryTab.STOK_KELUAR -> "${uiState.totalResults} Stok Keluar"
                                        InventoryTab.PRODUKSI_STOK -> "${uiState.totalResults} Dokumen Produksi"
                                        InventoryTab.STOK_OPNAME -> "${uiState.totalResults} Opname Stok"
                                        InventoryTab.PERGERAKAN_STOK -> "${uiState.totalResults} Item Terdata"
                                    }

                                    Text(
                                        text = titleText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = countText,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showDateFilterDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Filter Tanggal",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = uiState.startDate.ifEmpty { "Pilih Tanggal" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF334155)
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // Loading State
                            if (uiState.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = MatahatiMaroon)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Memuat data inventori...", color = Color(0xFF64748B), fontSize = 14.sp)
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
                            } else if (uiState.items.isEmpty()) {
                                val (emptyIcon, emptyText) = when (uiState.selectedTab) {
                                    InventoryTab.STOK_MASUK -> Pair(Icons.Outlined.Inbox, "Belum ada riwayat stok masuk.")
                                    InventoryTab.STOK_KELUAR -> Pair(Icons.Outlined.Inbox, "Tidak ada riwayat transaksi stok keluar pada periode ini.")
                                    InventoryTab.PRODUKSI_STOK -> Pair(Icons.Outlined.Settings, "Belum ada dokumen Produksi Stok yang ditemukan.")
                                    InventoryTab.STOK_OPNAME -> Pair(Icons.Outlined.Assignment, "Belum ada dokumen Stok Opname yang ditemukan.")
                                    InventoryTab.PERGERAKAN_STOK -> Pair(Icons.Outlined.ContentPaste, "Belum ada data pergerakan stok pada periode ini.")
                                }

                                when (uiState.selectedTab) {
                                    InventoryTab.STOK_MASUK -> StockInTableHeader()
                                    InventoryTab.STOK_KELUAR -> StockOutTableHeader()
                                    InventoryTab.PRODUKSI_STOK -> ProductionTableHeader()
                                    InventoryTab.STOK_OPNAME -> StockOpnameTableHeader()
                                    InventoryTab.PERGERAKAN_STOK -> MovementTableHeader()
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = emptyIcon,
                                            contentDescription = "Empty",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = emptyText,
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            } else {
                                when (uiState.selectedTab) {
                                    InventoryTab.STOK_MASUK -> StockInTable(
                                        items = uiState.items,
                                        onOpenDetail = { no -> viewModel.openDetail("stock_in", no) }
                                    )
                                    InventoryTab.STOK_KELUAR -> StockOutTable(
                                        items = uiState.items,
                                        onOpenDetail = { no -> viewModel.openDetail("stock_out", no) }
                                    )
                                    InventoryTab.PRODUKSI_STOK -> ProductionTable(items = uiState.items)
                                    InventoryTab.STOK_OPNAME -> StockOpnameTable(items = uiState.items)
                                    InventoryTab.PERGERAKAN_STOK -> MovementTable(items = uiState.items)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                PaginationControls(
                                    currentPage = uiState.currentPage,
                                    lastPage = uiState.lastPage,
                                    perPage = uiState.perPage,
                                    totalResults = uiState.totalResults,
                                    tab = uiState.selectedTab,
                                    onPageSelected = { viewModel.selectPage(it) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// =============================================================================
// DETAIL INVENTORY VIEW CONTENT (MATCHING SCREENSHOT 1 100%)
// =============================================================================

@Composable
private fun InventoryDetailContent(
    uiState: InventoryUiState,
    onBack: () -> Unit,
    onExportPdf: () -> Unit,
    onRefresh: () -> Unit
) {
    val detail = uiState.selectedInventoryDetail
    val isStockOut = uiState.selectedDetailType == "stock_out" || uiState.selectedDetailType == "stok_keluar"
    val docTypeLabel = if (isStockOut) "Detail Stok Keluar" else "Detail Stok Masuk"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Breadcrumb & Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventori  /  $docTypeLabel",
                fontSize = 13.sp,
                color = Color(0xFF0284C7),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isDetailLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MatahatiMaroon)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat detail transaksi...", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else if (uiState.detailErrorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.detailErrorMessage ?: "Gagal memuat detail", color = Color(0xFFDC2626))
                    Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon)) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        } else if (detail != null) {
            // Top Status Posted Banner Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Info",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Status Posted: Dokumen transaksi ini telah Diposting. Stok fisik barang telah ditambahkan secara otomatis ke outlet penerima dan tercatat pada laporan pergerakan stok.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Document Number Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = detail.statusLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (isStockOut) "No. Stok Keluar" else "No. Stok Masuk",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = detail.cnoRef,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = detail.outletName,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Action Buttons (Kembali & Cetak)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Kembali", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onExportPdf,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Cetak PDF",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Cetak PDF", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Metadata Info Cards Grid Row (Equal Height via IntrinsicSize.Max)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Tanggal Masuk/Keluar
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isStockOut) "Tanggal Keluar" else "Tanggal Masuk", fontSize = 12.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = detail.dmutation, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Text(text = detail.postedBy, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                // Card 2: Diterima Dari / Transfer Ke
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isStockOut) "Transfer ke toko" else "Diterima Dari", fontSize = 12.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = if (isStockOut) detail.transferKe else detail.diterimaDari, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Text(text = if (isStockOut) "Transfer Antar Toko" else "Input Stok Masuk Manual", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                // Card 3: Catatan
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        Text(text = "Catatan", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = detail.cnote, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }

                // Card 4: Alasan Penerimaan/Pengeluaran
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        Text(text = if (isStockOut) "Alasan Pengeluaran" else "Alasan Penerimaan", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = detail.creason, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Blue Header Table Item Barang Masuk / Keluar (X)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Blue Section Title Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2563EB))
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = if (isStockOut) "Item Barang Keluar (${detail.details.size})" else "Item Barang Masuk (${detail.details.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Table Column Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("NO", Modifier.weight(0.5f))
                        TableHeaderCell("TIPE", Modifier.weight(0.9f))
                        TableHeaderCell("PRODUK / BAHAN BAKU", Modifier.weight(3f))
                        TableHeaderCell("STOK SAAT INI", Modifier.weight(1.2f), textAlign = TextAlign.Center)
                        TableHeaderCell(if (isStockOut) "QTY KELUAR" else "QTY MASUK", Modifier.weight(1.2f), textAlign = TextAlign.Center)
                        TableHeaderCell("HARGA BELI", Modifier.weight(1.2f), textAlign = TextAlign.Right)
                        TableHeaderCell("SUBTOTAL", Modifier.weight(1.3f), textAlign = TextAlign.Right)
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Detail Items Rows
                    detail.details.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableBodyCell("${item.no}", Modifier.weight(0.5f))

                            // Tipe Pill Badge
                            Box(modifier = Modifier.weight(0.9f)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE0F2FE)
                                ) {
                                    Text(
                                        text = item.tipe,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Produk / Bahan Baku Name & Category Pill
                            Row(
                                modifier = Modifier.weight(3f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Outlined.Inventory2, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = item.itemName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(text = item.categoryName, fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            // Stok Saat Ini Pill
                            Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(text = "${item.currentStock} pcs", fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }

                            // Qty Masuk / Keluar
                            TableBodyCell("${item.qty} pcs", Modifier.weight(1.2f), isBold = true, textAlign = TextAlign.Center)

                            // Harga Beli
                            TableBodyCell(item.costFormatted, Modifier.weight(1.2f), textAlign = TextAlign.Right)

                            // Subtotal
                            TableBodyCell(item.subtotalFormatted, Modifier.weight(1.3f), isBold = true, color = Color(0xFF0F172A), textAlign = TextAlign.Right)
                        }

                        if (index < detail.details.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Table Footer Row (Jumlah & Total Nilai - Presisi Web Screenshot 3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(5.6f))

                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Jumlah:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${detail.totalQty}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        }

                        Text(
                            text = "Total Nilai:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(1.2f)
                        )

                        Text(
                            text = detail.totalCostFormatted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// =============================================================================
// TABLE HEADERS & TABLES FOR 5 TABS
// =============================================================================

@Composable
private fun StockInTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("No.", Modifier.weight(1.8f))
        TableHeaderCell("Diterima dari", Modifier.weight(1.3f))
        TableHeaderCell("Supplier", Modifier.weight(1.3f))
        TableHeaderCell("Tanggal", Modifier.weight(1.2f))
        TableHeaderCell("Catatan", Modifier.weight(1.8f))
        TableHeaderCell("Status", Modifier.weight(1f), textAlign = TextAlign.Center)
        TableHeaderCell("Diterima Oleh", Modifier.weight(1.2f))
        TableHeaderCell("Aksi", Modifier.weight(0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun StockInTable(
    items: List<InventoryItemDto>,
    onOpenDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        StockInTableHeader()
        HorizontalDivider(color = Color(0xFFE2E8F0))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(item.no ?: "-", Modifier.weight(1.8f), isBold = true, color = Color(0xFF0F172A))
                TableBodyCell(item.diterimaDari ?: "-", Modifier.weight(1.3f))
                TableBodyCell(item.supplier ?: "-", Modifier.weight(1.3f))
                TableBodyCell(item.tanggal ?: "-", Modifier.weight(1.2f))
                TableBodyCell(item.catatan ?: "-", Modifier.weight(1.8f))
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatusPill(statusLabel = item.statusLabel ?: "Draf", statusRaw = item.status ?: "DRAFT")
                }

                TableBodyCell(item.diterimaOleh ?: "-", Modifier.weight(1.2f))

                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { item.no?.let { onOpenDetail(it) } },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Buka", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Buka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (index < items.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun StockOutTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("No.", Modifier.weight(1.8f))
        TableHeaderCell("Transfer ke", Modifier.weight(1.3f))
        TableHeaderCell("Tanggal", Modifier.weight(1.2f))
        TableHeaderCell("Catatan", Modifier.weight(1.6f))
        TableHeaderCell("Alasan", Modifier.weight(1.4f))
        TableHeaderCell("Status", Modifier.weight(1f), textAlign = TextAlign.Center)
        TableHeaderCell("Dibuat Oleh", Modifier.weight(1.2f))
        TableHeaderCell("Aksi", Modifier.weight(0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun StockOutTable(
    items: List<InventoryItemDto>,
    onOpenDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        StockOutTableHeader()
        HorizontalDivider(color = Color(0xFFE2E8F0))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(item.no ?: "-", Modifier.weight(1.8f), isBold = true, color = Color(0xFF0F172A))
                TableBodyCell(item.transferKe ?: "-", Modifier.weight(1.3f))
                TableBodyCell(item.tanggal ?: "-", Modifier.weight(1.2f))
                TableBodyCell(item.catatan ?: "-", Modifier.weight(1.6f))
                TableBodyCell(item.alasan ?: "-", Modifier.weight(1.4f))
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatusPill(statusLabel = item.statusLabel ?: "Draf", statusRaw = item.status ?: "DRAFT")
                }

                TableBodyCell(item.dibuatOleh ?: "-", Modifier.weight(1.2f))

                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { item.no?.let { onOpenDetail(it) } },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Buka", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Buka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (index < items.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun ProductionTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("No.", Modifier.weight(2f))
        TableHeaderCell("Tanggal", Modifier.weight(1.4f))
        TableHeaderCell("Catatan", Modifier.weight(2.2f))
        TableHeaderCell("Status", Modifier.weight(1.1f), textAlign = TextAlign.Center)
        TableHeaderCell("Diterima / Dibuat Oleh", Modifier.weight(1.5f))
    }
}

@Composable
private fun ProductionTable(items: List<InventoryItemDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        ProductionTableHeader()
        HorizontalDivider(color = Color(0xFFE2E8F0))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(item.no ?: "-", Modifier.weight(2f), isBold = true, color = Color(0xFF0F172A))
                TableBodyCell(item.tanggal ?: "-", Modifier.weight(1.4f))
                TableBodyCell(item.catatan ?: "-", Modifier.weight(2.2f))
                
                Box(modifier = Modifier.weight(1.1f), contentAlignment = Alignment.Center) {
                    StatusPill(statusLabel = item.statusLabel ?: "Draf", statusRaw = item.status ?: "DRAFT")
                }

                TableBodyCell(item.diterimaDibuatOleh ?: "-", Modifier.weight(1.5f))
            }

            if (index < items.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun StockOpnameTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("No.", Modifier.weight(2f))
        TableHeaderCell("Tanggal", Modifier.weight(1.4f))
        TableHeaderCell("Catatan", Modifier.weight(2.2f))
        TableHeaderCell("Status", Modifier.weight(1.1f), textAlign = TextAlign.Center)
        TableHeaderCell("Diterima Oleh", Modifier.weight(1.5f))
    }
}

@Composable
private fun StockOpnameTable(items: List<InventoryItemDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        StockOpnameTableHeader()
        HorizontalDivider(color = Color(0xFFE2E8F0))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(item.no ?: "-", Modifier.weight(2f), isBold = true, color = Color(0xFF0F172A))
                TableBodyCell(item.tanggal ?: "-", Modifier.weight(1.4f))
                TableBodyCell(item.catatan ?: "-", Modifier.weight(2.2f))
                
                Box(modifier = Modifier.weight(1.1f), contentAlignment = Alignment.Center) {
                    StatusPill(statusLabel = item.statusLabel ?: "Draf", statusRaw = item.status ?: "DRAFT")
                }

                TableBodyCell(item.diterimaOleh ?: "-", Modifier.weight(1.5f))
            }

            if (index < items.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun MovementTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("Grup / Kategori", Modifier.weight(1.5f))
        TableHeaderCell("Produk", Modifier.weight(2.4f))
        TableHeaderCell("Awal", Modifier.weight(0.8f), textAlign = TextAlign.Center)
        TableHeaderCell("Masuk", Modifier.weight(0.8f), textAlign = TextAlign.Center)
        TableHeaderCell("Pengembalian", Modifier.weight(1.1f), textAlign = TextAlign.Center)
        TableHeaderCell("Penjualan", Modifier.weight(1f), textAlign = TextAlign.Center)
        TableHeaderCell("Keluar", Modifier.weight(0.8f), textAlign = TextAlign.Center)
        TableHeaderCell("Sisa", Modifier.weight(0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun MovementTable(items: List<InventoryItemDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        MovementTableHeader()
        HorizontalDivider(color = Color(0xFFE2E8F0))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableBodyCell(item.categoryName ?: "-", Modifier.weight(1.5f))
                TableBodyCell(item.cname ?: "-", Modifier.weight(2.4f), isBold = true, color = Color(0xFF0F172A))
                TableBodyCell("${item.qtyAwal}", Modifier.weight(0.8f), textAlign = TextAlign.Center)
                
                TableBodyCell(
                    text = "${item.qtyMasuk}",
                    modifier = Modifier.weight(0.8f),
                    isBold = true,
                    color = Color(0xFF16A34A),
                    textAlign = TextAlign.Center
                )

                TableBodyCell(
                    text = "${item.qtyPengembalian}",
                    modifier = Modifier.weight(1.1f),
                    isBold = true,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center
                )

                TableBodyCell(
                    text = "${item.qtyPenjualan}",
                    modifier = Modifier.weight(1f),
                    isBold = true,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center
                )

                TableBodyCell(
                    text = "${item.qtyKeluar}",
                    modifier = Modifier.weight(0.8f),
                    isBold = true,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center
                )

                TableBodyCell(
                    text = "${item.qtySisa}",
                    modifier = Modifier.weight(0.8f),
                    isBold = true,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            }

            if (index < items.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

@Composable
private fun StatusPill(statusLabel: String, statusRaw: String) {
    val (bgColor, textColor) = when (statusRaw.uppercase()) {
        "POSTED" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "DRAFT" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        "CANCELLED", "VOID" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = statusLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
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
    tab: InventoryTab,
    onPageSelected: (Int) -> Unit
) {
    if (totalResults == 0) return

    val from = ((currentPage - 1) * perPage) + 1
    val to = minOf(currentPage * perPage, totalResults)

    val showingText = when (tab) {
        InventoryTab.PERGERAKAN_STOK -> "Menampilkan $from - $to dari $totalResults item"
        InventoryTab.STOK_MASUK -> "Menampilkan $from - $to dari $totalResults stok masuk"
        InventoryTab.STOK_KELUAR -> "Menampilkan $from - $to dari $totalResults stok keluar"
        InventoryTab.PRODUKSI_STOK -> "Menampilkan $from - $to dari $totalResults dokumen produksi"
        InventoryTab.STOK_OPNAME -> "Menampilkan $from - $to dari $totalResults opname stok"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = showingText,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
