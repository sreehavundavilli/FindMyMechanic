package anything.infinity.findmymechanic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import anything.infinity.findmymechanic.navigation.NavGraph
import anything.infinity.findmymechanic.ui.theme.FindMyMechanicTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindMyMechanicTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
