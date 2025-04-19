package anything.infinity.findmymechanic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)



@Composable

fun BookingSuccessScreen(
    navController: NavController,
    mechanicName: String,
    serviceType: String,
    date: String,
    time: String,
    location: String
) {
    val decodedMechanicName = URLDecoder.decode(mechanicName, StandardCharsets.UTF_8.toString())
    val decodedServiceType = URLDecoder.decode(serviceType, StandardCharsets.UTF_8.toString())
    val decodedDate = URLDecoder.decode(date, StandardCharsets.UTF_8.toString())
    val decodedTime = URLDecoder.decode(time, StandardCharsets.UTF_8.toString())
    val decodedLocation = URLDecoder.decode(location, StandardCharsets.UTF_8.toString())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Confirmed") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🎉 Booking Successful!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Mechanic: $decodedMechanicName")
            Text(text = "Service: $decodedServiceType")
            Text(text = "Date: $decodedDate")
            Text(text = "Time: $decodedTime")
            Text(text = "Location: $decodedLocation")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                navController.navigate("user_dashboard") {
                    popUpTo("user_dashboard") { inclusive = true }
                }
            }) {
                Text("Back to Dashboard")
            }
        }
    }
}


