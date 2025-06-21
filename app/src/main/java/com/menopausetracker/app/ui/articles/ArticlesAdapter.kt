package com.menopausetracker.app.ui.articles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Article
import com.menopausetracker.app.databinding.ItemArticleBinding

class ArticlesAdapter(
    private val onSaveClick: (Article) -> Unit,
    private val onArticleClick: (Article) -> Unit
) : ListAdapter<Article, ArticlesAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                articleTitle.text = article.title
                articleDescription.text = article.content
                saveButton.setImageResource(
                    if (article.isSaved) R.drawable.ic_saved
                    else R.drawable.ic_save
                )
                saveButton.setOnClickListener {
                    onSaveClick(article)
                }

                // Make the whole item clickable to view article detail
                root.setOnClickListener {
                    onArticleClick(article)
                }

                // Load image using Glide
                Glide.with(articleImage)
                    .load(article.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(articleImage)
            }
        }
    }

    private class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}

