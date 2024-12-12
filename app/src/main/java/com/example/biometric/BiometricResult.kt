package com.example.biometric

import androidx.biometric.BiometricPrompt

sealed interface BiometricResult {
    data object HardwareUnavailable : BiometricResult
    data object FeatureUnavailable : BiometricResult
    data class AuthenticationError(val error: String) : BiometricResult
    data object AuthenticationFailed : BiometricResult
    data class AuthenticationSuccess (val result : BiometricPrompt.AuthenticationResult) : BiometricResult
    data object AuthenticationNotSet : BiometricResult
}