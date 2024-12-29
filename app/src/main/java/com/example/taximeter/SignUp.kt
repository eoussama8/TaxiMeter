package com.example.taximeter

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taximeter.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signInButton.setOnClickListener {
            // Disable button to prevent multiple clicks
            binding.signInButton.isEnabled = false
            registerUser()
        }
        binding.backButton.setOnClickListener {
            // Navigate back to SignIn activity
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }
    }

    private fun registerUser() {
        val email = binding.emailUser.editText?.text?.toString()?.trim() ?: ""
        val pwd = binding.passwordLayout.editText?.text?.toString()?.trim() ?: ""
        val cpwd = binding.ConfirmPasswordText.editText?.text?.toString()?.trim() ?: ""

        if (!validateInputs(email, pwd, cpwd)) {
            binding.signInButton.isEnabled = true
            return
        }

        // Show progress indicator
        binding.progressBar?.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                binding.progressBar?.visibility = View.GONE
                binding.signInButton.isEnabled = true

                if (task.isSuccessful) {
                    handleSuccessfulRegistration()
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun validateInputs(email: String, pwd: String, cpwd: String): Boolean {
        when {
            email.isEmpty() -> {
                binding.emailUser.error = "Email is required"
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailUser.error = "Please enter a valid email address"
                return false
            }
            pwd.isEmpty() -> {
                binding.passwordLayout.error = "Password is required"
                return false
            }
            pwd.length < 8 -> {
                binding.passwordLayout.error = "Password must be at least 8 characters"
                return false
            }
            cpwd.isEmpty() -> {
                binding.ConfirmPasswordText.error = "Please confirm your password"
                return false
            }
            pwd != cpwd -> {
                binding.ConfirmPasswordText.error = "Passwords do not match"
                return false
            }
            else -> {
                // Clear any previous errors
                binding.emailUser.error = null
                binding.passwordLayout.error = null
                binding.ConfirmPasswordText.error = null
                return true
            }
        }
    }

    private fun handleSuccessfulRegistration() {
        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

        // Clear sensitive data
        binding.passwordLayout.editText?.text?.clear()
        binding.ConfirmPasswordText.editText?.text?.clear()

        // Navigate to SignIn activity
        val intent = Intent(this, SignIn::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
            is FirebaseAuthUserCollisionException -> "Email already in use"
            else -> "Registration failed: ${exception?.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}