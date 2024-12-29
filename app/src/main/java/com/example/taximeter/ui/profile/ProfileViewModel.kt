package com.example.taximeter.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")

    val userProfile = MutableLiveData<UserProfile>()
    val errorMessage = MutableLiveData<String>()
    val saveResult = MutableLiveData<Boolean>()

    fun loadUserProfile(userId: String) {
        userRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserProfile::class.java)
                        if (user != null) {
                            userProfile.value = user
                            Log.d("ProfileViewModel", "User data loaded: $user")
                        } else {
                            errorMessage.value = "Failed to parse user data"
                            Log.e("ProfileViewModel", "Failed to parse user data")
                        }
                    } else {
                        errorMessage.value = "User not found!"
                        Log.e("ProfileViewModel", "User with ID $userId not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage.value = "Error: ${error.message}"
                    Log.e("ProfileViewModel", "Database error: ${error.message}")
                }
            })
    }

    fun saveUserProfile(
        userId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUri: Uri?
    ) {
        if (firstName.isBlank() || lastName.isBlank() || phoneNumber.isBlank()) {
            errorMessage.value = "All fields are required"
            saveResult.value = false
            return
        }

        val user = UserProfile(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            photoUrl = profileImageUri?.toString()
        )

        userRef.child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveResult.value = true
                    Log.d("ProfileViewModel", "User profile saved successfully")
                } else {
                    saveResult.value = false
                    errorMessage.value = "Error: ${task.exception?.message}"
                    Log.e("ProfileViewModel", "Save error: ${task.exception?.message}")
                }
            }
    }

}