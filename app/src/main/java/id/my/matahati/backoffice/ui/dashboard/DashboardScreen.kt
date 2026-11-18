package id.my.matahati.backoffice.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Output
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.model.Outlet
import id.my.matahati.backoffice.model.TopGroup
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon
import id.my.matahati.backoffice.ui.theme.MatahatiMaroonContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDateFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi Matahati Office?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Ya, Keluar", color = MatahatiMaroon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

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

    Row(modifier = Modifier.fillMaxSize()) {
        // Animated Sidebar Navigation
        AnimatedVisibility(
            visible = uiState.isSidebarVisible,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            SidebarNavigation(
                selectedMenu = uiState.selectedMenu,
                onMenuSelected = { viewModel.selectMenu(it) },
                onLogoutClick = { showLogoutDialog = true },
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
            )
        }

        // Main Content Area (Header + Body)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header / TopBar Maroon
            DashboardTopBar(
                selectedMenuTitle = uiState.selectedMenu.title,
                userName = uiState.user?.name ?: "User",
                userRole = uiState.user?.roleName ?: "Role",
                selectedOutletName = uiState.selectedOutletName,
                outlets = uiState.outlets,
                isOutletLocked = uiState.isOutletLocked || uiState.outlets.size <= 1,
                onToggleSidebar = { viewModel.toggleSidebar() },
                onOutletSelected = { id, name ->
                    viewModel.selectOutlet(id, name)
                }
            )

            // Dynamic Page Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                when (uiState.selectedMenu) {
                    DashboardMenu.DASHBOARD -> DashboardMainContent(
                        uiState = uiState,
                        onOpenDateFilter = { showDateFilterDialog = true },
                        onRefresh = { viewModel.refreshDashboardData() },
                        onToggleCategoryExpand = { viewModel.toggleCategoryExpand(it) }
                    )
                    else -> PlaceholderContent(menuTitle = uiState.selectedMenu.title)
                }
            }
        }
    }
}

