package com.menopausetracker.app.ui.symptoms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.databinding.FragmentSymptomHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SymptomHistoryFragment : Fragment(), SymptomAdapter.SymptomInteractionListener {
    private var _binding: FragmentSymptomHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SymptomHistoryViewModel
    private lateinit var adapter: SymptomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSymptomHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SymptomHistoryViewModel::class.java]

        setupRecyclerView()
        setupUI()
        observeViewModel()

        // Load symptoms when the fragment is created
        viewModel.loadSymptoms()
    }

    private fun setupRecyclerView() {
        adapter = SymptomAdapter(this)
        binding.recyclerViewSymptoms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SymptomHistoryFragment.adapter
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabAddSymptom.setOnClickListener {
            // Use the correct action ID from the navigation graph
            findNavController().navigate(R.id.action_symptom_history_to_edit_symptom)
        }
    }

    private fun observeViewModel() {
        viewModel.symptoms.observe(viewLifecycleOwner) { symptoms ->
            adapter.submitList(symptoms)
            binding.textNoSymptoms.isVisible = symptoms.isEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.symptomDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                Toast.makeText(context, R.string.symptom_deleted, Toast.LENGTH_SHORT).show()
                viewModel.resetSymptomDeleted()
            }
        }
    }

    override fun onEditSymptom(symptom: Symptom) {
        // Create a bundle to pass the symptom ID to the edit fragment
        val bundle = Bundle().apply {
            putLong("symptomId", symptom.id)
        }
        // Navigate using the correct action ID and pass the symptom ID as an argument
        findNavController().navigate(R.id.action_symptom_history_to_edit_symptom, bundle)
    }

    override fun onDeleteSymptom(symptom: Symptom) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation_symptom)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteSymptom(symptom)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
