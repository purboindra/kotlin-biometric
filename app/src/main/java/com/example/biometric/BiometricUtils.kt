package com.example.biometric

import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricUtils(private val activity: AppCompatActivity) {
    
    private val resultChannel = Channel<BiometricResult>()
    val promptResults = resultChannel.receiveAsFlow()
    
    private val callBack = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
        }
        
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            
            val debugResult = mapOf(
                "type" to result.authenticationType,
                "cipher" to result.cryptoObject?.cipher,
                "mac" to result.cryptoObject?.mac,
                "signature" to result.cryptoObject?.signature,
                "session" to result.cryptoObject?.presentationSession
            )
            
            println("debugResult: $debugResult")
            
            resultChannel.trySend(BiometricResult.AuthenticationSuccess(result))
            Toast.makeText(
                activity,
                "Authentication success!",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            resultChannel.trySend(BiometricResult.AuthenticationFailed)
        }
        
    }
    
    fun showBiometricPrompt(
        title: String,
        description: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else BIOMETRIC_STRONG
        
        val promptInfo = PromptInfo.Builder().setTitle(title).setDescription(description)
            .setAllowedAuthenticators(authenticators)
        
        if (Build.VERSION.SDK_INT < 30) {
            promptInfo.setNegativeButtonText("Cancel")
        }
        
        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                Toast.makeText(
                    activity,
                    "Biometric hardware unavailable. Try again later.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                Toast.makeText(activity, "No biometric hardware available", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                Toast.makeText(
                    activity,
                    "No biometrics enrolled. Please set up biometrics first.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            
            else -> Unit
        }
        
        val prompt = BiometricPrompt(
            activity,
            callBack
        )
        
        prompt.authenticate(promptInfo.build())
        
    }
    
}