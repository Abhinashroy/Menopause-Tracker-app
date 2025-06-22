package com.menopausetracker.app.ui.ai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.menopausetracker.app.data.model.Suggestion
import com.menopausetracker.app.databinding.FragmentAiPromptBinding

class AIPromptFragment : Fragment() {

    private var _binding: FragmentAiPromptBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AIAssistantViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiPromptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AIAssistantViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup navigation with the cross button in toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set current timestamp
        binding.textTimestamp.text = java.text.SimpleDateFormat(
            "MMMM dd, yyyy • hh:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        // Set up character counter
        binding.editTextPrompt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateCharacterCount(s?.length ?: 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initialize character count
        updateCharacterCount(0)

        // Set up Get Advice button
        binding.buttonGetAiAdvice.setOnClickListener {
            val prompt = binding.editTextPrompt.text.toString().trim()
            viewModel.getAdvice(prompt)
            binding.editTextPrompt.text?.clear()
        }

        // Set up RecyclerView for suggestions
        binding.recyclerSuggestions.layoutManager = LinearLayoutManager(context)
    }

    private fun updateCharacterCount(count: Int) {
        binding.textCharacterCount.text = getString(R.string.ai_character_limit, count)
        binding.buttonGetAiAdvice.isEnabled = count <= 100
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressAi.isVisible = isLoading
            binding.buttonGetAiAdvice.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                viewModel.clearErrors()
            }
        }

        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isNotEmpty()) {
                showSuggestions(suggestions)
            }
        }
    }

    private fun showSuggestions(suggestions: List<Suggestion>) {
        val adapter = SuggestionAdapter(
            suggestions = suggestions,
            clickListener = { suggestion ->
                navigateToSuggestionDetail(suggestion)
            },
            deleteListener = { suggestionId ->
                viewModel.deleteSuggestion(suggestionId)
            }
        )
        binding.recyclerSuggestions.adapter = adapter
    }

    private fun navigateToSuggestionDetail(suggestion: Suggestion) {
        val action = AIPromptFragmentDirections.actionAiPromptToSuggestionDetail(
            suggestion.title,
            suggestion.content
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
