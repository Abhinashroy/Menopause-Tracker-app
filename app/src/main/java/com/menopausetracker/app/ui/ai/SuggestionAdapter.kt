package com.menopausetracker.app.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Suggestion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SuggestionAdapter(
    private val suggestions: List<Suggestion>,
    private val clickListener: (Suggestion) -> Unit,
    private val deleteListener: ((String) -> Unit)? = null
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view, clickListener, deleteListener)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    override fun getItemCount() = suggestions.size

    class SuggestionViewHolder(
        itemView: View,
        private val clickListener: (Suggestion) -> Unit,
        private val deleteListener: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.text_suggestion_title)
        private val previewTextView: TextView = itemView.findViewById(R.id.text_suggestion_preview)
        private val promptTextView: TextView = itemView.findViewById(R.id.text_user_prompt)
        private val timestampTextView: TextView = itemView.findViewById(R.id.text_suggestion_timestamp)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_suggestion)
        private var currentSuggestion: Suggestion? = null

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

        init {
            itemView.setOnClickListener {
                currentSuggestion?.let { suggestion ->
                    clickListener(suggestion)
                }
            }

            deleteButton.setOnClickListener {
                currentSuggestion?.let { suggestion ->
                    deleteListener?.invoke(suggestion.id)
                }
            }
        }

        fun bind(suggestion: Suggestion) {
            currentSuggestion = suggestion
            titleTextView.text = suggestion.title
            previewTextView.text = suggestion.content

            // Show the user's prompt if available
            if (suggestion.prompt.isNotBlank()) {
                promptTextView.text = "\"${suggestion.prompt}\""
                promptTextView.visibility = View.VISIBLE
            } else {
                promptTextView.visibility = View.GONE
            }

            // Show timestamp
            val date = Date(suggestion.timestamp)
            timestampTextView.text = dateFormat.format(date)

            // Show delete button icon
            deleteButton.isVisible = deleteListener != null
        }
    }
}
