package id.my.matahati.backoffice.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Inbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.InventoryItemDto
import id.my.matahati.backoffice.ui.dashboard.DatePresetFilterDialog
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                        // Empty State (Presisi dengan Screenshot Web POS)
                        val (emptyIcon, emptyText) = when (uiState.selectedTab) {
                            InventoryTab.STOK_MASUK -> Pair(Icons.Outlined.Inbox, "Belum ada riwayat stok masuk.")
                            InventoryTab.STOK_KELUAR -> Pair(Icons.Outlined.Inbox, "Tidak ada riwayat transaksi stok keluar pada periode ini.")
                            InventoryTab.PRODUKSI_STOK -> Pair(Icons.Outlined.Settings, "Belum ada dokumen Produksi Stok yang ditemukan.")
                            InventoryTab.STOK_OPNAME -> Pair(Icons.Outlined.Assignment, "Belum ada dokumen Stok Opname yang ditemukan.")
                            InventoryTab.PERGERAKAN_STOK -> Pair(Icons.Outlined.ContentPaste, "Belum ada data pergerakan stok pada periode ini.")
                        }

                        // Empty State Table Header
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
                        // Data Table Content
                        when (uiState.selectedTab) {
                            InventoryTab.STOK_MASUK -> StockInTable(items = uiState.items)
                            InventoryTab.STOK_KELUAR -> StockOutTable(items = uiState.items)
                            InventoryTab.PRODUKSI_STOK -> ProductionTable(items = uiState.items)
                            InventoryTab.STOK_OPNAME -> StockOpnameTable(items = uiState.items)
                            InventoryTab.PERGERAKAN_STOK -> MovementTable(items = uiState.items)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pagination Controls
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
    }
}

@Composable
private fun StockInTable(items: List<InventoryItemDto>) {
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
    }
}

@Composable
private fun StockOutTable(items: List<InventoryItemDto>) {
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
                
                // Masuk -> Hijau (#16A34A)
                TableBodyCell(
                    text = "${item.qtyMasuk}",
                    modifier = Modifier.weight(0.8f),
                    isBold = true,
                    color = Color(0xFF16A34A),
                    textAlign = TextAlign.Center
                )

                // Pengembalian -> Biru (#2563EB)
                TableBodyCell(
                    text = "${item.qtyPengembalian}",
                    modifier = Modifier.weight(1.1f),
                    isBold = true,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center
                )

                // Penjualan -> Biru (#2563EB)
                TableBodyCell(
                    text = "${item.qtyPenjualan}",
                    modifier = Modifier.weight(1f),
                    isBold = true,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center
                )

                // Keluar -> Merah (#DC2626)
                TableBodyCell(
                    text = "${item.qtyKeluar}",
                    modifier = Modifier.weight(0.8f),
                    isBold = true,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center
                )

                // Sisa -> Hitam Pekat Bold (#0F172A)
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
        "POSTED" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D)) // Hijau
        "DRAFT" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Orange / Kuning
        "CANCELLED", "VOID" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626)) // Merah
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
