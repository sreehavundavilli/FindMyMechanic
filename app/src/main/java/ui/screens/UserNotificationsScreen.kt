package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNotificationsScreen() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    var notifications by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    // Fetch notifications
    LaunchedEffect(userId) {
        userId?.let {
            db.collection("notifications")
                .whereEqualTo("userId", it)
                .get()
                .addOnSuccessListener { result ->
                    notifications = result.documents.mapNotNull { it.data }
                        .sortedByDescending { it["timestamp"] as? Long ?: 0L }
                }
                .addOnFailureListener { e ->
                    Log.e("UserNotifications", "Failed to load notifications", e)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Your Notifications") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (notifications.isEmpty()) {
                Text("No notifications yet.")
            } else {
                LazyColumn {
                    items(notifications) { notification ->
                        NotificationItem(notification)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Map<String, Any>) {
    val message = notification["message"] as? String ?: "No message"
    val timestamp = notification["timestamp"] as? Long ?: 0L
    val dateTime = remember(timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(message, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
