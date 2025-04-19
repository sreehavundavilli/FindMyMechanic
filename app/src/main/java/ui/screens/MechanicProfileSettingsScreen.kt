package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicProfileSettingsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val mechanicId = FirebaseAuth.getInstance().currentUser?.uid

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    // Fetch existing mechanic profile
    LaunchedEffect(mechanicId) {
        mechanicId?.let {
            db.collection("mechanics").document(it).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
                    email = doc.getString("email") ?: ""
                    phone = doc.getString("phone") ?: ""
                    location = doc.getString("location") ?: ""
                }
                .addOnFailureListener { e ->
                    Log.e("MechanicProfile", "Failed to load mechanic info", e)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mechanic Profile Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Update your profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Location Field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (mechanicId != null && name.isNotEmpty() && email.isNotEmpty()) {
                        db.collection("mechanics").document(mechanicId)
                            .update(
                                mapOf(
                                    "name" to name,
                                    "email" to email,
                                    "phone" to phone,
                                    "location" to location
                                )
                            )
                            .addOnSuccessListener {
                                statusMessage = "Profile updated successfully!"
                            }
                            .addOnFailureListener {
                                statusMessage = "Error updating profile."
                            }
                    } else {
                        statusMessage = "All fields are required."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    color = if (statusMessage.contains("success")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
