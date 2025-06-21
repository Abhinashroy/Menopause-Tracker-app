package com.menopausetracker.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

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

        // Setup severity slider
        binding.severitySlider.value = 5f

        // Setup log symptoms button
        binding.buttonLogSymptoms.setOnClickListener {
            val description = binding.editTextSymptoms.text.toString()
            val severity = binding.severitySlider.value.toInt()
            val hotFlashes = binding.chipHotFlashes.isChecked
            val nightSweats = binding.chipNightSweats.isChecked
            val moodChanges = binding.chipMoodChanges.isChecked
            val sleepIssues = binding.chipSleepIssues.isChecked
            val fatigue = binding.chipFatigue.isChecked

            if (description.isNotEmpty() || hotFlashes || nightSweats ||
                moodChanges || sleepIssues || fatigue) {
                viewModel.logSymptom(
                    description,
                    severity,
                    hotFlashes,
                    nightSweats,
                    moodChanges,
                    sleepIssues,
                    fatigue
                )
            } else {
                Toast.makeText(context, R.string.enter_symptoms, Toast.LENGTH_SHORT).show()
            }
        }

        // Setup get recommendations button
        binding.buttonGetRecommendations.setOnClickListener {
            val symptoms = binding.editTextSymptoms.text.toString()

            // Construct a description from selected symptoms
            val selectedSymptoms = mutableListOf<String>()
            if (binding.chipHotFlashes.isChecked) selectedSymptoms.add(getString(R.string.hot_flashes))
            if (binding.chipNightSweats.isChecked) selectedSymptoms.add(getString(R.string.night_sweats))
            if (binding.chipMoodChanges.isChecked) selectedSymptoms.add(getString(R.string.mood_changes))
            if (binding.chipSleepIssues.isChecked) selectedSymptoms.add(getString(R.string.sleep_issues))
            if (binding.chipFatigue.isChecked) selectedSymptoms.add(getString(R.string.fatigue))

            val finalDescription = if (symptoms.isNotEmpty()) {
                if (selectedSymptoms.isEmpty()) symptoms
                else "$symptoms. I'm also experiencing: ${selectedSymptoms.joinToString(", ")}"
            } else if (selectedSymptoms.isNotEmpty()) {
                "I'm experiencing: ${selectedSymptoms.joinToString(", ")}"
            } else {
                ""
            }

            if (finalDescription.isNotEmpty()) {
                viewModel.getRecommendations(finalDescription)
            } else {
                Toast.makeText(context, R.string.enter_symptoms, Toast.LENGTH_SHORT).show()
            }
        }

        // Setup recovery reporting
        binding.buttonReportRecovery.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.feeling_better)
                .setMessage("Are you sure you want to mark yourself as recovered? This will reset your tracking.")
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.reportRecovery()
                    Toast.makeText(context, R.string.recovery_reported, Toast.LENGTH_LONG).show()
                    clearInputs()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun setupAutoComplete() {
        // Get symptom suggestions from resources
        val suggestions = resources.getStringArray(R.array.symptom_suggestions)

        // Create adapter for AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )

        binding.editTextSymptoms.apply {
            setAdapter(adapter)
            threshold = 1 // Show suggestions after typing 1 character
        }
    }

    private fun observeViewModel() {
        // Observe recommendations
        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            binding.textRecommendations.text = recommendations
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        // Observe greeting message
        viewModel.greeting.observe(viewLifecycleOwner) { greeting ->
            binding.textGreeting.text = greeting
        }

        // Observe days count
        viewModel.daysCount.observe(viewLifecycleOwner) { days ->
            binding.textDaysCounter.text = "Days in your journey: $days"
        }

        // Observe tracking status
        viewModel.isTrackingActive.observe(viewLifecycleOwner) { isActive ->
            binding.recoveryCard.isVisible = isActive
            if (!isActive) {
                binding.textDaysCounter.isVisible = false
            } else {
                binding.textDaysCounter.isVisible = true
            }
        }

        // Observe symptom saved confirmation
        viewModel.symptomSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, R.string.symptoms_logged, Toast.LENGTH_SHORT).show()
                clearInputs()
                viewModel.resetSymptomSaved()
            }
        }
    }

    private fun clearInputs() {
        binding.editTextSymptoms.text?.clear()
        binding.chipHotFlashes.isChecked = false
        binding.chipNightSweats.isChecked = false
        binding.chipMoodChanges.isChecked = false
        binding.chipSleepIssues.isChecked = false
        binding.chipFatigue.isChecked = false
        binding.severitySlider.value = 5f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
