package anything.infinity.findmymechanic.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(navController: NavController, mechanicId: String) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var mechanicName by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("Engine") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var bookingStatus by remember { mutableStateOf("") }

    // Fetch mechanic name
    LaunchedEffect(mechanicId) {
        db.collection("mechanics").document(mechanicId).get()
            .addOnSuccessListener { doc ->
                mechanicName = doc.getString("name") ?: "Unknown"
            }
            .addOnFailureListener { e ->
                Log.e("BookingConfirmation", "Error fetching mechanic data", e)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Confirmation") },
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
            Text("Mechanic: $mechanicName", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Your Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = serviceType,
                onValueChange = { serviceType = it },
                label = { Text("Service Type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date picker
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Select Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = "$day/${month + 1}/$year"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Time picker
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                label = { Text("Select Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val formattedTime = String.format("%02d:%02d", hour, minute)
                                selectedTime = formattedTime
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedDate.isEmpty() || selectedTime.isEmpty() || location.isEmpty()) {
                        bookingStatus = "Please fill all details."
                    } else {
                        val bookingData = hashMapOf(
                            "userId" to userId,
                            "mechanicId" to mechanicId,
                            "serviceType" to serviceType,
                            "date" to selectedDate,
                            "time" to selectedTime,
                            "location" to location,
                            "status" to "Pending"
                        )
                        db.collection("bookings").add(bookingData)
                            .addOnSuccessListener {
                                bookingStatus = "Booking confirmed successfully!"

                                // Encode values before navigating
                                val encodedMechanicName = URLEncoder.encode(mechanicName, "UTF-8")
                                val encodedServiceType = URLEncoder.encode(serviceType, "UTF-8")
                                val encodedDate = URLEncoder.encode(selectedDate, "UTF-8")
                                val encodedTime = URLEncoder.encode(selectedTime, "UTF-8")
                                val encodedLocation = URLEncoder.encode(location, "UTF-8")

                                try {
                                    navController.navigate(
                                        "booking_success/$encodedMechanicName/$encodedServiceType/$encodedDate/$encodedTime/$encodedLocation"
                                    )
                                } catch (e: Exception) {
                                    Log.e("BookingConfirmation", "Navigation failed: ${e.message}")
                                }
                            }
                            .addOnFailureListener { e ->
                                bookingStatus = "Error saving booking."
                                Log.e("BookingConfirmation", "Error saving booking", e)
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Booking")
            }

            if (bookingStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = bookingStatus,
                    color = if (bookingStatus.contains("successfully")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
