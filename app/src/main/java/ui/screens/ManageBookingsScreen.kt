package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBookingsScreen(navController: NavController) {
    val mechanicId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    var bookings by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(mechanicId) {
        mechanicId?.let {
            db.collection("bookings")
                .whereEqualTo("mechanicId", it)
                .get()
                .addOnSuccessListener { result ->
                    bookings = result.documents.mapNotNull { doc ->
                        doc.data?.plus("bookingId" to doc.id)
                    }.sortedBy { booking ->
                        try {
                            val dateTimeStr = "${booking["date"]} ${booking["time"]}"
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateTimeStr)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("ManageBookings", "Failed to fetch bookings", e)
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Bookings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (bookings.isEmpty()) {
                Text("No bookings yet.")
            } else {
                LazyColumn {
                    items(bookings) { booking ->
                        BookingItem(booking, db) {
                            // Refresh on update
                            mechanicId?.let {
                                db.collection("bookings")
                                    .whereEqualTo("mechanicId", it)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        bookings = result.documents.mapNotNull { doc ->
                                            doc.data?.plus("bookingId" to doc.id)
                                        }.sortedBy { booking ->
                                            try {
                                                val dateTimeStr = "${booking["date"]} ${booking["time"]}"
                                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateTimeStr)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: Map<String, Any>,
    db: FirebaseFirestore,
    onStatusUpdated: () -> Unit
) {
    val bookingId = booking["bookingId"] as? String ?: return
    val userId = booking["userId"] as? String ?: "Unknown"
    val serviceType = booking["serviceType"] as? String ?: "N/A"
    val date = booking["date"] as? String ?: "N/A"
    val time = booking["time"] as? String ?: "N/A"
    val status = booking["status"] as? String ?: "Pending"

    var showDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User ID: $userId", fontWeight = FontWeight.Bold)
            Text("Service: $serviceType")
            Text("Date: $date")
            Text("Time: $time")
            StatusBadge(status)

            if (status == "Pending") {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedStatus = "Accepted"
                            showDialog = true
                        }
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = {
                            selectedStatus = "Rejected"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to mark this booking as $selectedStatus?") },
            confirmButton = {
                TextButton(onClick = {
                    updateBookingStatus(
                        db, bookingId, selectedStatus, userId, date, time, onStatusUpdated
                    )
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Accepted" -> Color(0xFF4CAF50)
        "Rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 4.dp)
            .background(color, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status, color = Color.White)
    }
}

fun updateBookingStatus(
    db: FirebaseFirestore,
    bookingId: String,
    newStatus: String,
    userId: String,
    date: String,
    time: String,
    onComplete: () -> Unit
) {
    db.collection("bookings").document(bookingId)
        .update("status", newStatus)
        .addOnSuccessListener {
            Log.d("ManageBookings", "Status updated to $newStatus")

            val message = "Your booking on $date at $time has been $newStatus"
            db.collection("notifications").add(
                mapOf(
                    "userId" to userId,
                    "message" to message,
                    "timestamp" to System.currentTimeMillis()
                )
            )

            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("ManageBookings", "Failed to update status", e)
        }
}
