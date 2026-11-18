package id.my.matahati.backoffice.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.my.matahati.backoffice.data.local.SessionManager
import id.my.matahati.backoffice.data.remote.RetrofitClient
import id.my.matahati.backoffice.repository.AuthRepository
import id.my.matahati.backoffice.ui.dashboard.DashboardScreen
import id.my.matahati.backoffice.ui.dashboard.DashboardViewModel
import id.my.matahati.backoffice.ui.login.LoginScreen
import id.my.matahati.backoffice.ui.login.LoginViewModel
import id.my.matahati.backoffice.ui.theme.MatahatiMaroon

object Screen {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val apiService = RetrofitClient.getApiService(sessionManager)
    val authRepository = AuthRepository(apiService, sessionManager)

    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH
    ) {
        composable(Screen.SPLASH) {
            SplashScreen(
                authRepository = authRepository,
                onSessionChecked = { isLoggedIn ->
                    val destination = if (isLoggedIn) Screen.DASHBOARD else Screen.LOGIN
                    navController.navigate(destination) {
                        popUpTo(Screen.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(authRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.DASHBOARD) {
                        popUpTo(Screen.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(authRepository)
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onLogoutSuccess = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun SplashScreen(
    authRepository: AuthRepository,
    onSessionChecked: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        val isLoggedIn = authRepository.checkSession()
        onSessionChecked(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatahatiMaroon),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Matahati Office",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Web Office Management",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
