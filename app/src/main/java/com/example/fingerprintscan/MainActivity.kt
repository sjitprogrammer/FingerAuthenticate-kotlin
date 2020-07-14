package com.example.fingerprintscan

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                userNotify("Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                userNotify("Authentication Success")
                startActivity(Intent(this@MainActivity, SecretActivity::class.java))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBiometricSupport()
        btn_auth.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val biometricPrompt: BiometricPrompt =
                    BiometricPrompt.Builder(this)
                        .setTitle("My App's Authentication")
                        .setSubtitle("Please login to get access")
                        .setDescription("My App is using Android biometric authentication")
                        .setNegativeButton(
                            "Cancel",
                            this.mainExecutor,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                userNotify("Authentication cancelled")
                            }).build()
                biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
            } else {
                userNotify("Your android version not supported ")
            }


        }
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            userNotify("Authentication was cancell by user!")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            userNotify("Fingerprint Auth has not been enable in setting")
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            userNotify("Fingerprint Auth permission is not enabled")
            return false
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun userNotify(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
