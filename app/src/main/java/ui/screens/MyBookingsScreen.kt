package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var bookings by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    bookings = result.documents.mapNotNull { it.data }
                    loading = false
                }
                .addOnFailureListener { e ->
                    Log.e("MyBookings", "Error fetching bookings", e)
                    loading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator()
                }

                bookings.isEmpty() -> {
                    Text("No bookings found.")
                }

                else -> {
                    LazyColumn {
                        items(bookings) { booking ->
                            val status = booking["status"] as? String ?: "Pending"
                            val statusColor = when (status) {
                                "Accepted" -> MaterialTheme.colorScheme.primary
                                "Rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Service: ${booking["serviceType"]}", style = MaterialTheme.typography.titleMedium)
                                    Text("Date: ${booking["date"]}")
                                    Text("Time: ${booking["time"]}")
                                    Text(
                                        text = "Status: $status",
                                        color = statusColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

