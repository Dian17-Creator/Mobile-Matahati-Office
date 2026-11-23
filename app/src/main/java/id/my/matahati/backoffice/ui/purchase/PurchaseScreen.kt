package id.my.matahati.backoffice.ui.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.PurchaseDetailData
import id.my.matahati.backoffice.model.PurchaseItemDto
import id.my.matahati.backoffice.ui.dashboard.DatePresetFilterDialog
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel,
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
        // 4 Tab Bar Menu (Flush to top header, fill max width equally)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PurchaseTab.entries.forEach { tab ->
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
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }

                // Full-width Bottom Indicator Line
                Row(modifier = Modifier.fillMaxWidth()) {
                    PurchaseTab.entries.forEach { tab ->
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

        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (uiState.isDetailOpen) {
                PurchaseDetailContent(
                    uiState = uiState,
                    onBack = { viewModel.closeDetail() },
                    onRefresh = { uiState.selectedPurchaseDetail?.id?.let { viewModel.openDetail(it) } },
                    onPrintPdf = { viewModel.exportPdf(context) }
                )
            } else {
                PurchaseListContent(
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onOpenDetail = { viewModel.openDetail(it) },
                    onOpenDateFilter = { showDateFilterDialog = true },
                    onRefresh = { viewModel.fetchPurchases() }
                )
            }
        }
    }
}

