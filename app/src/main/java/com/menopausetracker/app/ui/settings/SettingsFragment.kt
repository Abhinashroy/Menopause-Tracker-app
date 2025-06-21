package com.menopausetracker.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentSettingsBinding
import com.menopausetracker.app.util.FontSizeManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup font size settings
        setupFontSizeSettings()

        // Save settings when button is clicked
        binding.btnSaveSettings.setOnClickListener {
            saveFontSizeSettings()
        }
    }

    private fun setupFontSizeSettings() {
        // Get saved font size preference using FontSizeManager
        val fontSize = FontSizeManager.getCurrentFontSize(requireContext())

        // Set the correct radio button based on saved preference
        when (fontSize) {
            FontSizeManager.FONT_SIZE_SMALL -> binding.radioSmall.isChecked = true
            FontSizeManager.FONT_SIZE_NORMAL -> binding.radioNormal.isChecked = true
            FontSizeManager.FONT_SIZE_LARGE -> binding.radioLarge.isChecked = true
        }
    }

    private fun saveFontSizeSettings() {
        var selectedFontSize = FontSizeManager.FONT_SIZE_NORMAL

        if (binding.radioSmall.isChecked) {
            selectedFontSize = FontSizeManager.FONT_SIZE_SMALL
        } else if (binding.radioNormal.isChecked) {
            selectedFontSize = FontSizeManager.FONT_SIZE_NORMAL
        } else if (binding.radioLarge.isChecked) {
            selectedFontSize = FontSizeManager.FONT_SIZE_LARGE
        }

        // Save font size preference
        val sharedPrefs = requireActivity().getSharedPreferences(
            "app_preferences", Context.MODE_PRIVATE
        )
        sharedPrefs.edit().putString("font_size", selectedFontSize).apply()

        // Show confirmation message
        binding.settingsSavedMessage.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

