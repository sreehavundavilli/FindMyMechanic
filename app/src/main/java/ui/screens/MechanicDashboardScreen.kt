package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDashboardScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val mechanicId = FirebaseAuth.getInstance().currentUser?.uid
    var mechanicName by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(false) }
    var pendingBookingsCount by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    // Fetch mechanic info and booking requests
    LaunchedEffect(mechanicId) {
        mechanicId?.let {
            try {
                val mechanicDoc = db.collection("mechanics").document(it).get().await()
                mechanicName = mechanicDoc.getString("name") ?: "Mechanic"
                isAvailable = mechanicDoc.getBoolean("available") ?: false

                val bookingsSnapshot = db.collection("bookings")
                    .whereEqualTo("mechanicId", it)
                    .whereEqualTo("status", "Pending")
                    .get().await()

                pendingBookingsCount = bookingsSnapshot.size()
            } catch (e: Exception) {
                Log.e("MechanicDashboard", "Error fetching mechanic data", e)
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mechanic Dashboard") }
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome, $mechanicName",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Availability Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Availability:")
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { newValue ->
                            isAvailable = newValue
                            mechanicId?.let {
                                db.collection("mechanics").document(it)
                                    .update("available", newValue)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = if (isAvailable) "Available ✅" else "Unavailable ❌",
                        color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notification Banner
                if (pendingBookingsCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔔 You have $pendingBookingsCount pending booking(s)!", fontSize = 16.sp)
                            Text("Go to Manage Bookings to respond.", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Manage Bookings Button with Badge
                BadgedBox(
                    badge = {
                        if (pendingBookingsCount > 0) {
                            Badge { Text(pendingBookingsCount.toString()) }
                        }
                    }
                ) {
                    Button(
                        onClick = { navController.navigate("manage_bookings") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Manage Bookings")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Bookings")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                // Profile Settings Button
                Button(
                    onClick = { navController.navigate("mechanic_profile_settings") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile Settings")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profile Settings")
                }
            }
        }
    }
}
