package anything.infinity.findmymechanic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import anything.infinity.findmymechanic.ui.screens.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import anything.infinity.findmymechanic.ui.screens.ManageBookingsScreen





@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {

        // Splash + Role Selection
        composable("splash") {
            SplashScreen(navController)
        }
        composable("role_selection") {
            RoleSelectionScreen(navController)
        }

        // User Auth
        composable("register/user") {
            UserRegisterScreen(navController)
        }
        composable("login/user") {
            UserLoginScreen(navController)
        }

        // Mechanic Auth
        composable("register/mechanic") {
            MechanicRegisterScreen(navController)
        }
        composable("login/mechanic") {
            MechanicLoginScreen(navController)
        }

        // User Side
        composable("user_dashboard") {
            UserDashboardScreen(navController)
        }
        composable("user_profile") {
            UserProfileScreen(navController)
        }

        composable("mechanic_profile/{mechanicId}") { backStackEntry ->
            val mechanicId = backStackEntry.arguments?.getString("mechanicId") ?: ""
            MechanicProfileScreen(navController = navController, mechanicId = mechanicId)
        }

        composable("booking_confirmation/{mechanicId}") { backStackEntry ->
            val mechanicId = backStackEntry.arguments?.getString("mechanicId") ?: "default_id"
            BookingConfirmationScreen(navController, mechanicId)
        }
        composable("my_bookings") {
            MyBookingsScreen(navController)
        }

        // Mechanic Side
        composable("mechanic_dashboard") {
            MechanicDashboardScreen(navController)
        }

        composable("manage_bookings") {
            ManageBookingsScreen(navController)
        }



        composable("mechanic_profile_settings") {
            MechanicProfileSettingsScreen(navController)
        }
        composable(
            route = "booking_success/{mechanicName}/{serviceType}/{date}/{time}/{location}",
            arguments = listOf(
                navArgument("mechanicName") { type = NavType.StringType },
                navArgument("serviceType") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("time") { type = NavType.StringType },
                navArgument("location") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            BookingSuccessScreen(
                navController = navController,
                mechanicName = backStackEntry.arguments?.getString("mechanicName") ?: "",
                serviceType = backStackEntry.arguments?.getString("serviceType") ?: "",
                date = backStackEntry.arguments?.getString("date") ?: "",
                time = backStackEntry.arguments?.getString("time") ?: "",
                location = backStackEntry.arguments?.getString("location") ?: ""
            )
        }
        composable("user_notifications") {
            UserNotificationsScreen()
        }


    }
}



