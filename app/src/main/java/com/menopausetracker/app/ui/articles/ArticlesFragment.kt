package com.menopausetracker.app.ui.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentArticlesBinding

class ArticlesFragment : Fragment() {
    private var _binding: FragmentArticlesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ArticlesViewModel
    private lateinit var adapter: ArticlesAdapter

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
        viewModel = ViewModelProvider(this)[ArticlesViewModel::class.java]

        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ArticlesAdapter(
            onSaveClick = { article ->
                viewModel.toggleSaveArticle(article)
            },
            onArticleClick = { article ->
                navigateToArticleDetail(article)
            }
        )
        binding.articlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ArticlesFragment.adapter
        }
    }

    private fun setupUI() {
        binding.fabSavedArticles.setOnClickListener {
            viewModel.toggleSavedArticlesView()
        }
    }

    private fun observeViewModel() {
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)
        }

        viewModel.isShowingSavedArticles.observe(viewLifecycleOwner) { isShowingSaved ->
            val drawableId = if (isShowingSaved) R.drawable.ic_article else R.drawable.ic_saved
            binding.fabSavedArticles.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), drawableId)
            )
        }
    }

    private fun navigateToArticleDetail(article: com.menopausetracker.app.data.model.Article) {
        try {
            // Using Bundle instead of SafeArgs to avoid build errors
            val bundle = Bundle().apply {
                // Pass articleId as a Long if possible, otherwise as String
                try {
                    val idAsLong = article.id.toLongOrNull() ?: 0L
                    putLong("articleId", idAsLong)
                } catch (e: Exception) {
                    // Fallback to String if conversion fails
                    putString("articleId", article.id)
                }
                putString("articleTitle", article.title)
                putString("articleContent", article.content)
                putString("articleImageUrl", article.imageUrl)
            }
            findNavController().navigate(R.id.navigation_article_detail, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
