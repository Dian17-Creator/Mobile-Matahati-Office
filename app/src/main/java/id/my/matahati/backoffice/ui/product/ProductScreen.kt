package id.my.matahati.backoffice.ui.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.CategoryItemDto
import id.my.matahati.backoffice.model.ProductItemDto
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sub-header Tab Bar (2 Equal Flush Tabs: Produk | Kategori)
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
                    ProductSubTab.values().forEach { tab ->
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
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Full-width Bottom Indicator Line
                Row(modifier = Modifier.fillMaxWidth()) {
                    ProductSubTab.values().forEach { tab ->
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

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (uiState.selectedTab == ProductSubTab.PRODUK) {
                ProductsTabContent(
                    uiState = uiState,
                    onCategorySelect = { viewModel.selectCategoryFilter(it) },
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onToggleHideEmpty = { viewModel.toggleHideEmptyStock(it) },
                    onPageSelected = { viewModel.selectPage(it) },
                    onRefresh = { viewModel.refresh() }
                )
            } else {
                CategoriesTabContent(
                    uiState = uiState,
                    onNameChange = { viewModel.onNewCatNameChange(it) },
                    onOutletChange = { viewModel.onNewCatOutletChange(it) },
                    onTrackStockChange = { viewModel.onNewCatTrackStockChange(it) },
                    onShowPosChange = { viewModel.onNewCatShowPosChange(it) },
                    onSubmit = { viewModel.submitCreateCategory() },
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
                )
            }
        }
    }
}

// =============================================================================
// TAB 1: PRODUK (DAFTAR PRODUK)
// =============================================================================

