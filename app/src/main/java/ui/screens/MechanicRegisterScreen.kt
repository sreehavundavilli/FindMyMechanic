package anything.infinity.findmymechanic.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MechanicRegisterScreen(navController: NavController?) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mechanic Registration", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
        OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Skills (e.g. 2-wheeler)") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@Button
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val mechanicData = hashMapOf(
                        "name" to name,
                        "phone" to phone,
                        "location" to location,
                        "skills" to skills,
                        "available" to true,
                        "email" to email
                    )

                    db.collection("mechanics").document(uid).set(mechanicData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Mechanic Registered Successfully", Toast.LENGTH_SHORT).show()
                            navController?.navigate("mechanic_dashboard") {
                                popUpTo("register/mechanic") { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController?.navigate("login/mechanic")
        }) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MechanicRegisterScreenPreview() {
    MechanicRegisterScreen(navController = null)
}
