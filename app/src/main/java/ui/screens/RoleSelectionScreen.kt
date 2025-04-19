package anything.infinity.findmymechanic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RoleSelectionScreen(navController: NavController?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Select Your Role",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    navController?.navigate("register/user")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "I am a User")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    navController?.navigate("register/mechanic")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I am a Mechanic")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectionPreview() {
    RoleSelectionScreen(navController = null)
}
