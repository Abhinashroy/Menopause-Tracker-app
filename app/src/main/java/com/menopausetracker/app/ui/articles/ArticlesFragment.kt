package com.menopausetracker.app.ui.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentArticlesBinding

class ArticlesFragment : Fragment() {
    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ArticlesViewModel
    private lateinit var adapter: ArticleAdapter

    // Add this variable to track if we're resuming from another fragment
    private var isResuming = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel with retained state
        viewModel = ViewModelProvider(requireActivity()).get(ArticlesViewModel::class.java)

        // Setup RecyclerView and adapter
        setupRecyclerView()

        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshArticles(true)
        }

        // Setup saved articles button - fixed to show saved articles
        binding.fabSavedArticles.setOnClickListener {
            // Toggle between showing all articles and saved articles
            if (viewModel.isShowingSavedArticles()) {
                // If currently showing saved articles, show all articles instead
                viewModel.showAllArticles()
                binding.fabSavedArticles.setImageResource(R.drawable.ic_saved)
            } else {
                // If showing all articles, switch to showing saved articles
                viewModel.showSavedArticles()
                binding.fabSavedArticles.setImageResource(R.drawable.ic_article)
            }
        }

        // Update FAB icon based on current state
        updateSavedArticlesFabIcon()

        // Observe articles data
        observeViewModel()
    }

    /**
     * Update the FAB icon to reflect the current state (saved or all articles)
     */
    private fun updateSavedArticlesFabIcon() {
        val iconRes = if (viewModel.isShowingSavedArticles()) {
            R.drawable.ic_article
        } else {
            R.drawable.ic_saved
        }
        binding.fabSavedArticles.setImageResource(iconRes)
    }

    private fun setupRecyclerView() {
        adapter = ArticleAdapter(
            onArticleClick = { article ->
                // Navigate to article detail with action and argument
                val action = ArticlesFragmentDirections.actionArticlesToArticleDetail(article.id)
                findNavController().navigate(action)
            },
            onSaveClick = { article ->
                // Toggle article saved status
                viewModel.toggleArticleSaved(article)

                // Show feedback to user
                val message = if (article.isSaved)
                    "Article removed from saved"
                else
                    "Article saved"

                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.articlesRecyclerView.adapter = adapter
        binding.articlesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupCategoryChips() {
        // Clear the category chip group - no categories will be displayed
        binding.categoryChipGroup.removeAllViews()

        // Since we're not displaying categories anymore, there's no need to set a default chip
    }

    private fun observeViewModel() {
        // Observe articles
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)

            // Show "No articles Found" message when the list is empty
            binding.emptyStateTextView.isVisible = articles.isEmpty()
            if (articles.isEmpty()) {
                binding.emptyStateTextView.text = "No articles Found. Check Your Internet Connection"
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.isVisible = isLoading && adapter.itemCount == 0
        }

        // Observe error messages but don't show them - the empty state text will handle this
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            // Don't show any error messages - the empty state text will handle this
            // No UI updates needed as we're ignoring error messages
        }
    }

    /**
     * Called when the fragment becomes visible or returns from another screen
     */
    override fun onResume() {
        super.onResume()

        if (isResuming) {
            // If returning from another fragment, refresh articles
            // but don't force network refresh to avoid unnecessary loading
            if (viewModel.isShowingSavedArticles()) {
                viewModel.refreshSavedArticles()
            } else {
                viewModel.refreshArticles(false)
            }
        }

        isResuming = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