@Composable
private fun ProductsTabContent(
    uiState: ProductUiState,
    onCategorySelect: (Int?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleHideEmpty: (Boolean) -> Unit,
    onPageSelected: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val selectedCategoryLabel = if (uiState.selectedCategoryId == null || uiState.selectedCategoryId == 0) {
        "Semua Kategori"
    } else {
        uiState.categoriesOptions.find { it.id == uiState.selectedCategoryId }?.cname ?: "Semua Kategori"
    }

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
                // Header & Action Bar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Produk",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Right Filter & Action Tools
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Dropdown Filter
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCategoryDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCategoryLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Semua Kategori", fontSize = 13.sp) },
                                    onClick = {
                                        showCategoryDropdown = false
                                        onCategorySelect(null)
                                    }
                                )
                                uiState.categoriesOptions.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.cname, fontSize = 13.sp) },
                                        onClick = {
                                            showCategoryDropdown = false
                                            onCategorySelect(cat.id)
                                        }
                                    )
                                }
                            }
                        }

                        // Search Box (Surface + BasicTextField)
                        Surface(
                            modifier = Modifier.width(260.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari produk...",
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
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Toggle Sembunyikan Stok Kosong
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = uiState.hideEmptyStock,
                                onCheckedChange = onToggleHideEmpty,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MatahatiMaroon
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sembunyikan Stok Kosong",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Loading & Error States
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
                            Text("Memuat data produk...", color = Color(0xFF64748B), fontSize = 14.sp)
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
                            Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon)) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                } else if (uiState.products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada data produk ditemukan.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                } else {
                    // Products Table (8 Columns)
                    ProductsTable(products = uiState.products)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pagination Controls
                    PaginationControls(
                        currentPage = uiState.currentPage,
                        lastPage = uiState.lastPage,
                        perPage = uiState.perPage,
                        totalResults = uiState.totalResults,
                        onPageSelected = onPageSelected
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProductsTable(products: List<ProductItemDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(8.dp))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Foto", Modifier.weight(0.7f))
            TableHeaderCell("Nama Produk", Modifier.weight(2.2f))
            TableHeaderCell("Kategori", Modifier.weight(1.3f))
            TableHeaderCell("Outlet", Modifier.weight(1.4f))
            TableHeaderCell("Harga", Modifier.weight(1.5f))
            TableHeaderCell("Stok / Qty", Modifier.weight(1.1f))
            TableHeaderCell("Status", Modifier.weight(0.9f))
            TableHeaderCell("Aksi", Modifier.weight(1.1f), textAlign = TextAlign.Center)
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // Table Rows
        products.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto (Thumbnail Icon Box matching Web Screenshot)
                Box(modifier = Modifier.weight(0.7f)) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Nama Produk
                TableBodyCell(item.cname, Modifier.weight(2.2f), isBold = true, color = Color(0xFF0F172A))

                // Kategori
                TableBodyCell(item.categoryName, Modifier.weight(1.3f))

                // Outlet Pill Badge
                Box(modifier = Modifier.weight(1.4f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = item.outletName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Harga (3 Tingkat)
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(text = item.npriceFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = "Online: ${item.npriceOnlineFormatted}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "Beli: ${item.npriceBuyFormatted}", fontSize = 11.sp, color = Color(0xFF64748B))
                }

                // Stok / Qty
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(text = "${item.nqty}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = item.csatuan, fontSize = 11.sp, color = Color(0xFF64748B))
                }

                // Status Badge
                Box(modifier = Modifier.weight(0.9f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = item.cstatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Aksi Buttons (View / Edit / Delete)
                Row(
                    modifier = Modifier.weight(1.1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (index < products.size - 1) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

// =============================================================================
// TAB 2: KATEGORI (KATEGORI PRODUK)
// =============================================================================

@Composable
private fun CategoriesTabContent(
    uiState: ProductUiState,
    onNameChange: (String) -> Unit,
    onOutletChange: (Int?) -> Unit,
    onTrackStockChange: (Boolean) -> Unit,
    onShowPosChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Kolom Kiri: Form Card "Tambah Kategori Produk" (50% Lebar Layar)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tambah Kategori Produk",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Button(
                        onClick = onSubmit,
                        enabled = !uiState.isSavingCategory,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = if (uiState.isSavingCategory) "Menyimpan..." else "Simpan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Success / Error Messages
                if (uiState.saveCategorySuccessMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.saveCategorySuccessMessage,
                            fontSize = 12.sp,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (uiState.saveCategoryErrorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.saveCategoryErrorMessage,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Input Nama Kategori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(text = "Nama Kategori *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Grup/kategori produk Anda, untuk memudahkan pengelompokan produk di kasir",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = uiState.newCatName,
                        onValueChange = onNameChange,
                        placeholder = { Text("Masukkan", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatahatiMaroon,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dropdown Pilih Outlet
                var showOutletDropdown by remember { mutableStateOf(false) }
                val selectedOutletLabel = uiState.outletsOptions.find { it.id == uiState.newCatOutletId }?.cname ?: "Pilih Outlet..."

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(text = "Pilih Outlet *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih cabang outlet tempat kategori produk ini tersedia",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showOutletDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = selectedOutletLabel, fontSize = 13.sp, color = Color(0xFF334155))
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                            }
                        }

                        DropdownMenu(
                            expanded = showOutletDropdown,
                            onDismissRequest = { showOutletDropdown = false }
                        ) {
                            DropdownMenuItem(text = { Text("Semua Outlet", fontSize = 13.sp) }, onClick = { showOutletDropdown = false; onOutletChange(null) })
                            uiState.outletsOptions.forEach { o ->
                                DropdownMenuItem(text = { Text(o.cname, fontSize = 13.sp) }, onClick = { showOutletDropdown = false; onOutletChange(o.id) })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Switch Kelola Stok Produk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(text = "Kelola Stok Produk", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aktifkan untuk barang retail fisik (IQOS/Device/Pod). Matikan untuk Makanan, Minuman, atau Snack agar kasir bisa bertransaksi tanpa batasan stok.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = uiState.newCatTrackStock,
                        onCheckedChange = onTrackStockChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MatahatiMaroon)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Switch Tampilkan di Menu Kasir (POS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(text = "Tampilkan di Menu Kasir (POS)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aktifkan agar kategori dan produk di dalamnya tampil di aplikasi kasir POS. Matikan untuk kategori Bahan Baku, Setengah Jadi, atau Keperluan Internal.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = uiState.newCatShowPos,
                        onCheckedChange = onShowPosChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MatahatiMaroon)
                    )
                }
            }
        }

        // Kolom Kanan: Card "Daftar Kategori"
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Title & Search Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Daftar Kategori", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "${uiState.totalResults} Kategori",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box Kategori
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.searchQuery.isEmpty()) {
                                Text(text = "Cari Kategori", fontSize = 13.sp, color = Color(0xFF94A3B8))
                            }
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF0F172A)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Items List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MatahatiMaroon)
                        }
                    } else if (uiState.categoriesList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada kategori.", color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    } else {
                        uiState.categoriesList.forEach { cat ->
                            CategoryCardItem(category = cat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCardItem(category: CategoryItemDto) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Reorder",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category.cname,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${category.productCount})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!category.badgeLabel.isNullOrEmpty()) {
                    val (badgeBg, badgeText) = when (category.badgeLabel) {
                        "Tanpa Stok" -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                        "Sembunyi di POS" -> Pair(Color(0xFFFFF1F2), MatahatiMaroon)
                        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = category.badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =============================================================================
// SHARED TABLE CELL & PAGINATION HELPER
// =============================================================================

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
                    Text("<", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (currentPage > 1) Color(0xFF334155) else Color(0xFF94A3B8))
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
                    Text(">", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (currentPage < lastPage) Color(0xFF334155) else Color(0xFF94A3B8))
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