@Composable
private fun SidebarNavigation(
    selectedMenu: DashboardMenu,
    onMenuSelected: (DashboardMenu) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Header Sidebar Branding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MatahatiMaroon),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Matahati Office",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatahatiMaroon
                        )
                        Text(
                            text = "Web Office Portal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Menu Items
                SidebarMenuItem(
                    title = DashboardMenu.DASHBOARD.title,
                    icon = Icons.Default.Dashboard,
                    isSelected = selectedMenu == DashboardMenu.DASHBOARD,
                    onClick = { onMenuSelected(DashboardMenu.DASHBOARD) }
                )

                SidebarSectionHeader(title = "Produk & Inventori")
                SidebarMenuItem(
                    title = DashboardMenu.PRODUK.title,
                    icon = Icons.Default.Inventory2,
                    isSelected = selectedMenu == DashboardMenu.PRODUK,
                    onClick = { onMenuSelected(DashboardMenu.PRODUK) }
                )
                SidebarMenuItem(
                    title = DashboardMenu.INVENTORI.title,
                    icon = Icons.Default.Warehouse,
                    isSelected = selectedMenu == DashboardMenu.INVENTORI,
                    onClick = { onMenuSelected(DashboardMenu.INVENTORI) }
                )

                SidebarSectionHeader(title = "Transaksi & Pembayaran")
                SidebarMenuItem(
                    title = DashboardMenu.PENJUALAN.title,
                    icon = Icons.Default.PointOfSale,
                    isSelected = selectedMenu == DashboardMenu.PENJUALAN,
                    onClick = { onMenuSelected(DashboardMenu.PENJUALAN) }
                )
                SidebarMenuItem(
                    title = DashboardMenu.PEMBELIAN.title,
                    icon = Icons.Default.ShoppingCart,
                    isSelected = selectedMenu == DashboardMenu.PEMBELIAN,
                    onClick = { onMenuSelected(DashboardMenu.PEMBELIAN) }
                )

                SidebarSectionHeader(title = "Analisis & Pelanggan")
                SidebarMenuItem(
                    title = DashboardMenu.LAPORAN.title,
                    icon = Icons.Default.Assessment,
                    isSelected = selectedMenu == DashboardMenu.LAPORAN,
                    onClick = { onMenuSelected(DashboardMenu.LAPORAN) }
                )
                SidebarMenuItem(
                    title = DashboardMenu.PELANGGAN_SUPPLIER.title,
                    icon = Icons.Default.People,
                    isSelected = selectedMenu == DashboardMenu.PELANGGAN_SUPPLIER,
                    onClick = { onMenuSelected(DashboardMenu.PELANGGAN_SUPPLIER) }
                )
            }

            // Bottom Logout Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                onClick = onLogoutClick
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Keluar (Logout)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SidebarMenuItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MatahatiMaroon else Color.Transparent
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DashboardTopBar(
    selectedMenuTitle: String,
    userName: String,
    userRole: String,
    selectedOutletName: String,
    outlets: List<Outlet>,
    isOutletLocked: Boolean,
    onToggleSidebar: () -> Unit,
    onOutletSelected: (Int, String) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MatahatiMaroon)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title & Hamburger Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleSidebar) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle Sidebar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Right Outlet Switcher Dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isOutletLocked) {
                            expandedDropdown = !expandedDropdown
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOutletLocked) Icons.Default.Lock else Icons.Default.Storefront,
                            contentDescription = "Outlet",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedOutletName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        if (!isOutletLocked) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (!isOutletLocked) {
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Outlet", fontWeight = FontWeight.Bold) },
                            onClick = {
                                expandedDropdown = false
                                onOutletSelected(0, "Semua Outlet")
                            }
                        )
                        outlets.forEach { outlet ->
                            DropdownMenuItem(
                                text = { Text(outlet.name) },
                                onClick = {
                                    expandedDropdown = false
                                    onOutletSelected(outlet.id, outlet.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMainContent(
    uiState: DashboardUiState,
    onOpenDateFilter: () -> Unit,
    onRefresh: () -> Unit,
    onToggleCategoryExpand: (String) -> Unit
) {
    val dashboardData = uiState.dashboardData
    val filter = dashboardData?.filter
    val summary = dashboardData?.summary
    val topGroups = dashboardData?.topGroups ?: emptyList()
    val activeOutletName = filter?.activeOutletName ?: uiState.selectedOutletName
    val dateLabel = filter?.dateLabel ?: uiState.selectedPreset.label

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Banner Card (Exact style matching Web POS screenshot)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MatahatiMaroon)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Text(
                    text = "Selamat Datang di Backoffice Matahati POS ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Kelola master data, pelanggan, promo, dan menu kasir Anda dengan mudah dari satu tempat.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header Sekilas Outlet & Date Filter Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sekilas $activeOutletName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onOpenDateFilter() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Tanggal",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MatahatiMaroon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading / Error State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MatahatiMaroon)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Memuat data dashboard...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Terjadi kesalahan",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon)
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            }
        } else {
            // Card "Sekilas Toko Anda" (Exact matching Web POS layout)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Left Maroon Block ("Sekilas Toko Anda")
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(
                                color = MatahatiMaroon,
                                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sekilas Toko Anda",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sekilas Toko Anda",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Right 6 Metric Rows
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        MetricRowItem(
                            icon = Icons.Outlined.PersonAdd,
                            label = "Pelanggan Baru",
                            value = "${summary?.pelangganBaru ?: 0}",
                            showDivider = true
                        )
                        MetricRowItem(
                            icon = Icons.Outlined.ShoppingCart,
                            label = "Pesanan",
                            value = "${summary?.pesanan ?: 0}",
                            showDivider = true
                        )
                        MetricRowItem(
                            icon = Icons.Outlined.TrendingUp,
                            label = "Pendapatan",
                            value = formatIdrValue(summary?.pendapatan),
                            showDivider = true
                        )
                        MetricRowItem(
                            icon = Icons.Outlined.AccountBalanceWallet,
                            label = "Pengeluaran",
                            value = formatIdrValue(summary?.pengeluaran),
                            showDivider = true
                        )
                        MetricRowItem(
                            icon = Icons.Outlined.Output,
                            label = "Pengembalian / Pembatalan",
                            value = formatIdrValue(summary?.pengembalian),
                            showDivider = true
                        )
                        MetricRowItem(
                            icon = Icons.Outlined.BarChart,
                            label = "Rata-rata penjualan per transaksi",
                            value = formatIdrValue(summary?.rataRataPenjualan),
                            showDivider = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section Grup Produk Terjual Terbanyak
            Text(
                text = "Grup Produk Terjual Terbanyak",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (topGroups.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data penjualan grup produk pada periode ini.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topGroups.forEach { group ->
                        val isExpanded = uiState.expandedCategoryNames.contains(group.categoryName)
                        TopGroupCard(
                            group = group,
                            isExpanded = isExpanded,
                            onToggleExpand = { onToggleCategoryExpand(group.categoryName) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MetricRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F5F9)
            )
        }
    }
}

@Composable
private fun TopGroupCard(
    group: TopGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.categoryName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${group.totalQty.toInt()} Pcs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MatahatiMaroon
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (group.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MatahatiMaroon,
                        trackColor = MatahatiMaroonContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded && group.products.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Rincian Produk (${group.products.size} Item):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        group.products.forEach { prod ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prod.productName,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${prod.totalQty.toInt()} Pcs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePresetFilterDialog(
    currentPreset: DatePreset,
    currentStart: String,
    currentEnd: String,
    onDismiss: () -> Unit,
    onPresetSelected: (DatePreset, String?, String?) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var apiStart by remember { mutableStateOf(currentStart) }
    var apiEnd by remember { mutableStateOf(currentEnd) }

    var displayStart by remember { mutableStateOf(formatToDisplayDate(currentStart)) }
    var displayEnd by remember { mutableStateOf(formatToDisplayDate(currentEnd)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Tanggal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Stack (Hari Ini, Kemarin, 7 Hari Terakhir, Bulan Ini, Bulan Lalu)
                val presetOptions = listOf(
                    DatePreset.HARI_INI,
                    DatePreset.KEMARIN,
                    DatePreset.TUJUH_HARI_LALU,
                    DatePreset.BULAN_INI,
                    DatePreset.BULAN_LALU
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetOptions.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        val bg = if (isSelected) Color(0xFFFFF1F2) else Color.Transparent
                        val textColor = if (isSelected) MatahatiMaroon else Color(0xFF334155)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPreset = preset
                                    val (newStart, newEnd) = calculatePresetDatesFormatted(preset)
                                    apiStart = newStart
                                    apiEnd = newEnd
                                    displayStart = formatToDisplayDate(newStart)
                                    displayEnd = formatToDisplayDate(newEnd)
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Date Picker Input Row ("Dari" and "Sampai")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Dari" Field
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dari",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = displayStart,
                            onValueChange = { input ->
                                displayStart = input
                                selectedPreset = DatePreset.CUSTOM
                                apiStart = formatToApiDate(input)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Calendar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MatahatiMaroon,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Swap / Arrow Icon
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = "Exchange",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // "Sampai" Field
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sampai",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = displayEnd,
                            onValueChange = { input ->
                                displayEnd = input
                                selectedPreset = DatePreset.CUSTOM
                                apiEnd = formatToApiDate(input)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Calendar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MatahatiMaroon,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "Terapkan" Button
                Button(
                    onClick = {
                        onPresetSelected(selectedPreset, apiStart, apiEnd)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MatahatiMaroon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Terapkan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderContent(menuTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MatahatiMaroonContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MatahatiMaroon,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Halaman $menuTitle",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Halaman $menuTitle - Segera Hadir",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun calculatePresetDatesFormatted(preset: DatePreset): Pair<String, String> {
    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val todayApi = apiFormat.format(cal.time)

    return when (preset) {
        DatePreset.HARI_INI -> Pair(todayApi, todayApi)
        DatePreset.KEMARIN -> {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dateStr = apiFormat.format(cal.time)
            Pair(dateStr, dateStr)
        }
        DatePreset.TUJUH_HARI_LALU -> {
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val startStr = apiFormat.format(cal.time)
            Pair(startStr, todayApi)
        }
        DatePreset.BULAN_INI -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startStr = apiFormat.format(cal.time)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, maxDay)
            val endStr = apiFormat.format(cal.time)
            Pair(startStr, endStr)
        }
        DatePreset.BULAN_LALU -> {
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startStr = apiFormat.format(cal.time)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, maxDay)
            val endStr = apiFormat.format(cal.time)
            Pair(startStr, endStr)
        }
        DatePreset.CUSTOM -> Pair(todayApi, todayApi)
    }
}

private fun formatToDisplayDate(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = apiFormat.parse(dateStr)
        if (date != null) displayFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatToApiDate(dateStr: String): String {
    if (dateStr.isBlank()) return ""
    return try {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = displayFormat.parse(dateStr)
        if (date != null) apiFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatIdrValue(amount: Double?): String {
    val valAmount = amount ?: 0.0
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return "IDR ${formatter.format(valAmount)}"
}