@Composable
private fun PurchaseListContent(
    uiState: PurchaseUiState,
    onSearchQueryChange: (String) -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenDateFilter: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Page Title & Header Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pembelian ${uiState.selectedTab.label}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                OutlinedButton(
//                    onClick = { /* Import PDF action */ },
//                    shape = RoundedCornerShape(8.dp),
//                    border = BorderStroke(1.dp, MatahatiMaroon),
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MatahatiMaroon)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.PictureAsPdf,
//                        contentDescription = "Import PDF",
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(text = "Import PDF", fontWeight = FontWeight.SemiBold)
//                }
//
//                Button(
//                    onClick = { /* Tambah Pembelian */ },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Tambah Pembelian",
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(6.dp))
//                    Text(text = "Tambah Pembelian", fontWeight = FontWeight.Bold, color = Color.White)
//                }
//            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter Bar Card (Date Filter & Search Input)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Filter Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FILTER :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onOpenDateFilter() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Filter Tanggal",
                                tint = MatahatiMaroon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.startDate.ifEmpty { "Semua Tanggal" },
                                fontSize = 13.sp,
                                color = MatahatiMaroon,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Search Box (Surface + BasicTextField for 100% vertical center alignment)
                Surface(
                    modifier = Modifier.width(360.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari No. PO, Supplier, No. SJ...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Loading & Error states
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
                    Text("Memuat transaksi pembelian...", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else if (uiState.errorMessage != null) {
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
                    Text(text = uiState.errorMessage ?: "Terjadi kesalahan", color = Color(0xFFDC2626), fontSize = 14.sp)
                    Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon)) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        } else if (uiState.purchases.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data transaksi pembelian untuk kategori ${uiState.selectedTab.label}.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Main Purchase Data Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("NO. PEMBELIAN", Modifier.weight(1.4f))
                        TableHeaderCell("TANGGAL BELI", Modifier.weight(1f))
                        TableHeaderCell("SUPPLIER", Modifier.weight(1f))
                        TableHeaderCell("OUTLET", Modifier.weight(1.2f))
                        TableHeaderCell("JUMLAH", Modifier.weight(1.2f))
                        TableHeaderCell("TELAH BAYAR", Modifier.weight(0.9f))
                        TableHeaderCell("TANGGAL PEMBAYARAN", Modifier.weight(1.2f))
                        TableHeaderCell("STATUS", Modifier.weight(0.9f))
                        TableHeaderCell("AKSI", Modifier.weight(0.8f))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Table Data Rows
                    uiState.purchases.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // NO. PEMBELIAN
                            Column(modifier = Modifier.weight(1.4f)) {
                                Text(
                                    text = item.cnoPurchase,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                                Text(
                                    text = "Ref: ${item.cnoReference}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // TANGGAL BELI
                            Text(
                                text = item.dpurchaseFormatted,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1f)
                            )

                            // SUPPLIER
                            Text(
                                text = item.supplierName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )

                            // OUTLET
                            Text(
                                text = item.outletName,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.2f)
                            )

                            // JUMLAH
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = item.totalAmountFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${item.itemsCount} Item",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // TELAH BAYAR
                            Box(modifier = Modifier.weight(0.9f)) {
                                StatusBadgePill(
                                    text = item.paymentStatusLabel,
                                    isSuccess = item.paymentStatus == "PAID"
                                )
                            }

                            // TANGGAL PEMBAYARAN
                            Text(
                                text = item.dpaymentFormatted,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.2f)
                            )

                            // STATUS
                            Box(modifier = Modifier.weight(0.9f)) {
                                StatusBadgePill(
                                    text = item.cstatusLabel,
                                    isSuccess = item.cstatus == "RECEIVED"
                                )
                            }

                            // AKSI
                            Box(modifier = Modifier.weight(0.8f)) {
                                OutlinedButton(
                                    onClick = { onOpenDetail(item.id) },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Buka",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Buka", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (index < uiState.purchases.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PurchaseDetailContent(
    uiState: PurchaseUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onPrintPdf: () -> Unit
) {
    val detail = uiState.selectedPurchaseDetail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Detail Header Bar with Breadcrumb & Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daftar Pembelian  /  ${detail?.cnoPurchase ?: "PO"}",
                    fontSize = 13.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = detail?.cnoPurchase ?: "PO",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = detail?.statusLabel ?: "Telah Diterima",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

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
                    onClick = onPrintPdf,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Cetak PO",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cetak PO", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isDetailLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
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
            // Banner Alert Payment Status
            val isPaid = detail.paymentStatus == "PAID"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isPaid) Color(0xFF16A34A) else Color(0xFFEF4444)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = detail.paymentStatusLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { }
                    ) {
                        Text(
                            text = "Ubah Status Bayar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) Color(0xFF16A34A) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main PO Overview Box
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
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = detail.cnoPurchase,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dibuat oleh: ${detail.createdBy} pada ${detail.dpurchaseFormatted}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Outlet Tujuan",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = detail.outletName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Info Cards Grid Row (Supplier, Info Penerimaan, Catatan & Jatuh Tempo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Supplier
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
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = "Supplier",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Supplier", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                            Text(text = "Rincian →", fontSize = 13.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailInfoRow("Nama Supplier", detail.supplier?.name ?: "-")
                        DetailInfoRow("PIC / Kontak", detail.supplier?.picContact ?: "-")
                        DetailInfoRow("Telepon & Email", detail.supplier?.phoneEmail ?: "-")
                        DetailInfoRow("Alamat", detail.supplier?.address ?: "-")
                    }
                }

                // Card 2: Info Penerimaan
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
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = "Info Penerimaan",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Info Penerimaan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailInfoRow("Tanggal Diterima", detail.receiptInfo?.dreceivedFormatted ?: "-", valueColor = Color(0xFF15803D))
                        DetailInfoRow("No. Terima / Surat Jalan", detail.receiptInfo?.noReceiptSj ?: "-")
                        DetailInfoRow("Penerima Barang", detail.receiptInfo?.receivedBy ?: "-")

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stok produk telah ditambahkan ke sistem.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }

                // Card 3: Catatan & Jatuh Tempo
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
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Catatan",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Catatan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detail.note,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Jatuh Tempo",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Jatuh Tempo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailInfoRow("Tanggal Jatuh Tempo", detail.dueDate?.dueDateFormatted ?: "Tidak ditentukan")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Daftar Item Pesanan Table
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Daftar Item Pesanan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${detail.itemsCount} Item",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Table Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("NO.", Modifier.weight(0.4f))
                        TableHeaderCell("ITEM (PRODUK / BAHAN BAKU)", Modifier.weight(2.5f))
                        TableHeaderCell("TIPE", Modifier.weight(1f))
                        TableHeaderCell("STOK SAAT INI", Modifier.weight(1.2f))
                        TableHeaderCell("KUANTITAS BELI", Modifier.weight(1.2f))
                        TableHeaderCell("HARGA BELI SATUAN", Modifier.weight(1.4f))
                        TableHeaderCell("TOTAL HARGA", Modifier.weight(1.4f))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Items List Rows
                    detail.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.no}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.weight(0.4f)
                            )

                            // Item Name & Category/Unit
                            Row(
                                modifier = Modifier.weight(2.5f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inventory2,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.productName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = item.categoryUnit,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // TIPE
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Text(
                                        text = item.itemType,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // STOK SAAT INI
                            Text(
                                text = item.currentStock,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.2f)
                            )

                            // KUANTITAS BELI
                            Text(
                                text = item.qtyBuy,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.weight(1.2f)
                            )

                            // HARGA BELI SATUAN
                            Text(
                                text = item.buyPriceFormatted,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.4f)
                            )

                            // TOTAL HARGA
                            Text(
                                text = item.totalPriceFormatted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1.4f)
                            )
                        }

                        if (index < detail.items.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Grand Total Footer Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRAND TOTAL PEMBELIAN:  ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = detail.totalAmountFormatted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF475569),
        modifier = modifier
    )
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun StatusBadgePill(
    text: String,
    isSuccess: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSuccess) Color(0xFF15803D) else Color(0xFFDC2626),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
