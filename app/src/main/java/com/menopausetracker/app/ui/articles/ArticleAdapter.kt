package com.menopausetracker.app.ui.articles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Article
import java.text.SimpleDateFormat
import java.util.Locale

class ArticleAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onSaveClick: (Article) -> Unit
) : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        holder.bind(article, onArticleClick, onSaveClick)
    }

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
        private val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)
        private val articleDescription: TextView = itemView.findViewById(R.id.articleDescription)
        private val saveButton: ImageButton = itemView.findViewById(R.id.saveButton)

        fun bind(
            article: Article,
            onArticleClick: (Article) -> Unit,
            onSaveClick: (Article) -> Unit
        ) {
            // Set article title and description
            articleTitle.text = article.title
            articleDescription.text = article.summary

            // Improved image loading with Glide
            try {
                if (!article.imageUrl.isNullOrEmpty()) {
                    // Ensure the URL starts with http or https
                    val imageUrl = if (!article.imageUrl.startsWith("http")) {
                        "https://${article.imageUrl}"
                    } else {
                        article.imageUrl
                    }

                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(articleImage)
                } else {
                    // Set placeholder image if no URL
                    articleImage.setImageResource(R.drawable.placeholder_image)
                }
            } catch (e: Exception) {
                // If any error occurs, show the placeholder
                articleImage.setImageResource(R.drawable.placeholder_image)
                println("Image loading error: ${e.message}")
            }

            // Set save button icon based on saved status
            saveButton.setImageResource(
                if (article.isSaved) R.drawable.ic_saved else R.drawable.ic_save
            )

            // Set click listeners
            itemView.setOnClickListener { onArticleClick(article) }
            saveButton.setOnClickListener { onSaveClick(article) }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
