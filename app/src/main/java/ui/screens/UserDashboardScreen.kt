package anything.infinity.findmymechanic.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var userName by remember { mutableStateOf("User") }
    var location by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("") }
    var mechanicsList by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    // Fetch mechanics based on filters
    fun fetchMechanics() {
        db.collection("mechanics").get()
            .addOnSuccessListener { result ->
                val allMechanics = result.documents.mapNotNull { doc ->
                    val data = doc.data
                    data?.toMutableMap()?.apply { put("uid", doc.id) }
                }


                val filtered = allMechanics.filter { mechanic ->
                    val matchLocation = location.isBlank() || mechanic["location"]?.toString()?.contains(location, ignoreCase = true) == true
                    val matchVehicle = vehicleType.isBlank() || mechanic["vehicleType"]?.toString()?.equals(vehicleType, ignoreCase = true) == true
                    val matchService = selectedService.isBlank() || mechanic["skills"]?.toString()?.contains(selectedService, ignoreCase = true) == true
                    matchLocation || matchVehicle || matchService
                }

                mechanicsList = if (filtered.isNotEmpty()) filtered else allMechanics
            }
            .addOnFailureListener { e ->
                Log.e("UserDashboard", "Error fetching mechanics", e)
            }
    }

    // Fetch user name and preferences
    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "User"
                    location = doc.getString("location") ?: ""
                    vehicleType = doc.getString("vehicleType") ?: ""
                    selectedService = doc.getString("serviceType") ?: ""
                    fetchMechanics()
                }
        }
    }

    // Save filters and refresh mechanics
    fun saveFiltersAndFetch() {
        userId?.let {
            val updates = mapOf(
                "location" to location,
                "vehicleType" to vehicleType,
                "serviceType" to selectedService
            )
            db.collection("users").document(it).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Preferences saved", Toast.LENGTH_SHORT).show()
                    fetchMechanics()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error saving filters: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Welcome, $userName") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("user_profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Your Location", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Enter Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Vehicle Type", fontWeight = FontWeight.Bold)
            Row {
                listOf("Car", "Bike").forEach { type ->
                    Button(
                        onClick = { vehicleType = type },
                        colors = if (vehicleType == type) ButtonDefaults.buttonColors(containerColor = Color.Green)
                        else ButtonDefaults.buttonColors()
                    ) {
                        Text(type)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Service", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Engine", "Oil", "Brakes", "Tires").forEach { service ->
                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { selectedService = service },
                        colors = if (selectedService == service)
                            CardDefaults.cardColors(containerColor = Color.Green)
                        else
                            CardDefaults.cardColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = service,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { saveFiltersAndFetch() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Mechanics")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Available Mechanics", fontWeight = FontWeight.Bold)

            mechanicsList.forEach { mechanic ->
                val mechanicId = mechanic["uid"]?.toString()

                if (!mechanicId.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                navController.navigate("mechanic_profile/$mechanicId")
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = mechanic["name"]?.toString() ?: "Mechanic", fontWeight = FontWeight.Bold)
                            Text(text = mechanic["location"]?.toString() ?: "")
                            Text(text = "Skills: ${mechanic["skills"]?.toString()}")
                            Text(text = "Rating: ${mechanic["rating"] ?: "N/A"}")
                        }
                    }
                }
            }

            Button(
                onClick = { navController.navigate("my_bookings") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("My Bookings")
            }
        }
    }
}
