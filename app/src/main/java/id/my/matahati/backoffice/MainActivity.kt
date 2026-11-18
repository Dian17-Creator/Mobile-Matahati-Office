package id.my.matahati.backoffice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.my.matahati.backoffice.data.local.SessionManager
import id.my.matahati.backoffice.ui.navigation.AppNavigation
import id.my.matahati.backoffice.ui.theme.MatahatiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager.getInstance(this)

        setContent {
            MatahatiTheme {
                AppNavigation(sessionManager = sessionManager)
            }
        }
    }
}
