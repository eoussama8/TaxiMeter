package com.example.taximeter.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.taximeter.R
import com.example.taximeter.databinding.FragmentSettingsBinding

import java.util.*

class SettingsFragment : Fragment() {
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        // Observe the language LiveData
        viewModel.language.observe(viewLifecycleOwner, { languageCode ->
            languageCode?.let {
                updateLanguage(it)
            }
        })

        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.englishButton).setOnClickListener {
            viewModel.setLanguage("en")
        }

        view.findViewById<Button>(R.id.frenchButton).setOnClickListener {
            viewModel.setLanguage("fr")
        }

        view.findViewById<Button>(R.id.arabicButton).setOnClickListener {
            viewModel.setLanguage("ar")
        }
    }

    private fun updateLanguage(languageCode: String) {
        context?.let { ctx ->
            LocaleHelper.setLocale(ctx, languageCode)
            requireActivity().apply {
                finish()
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }
}
