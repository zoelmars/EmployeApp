package com.example.employeeapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.employeeapp.navigation.AppNavGraph
import com.example.employeeapp.ui.theme.theme.EmployeeAppTheme
import com.example.employeeapp.util.SecurityUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()
        setContent {
            EmployeeAppTheme {
                val isRooted = remember { SecurityUtils.isDeviceRooted() }
                var userAcknowledged by remember { mutableStateOf(!isRooted) }

                if (!userAcknowledged) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Peringatan Keamanan") },
                        text = {
                            Text(
                                "Device ini terdeteksi telah di-root atau dimodifikasi.\n\n" +
                                        "Keamanan data sensitif (termasuk data gaji karyawan) tidak dapat dijamin.\n\n" +
                                        "Sangat disarankan untuk tidak menggunakan aplikasi ini di device yang di-root."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { userAcknowledged = true }
                            ) {
                                Text("Lanjutkan (Risiko Sendiri)", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            Button(onClick = { finish() }) {
                                Text("Keluar dari Aplikasi")
                            }
                        }
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EmployeeAppTheme {
        Greeting("Android")
    }
}