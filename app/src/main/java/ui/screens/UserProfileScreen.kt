package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    // Fetch user data
    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: ""
                    userEmail = doc.getString("email") ?: ""
                    profileImageUrl = doc.getString("profileImageUrl")
                    newName = userName
                    newEmail = userEmail
                }
                .addOnFailureListener { e ->
                    Log.e("UserProfileScreen", "Error fetching user data", e)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Profile Image
            profileImageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(60.dp))
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit User Name
            Text("Name", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Edit Email
            Text("Email", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Changes Button
            Button(
                onClick = {
                    // Update user info in Firestore
                    userId?.let {
                        val userMap = hashMapOf(
                            "name" to newName,
                            "email" to newEmail
                        )
                        db.collection("users").document(it).update(userMap as Map<String, Any>)
                            .addOnSuccessListener {
                                // Show a confirmation or navigate back
                                Log.d("UserProfileScreen", "User info updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserProfileScreen", "Error updating user info", e)
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

