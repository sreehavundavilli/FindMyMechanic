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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicProfileScreen(navController: NavController, mechanicId: String) {
    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(true) }
    var mechanicData by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Fetch mechanic data from Firestore
    LaunchedEffect(mechanicId) {
        db.collection("mechanics").document(mechanicId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    mechanicData = doc.data
                } else {
                    Log.e("MechanicProfile", "No such document")
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("MechanicProfile", "Error fetching mechanic data", e)
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mechanic Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            mechanicData?.let { data ->
                val name = data["name"]?.toString() ?: "Unknown"
                val skills = data["skills"]?.toString() ?: "No skills listed"
                val location = data["location"]?.toString() ?: "Unknown"
                val rating = data["rating"]?.toString() ?: "N/A"
                val imageUrl = data["profileImageUrl"]?.toString()
                    ?: "https://i.pravatar.cc/150"

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mechanic Profile Image
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Mechanic Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.Gray, shape = RoundedCornerShape(60.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mechanic Name
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Location
                    Text("Location: $location", fontSize = 16.sp)

                    // Skills
                    Text("Skills: $skills", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating
                    Text("Rating: ⭐ $rating", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Request Service Button
                    Button(
                        onClick = {
                            navController.navigate("booking_confirmation/$mechanicId")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Service")
                    }
                }
            } ?: run {
                // Mechanic not found or error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mechanic data not found.", color = Color.Red)
                }
            }
        }
    }
}
