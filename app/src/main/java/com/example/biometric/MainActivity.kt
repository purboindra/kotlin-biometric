package com.example.biometric

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.biometric.ui.theme.BiometricTheme

class MainActivity : AppCompatActivity() {
    
    private val biometricUtils by lazy {
        BiometricUtils(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            BiometricTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        
                        val biometricResult by biometricUtils.promptResults.collectAsState(
                            initial = null
                        )
                        
                        val enrollLauncher =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
                                onResult = {
                                    println("Activity result: $it")
                                })
                        
                        
                        LaunchedEffect(biometricResult) {
                            if (biometricResult is BiometricResult.AuthenticationNotSet) {
                                if (Build.VERSION.SDK_INT >= 30) {
                                    val enrollIntent =
                                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                            putExtra(
                                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                            )
                                        }
                                    enrollLauncher.launch(enrollIntent)
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                biometricUtils.showBiometricPrompt(
                                    title = "Level up authenticate method!",
                                    description = "Hello...."
                                )
                            }) {
                                Text(text = "Authenticate")
                            }
                        }
                        
                        biometricResult?.let { result ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .safeContentPadding()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (result) {
                                        is BiometricResult.AuthenticationError -> {
                                            result.error
                                        }
                                        
                                        BiometricResult.AuthenticationFailed -> {
                                            "Authentication failed"
                                        }
                                        
                                        BiometricResult.AuthenticationNotSet -> {
                                            "Authentication not set"
                                        }
                                        
                                        is BiometricResult.AuthenticationSuccess -> {
                                            "Authentication Success: ${result.result}"
                                        }
                                        
                                        BiometricResult.FeatureUnavailable -> {
                                            "Feature unavailable"
                                        }
                                        
                                        BiometricResult.HardwareUnavailable -> {
                                            "Hardware unavailable"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricTheme {
        Greeting("Android")
    }
}