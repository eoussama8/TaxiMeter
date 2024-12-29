package com.example.taximeter.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.taximeter.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import androidx.activity.result.contract.ActivityResultContracts

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var profileImageUri: Uri? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        userId = firebaseAuth.currentUser?.uid
        userId?.let { profileViewModel.loadUserProfile(it) }

        setupObservers()
        setupUI()

        return binding.root
    }

    private fun setupObservers() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            binding.firstNameEditText.setText(userProfile.firstName ?: "")
            binding.lastNameEditText.setText(userProfile.lastName ?: "")
            binding.phoneEditText.setText(userProfile.phoneNumber ?: "")

            userProfile.photoUrl?.let { url ->
                Picasso.get().load(url).into(binding.profileImageView)
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.saveResult.observe(viewLifecycleOwner) { isSuccess ->
            Toast.makeText(
                requireContext(),
                if (isSuccess) "Profile saved successfully!" else "Failed to save profile.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupUI() {
        binding.uploadPhotoButton.setOnClickListener {
            openImagePicker()
        }

        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                userId?.let { userId ->
                    profileViewModel.saveUserProfile(
                        userId,
                        binding.firstNameEditText.text?.toString().orEmpty(),
                        binding.lastNameEditText.text?.toString().orEmpty(),
                        binding.phoneEditText.text?.toString().orEmpty(),
                        profileImageUri
                    )
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                profileImageUri = uri
                binding.profileImageView.setImageURI(uri) // Update UI with selected image
            }
        }
    }

    private fun validateInput(): Boolean {
        val firstName = binding.firstNameEditText.text?.toString().orEmpty()
        val lastName = binding.lastNameEditText.text?.toString().orEmpty()
        val phone = binding.phoneEditText.text?.toString().orEmpty()

        return when {
            firstName.isEmpty() -> {
                Toast.makeText(requireContext(), "First name is required.", Toast.LENGTH_SHORT).show()
                false
            }
            lastName.isEmpty() -> {
                Toast.makeText(requireContext(), "Last name is required.", Toast.LENGTH_SHORT).show()
                false
            }
            phone.isEmpty() -> {
                Toast.makeText(requireContext(), "Phone number is required.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
