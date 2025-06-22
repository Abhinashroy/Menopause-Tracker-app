package com.menopausetracker.app.ui.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    // Store selected symptoms
    private val selectedSymptoms = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setupUI()
        setupAutoComplete()
        observeViewModel()
    }

    private fun setupUI() {
        // Add navigation to symptom history
        binding.buttonViewHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_symptom_history)
        }

        // Setup severity slider with updated scale (1-5)
        binding.severitySlider.value = 3f // Default to middle value

        // Setup common symptom chips
        binding.chipHotFlashes.setOnClickListener {
            addSymptomToSelection(getString(R.string.hot_flashes))
        }
        binding.chipNightSweats.setOnClickListener {
            addSymptomToSelection(getString(R.string.night_sweats))
        }
        binding.chipMoodChanges.setOnClickListener {
            addSymptomToSelection(getString(R.string.mood_changes))
        }
        binding.chipSleepIssues.setOnClickListener {
            addSymptomToSelection(getString(R.string.sleep_issues))
        }
        binding.chipFatigue.setOnClickListener {
            addSymptomToSelection(getString(R.string.fatigue))
        }

        // Setup log symptoms button
        binding.buttonLogSymptoms.setOnClickListener {
            val severity = binding.severitySlider.value.toInt()

            if (selectedSymptoms.isNotEmpty()) {
                // Common symptoms flags for backward compatibility
                val hotFlashes = selectedSymptoms.contains(getString(R.string.hot_flashes))
                val nightSweats = selectedSymptoms.contains(getString(R.string.night_sweats))
                val moodChanges = selectedSymptoms.contains(getString(R.string.mood_changes))
                val sleepIssues = selectedSymptoms.contains(getString(R.string.sleep_issues))
                val fatigue = selectedSymptoms.contains(getString(R.string.fatigue))

                // Create description from all selected symptoms
                val description = selectedSymptoms.joinToString(", ")

                viewModel.logSymptom(
                    description,
                    severity,
                    hotFlashes,
                    nightSweats,
                    moodChanges,
                    sleepIssues,
                    fatigue
                )

                // Clear selections after logging
                clearSelectedSymptoms()
            } else {
                Toast.makeText(context, R.string.enter_symptoms, Toast.LENGTH_SHORT).show()
            }
        }

        // Setup get recommendations button
        binding.buttonGetRecommendations.setOnClickListener {
            // Always get recommendations - either from selected symptoms or history
            val description = if (selectedSymptoms.isNotEmpty()) {
                selectedSymptoms.joinToString(", ")
            } else {
                // Empty string will trigger the repository to use historical symptoms
                ""
            }
            viewModel.getRecommendations(description)
        }

        // Setup "I feel cured" button click handler
        binding.buttonReportRecovery.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_recovery)
                .setMessage(R.string.recovery_confirmation_message)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    viewModel.reportRecovery()
                    Toast.makeText(context, R.string.recovery_reported, Toast.LENGTH_LONG).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        // Setup AI Assistant navigation
        binding.buttonAiAssistant?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ai_prompt)
        }
    }

    private fun setupAutoComplete() {
        // Get symptom suggestions from resources
        val symptomSuggestions = resources.getStringArray(R.array.symptom_suggestions)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            symptomSuggestions
        )

        // Set up the AutoCompleteTextView for symptom search
        val autoCompleteTextView = binding.editTextSymptoms
        autoCompleteTextView.setAdapter(adapter)

        // Handle symptom selection
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedSymptom = adapter.getItem(position).toString()
            addSymptomToSelection(selectedSymptom)
            autoCompleteTextView.text.clear()
        }
    }

    private fun addSymptomToSelection(symptom: String) {
        // Only add if not already selected
        if (!selectedSymptoms.contains(symptom)) {
            selectedSymptoms.add(symptom)
            addChip(symptom)
        }
    }

    private fun addChip(symptom: String) {
        val chip = Chip(context).apply {
            text = symptom
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.selectedSymptomsChipGroup.removeView(this)
                selectedSymptoms.remove(symptom)
            }
        }
        binding.selectedSymptomsChipGroup.addView(chip)
    }

    private fun clearSelectedSymptoms() {
        selectedSymptoms.clear()
        binding.selectedSymptomsChipGroup.removeAllViews()
        binding.severitySlider.value = 3f
    }

    private fun observeViewModel() {
        viewModel.greeting.observe(viewLifecycleOwner) { greeting ->
            binding.textGreeting.text = greeting
        }

        viewModel.daysCount.observe(viewLifecycleOwner) { days ->
            binding.textDaysCounter.text = getString(R.string.days_in_journey, days)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            if (recommendations != null) {
                // Use Html.fromHtml to render the formatted text
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    binding.textRecommendations.text = Html.fromHtml(recommendations, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    binding.textRecommendations.text = Html.fromHtml(recommendations)
                }
            } else {
                binding.textRecommendations.text = ""
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.symptomSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, R.string.symptoms_logged, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
