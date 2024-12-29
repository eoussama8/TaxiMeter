package com.example.taximeter

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taximeter.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
        checkCurrentUser()
        binding.backButton.setOnClickListener {
            // This uses the modern onBackPressedDispatcher instead of deprecated onBackPressed()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            binding.signInButton.isEnabled = false
            signInUser()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.googleSignIn.setOnClickListener {
            // Implement Google Sign-In
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show()
        }



        binding.registerText?.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }

    private fun checkCurrentUser() {
        // If user is already signed in, proceed to main activity
        auth.currentUser?.let {
            navigateToMain()
        }
    }

    private fun signInUser() {
        val email = binding.emailUser.editText?.text?.toString()?.trim() ?: ""
        val password = binding.passwordLayout.editText?.text?.toString()?.trim() ?: ""

        if (!validateInputs(email, password)) {
            binding.signInButton.isEnabled = true
            return
        }

        binding.progressBar?.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar?.visibility = View.GONE
                binding.signInButton.isEnabled = true

                if (task.isSuccessful) {
                    handleSuccessfulSignIn()
                } else {
                    handleSignInError(task.exception)
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                binding.emailUser.error = "Email is required"
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailUser.error = "Please enter a valid email address"
                return false
            }
            password.isEmpty() -> {
                binding.passwordLayout.error = "Password is required"
                return false
            }
            else -> {
                binding.emailUser.error = null
                binding.passwordLayout.error = null
                return true
            }
        }
    }

    private fun handleSuccessfulSignIn() {
        // Clear sensitive data
        binding.passwordLayout.editText?.text?.clear()

        Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    private fun handleSignInError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "No account found with this email"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
            else -> "Sign in failed: ${exception?.message}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}