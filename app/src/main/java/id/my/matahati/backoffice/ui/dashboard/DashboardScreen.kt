package id.my.matahati.backoffice.ui.dashboard

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon
import id.my.matahati.backoffice.ui.theme.MatahatiMaroonContainer
import id.my.matahati.backoffice.ui.theme.MatahatiMaroonDark
import id.my.matahati.backoffice.ui.theme.StatCardCustomers
import id.my.matahati.backoffice.ui.theme.StatCardProducts
import id.my.matahati.backoffice.ui.theme.StatCardSales
import id.my.matahati.backoffice.ui.theme.StatCardTx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar Navigation
        SidebarNavigation(
            selectedMenu = uiState.selectedMenu,
            onMenuSelected = { viewModel.selectMenu(it) },
            onLogoutClick = { showLogoutDialog = true },
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
        )

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
                    DashboardMenu.DASHBOARD -> DashboardMainContent(uiState)
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

                // Section Produk & Inventori
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

                // Section Transaksi & Pembayaran
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

                // Laporan
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
    outlets: List<id.my.matahati.backoffice.model.Outlet>,
    isOutletLocked: Boolean,
    onOutletSelected: (Int, String) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MatahatiMaroon)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title & User Info
            Column {
                Text(
                    text = selectedMenuTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Halo, $userName",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = userRole,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Right Outlet Switcher Dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(enabled = !isOutletLocked) {
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

                // Dropdown Options
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
private fun DashboardMainContent(uiState: DashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MatahatiMaroonContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selamat Datang di Web Office!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatahatiMaroonDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "User: ${uiState.user?.name ?: "-"} (${uiState.user?.email ?: "-"})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Role: ${uiState.user?.roleName ?: "-"} | Outlet Aktif: ${uiState.selectedOutletName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MatahatiMaroon),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.user?.name?.take(1)?.uppercase() ?: "U",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ringkasan Performa Hari Ini",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Stat Summary Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Penjualan Hari Ini",
                value = "Rp 2.450.000",
                icon = Icons.Default.TrendingUp,
                backgroundColor = StatCardSales,
                iconColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Jumlah Transaksi",
                value = "34 Transaksi",
                icon = Icons.Default.ReceiptLong,
                backgroundColor = StatCardTx,
                iconColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Produk Aktif",
                value = "128 Produk",
                icon = Icons.Default.Category,
                backgroundColor = StatCardProducts,
                iconColor = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Pelanggan",
                value = "85 Pelanggan",
                icon = Icons.Default.Groups,
                backgroundColor = StatCardCustomers,
                iconColor = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
