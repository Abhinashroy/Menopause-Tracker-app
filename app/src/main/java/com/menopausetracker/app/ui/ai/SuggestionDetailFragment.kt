package com.menopausetracker.app.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.menopausetracker.app.databinding.FragmentSuggestionDetailBinding

class SuggestionDetailFragment : Fragment() {

    private var _binding: FragmentSuggestionDetailBinding? = null
    private val binding get() = _binding!!
    private val args: SuggestionDetailFragmentArgs by navArgs()
    private lateinit var viewModel: AIAssistantViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AIAssistantViewModel::class.java]

        // Display the suggestion title and content
        binding.textSuggestionTitle.text = args.title
        binding.textSuggestionDetail.text = args.content

        // Hide the timestamp in the detail view as requested
        binding.textPromptTimestamp.visibility = View.GONE

        // Set up back navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
