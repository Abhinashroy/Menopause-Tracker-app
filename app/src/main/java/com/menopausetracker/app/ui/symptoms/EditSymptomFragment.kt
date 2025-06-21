package com.menopausetracker.app.ui.symptoms

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
import androidx.navigation.fragment.navArgs
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentEditSymptomBinding

class EditSymptomFragment : Fragment() {
    private var _binding: FragmentEditSymptomBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditSymptomViewModel
    private val args: EditSymptomFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSymptomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EditSymptomViewModel::class.java]

        // Check if we're editing an existing symptom
        val symptomId = args.symptomId
        if (symptomId > 0) {
            viewModel.loadSymptom(symptomId)
        }

        setupUI()
        setupAutoComplete()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.severitySlider.addOnChangeListener { _, value, _ ->
            updateSeverityText(value.toInt())
        }

        // Set initial severity
        binding.severitySlider.value = 5f
        updateSeverityText(5)

        binding.buttonSave.setOnClickListener {
            saveSymptom()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
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
        viewModel.symptom.observe(viewLifecycleOwner) { symptom ->
            symptom?.let {
                binding.editTextSymptoms.setText(it.description)
                binding.severitySlider.value = it.severity.toFloat()
                binding.chipHotFlashes.isChecked = it.hotFlashes
                binding.chipNightSweats.isChecked = it.nightSweats
                binding.chipMoodChanges.isChecked = it.moodChanges
                binding.chipSleepIssues.isChecked = it.sleepIssues
                binding.chipFatigue.isChecked = it.fatigue

                updateSeverityText(it.severity)

                // Update toolbar title if editing
                binding.toolbar.title = getString(R.string.edit_symptom)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.symptomSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, R.string.symptom_updated, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.resetSymptomSaved()
            }
        }
    }

    private fun saveSymptom() {
        val description = binding.editTextSymptoms.text.toString()
        val severity = binding.severitySlider.value.toInt()
        val hotFlashes = binding.chipHotFlashes.isChecked
        val nightSweats = binding.chipNightSweats.isChecked
        val moodChanges = binding.chipMoodChanges.isChecked
        val sleepIssues = binding.chipSleepIssues.isChecked
        val fatigue = binding.chipFatigue.isChecked

        if (description.isNotEmpty() || hotFlashes || nightSweats ||
            moodChanges || sleepIssues || fatigue) {
            viewModel.saveSymptom(
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

    private fun updateSeverityText(severity: Int) {
        binding.textSeverityValue.text = getString(R.string.severity_label, severity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
