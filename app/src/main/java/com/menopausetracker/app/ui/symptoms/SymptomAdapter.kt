package com.menopausetracker.app.ui.symptoms

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Symptom
import com.menopausetracker.app.databinding.ItemSymptomBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SymptomAdapter(
    private val listener: SymptomInteractionListener
) : ListAdapter<Symptom, SymptomAdapter.SymptomViewHolder>(SymptomDiffCallback()) {

    interface SymptomInteractionListener {
        fun onEditSymptom(symptom: Symptom)
        fun onDeleteSymptom(symptom: Symptom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val binding = ItemSymptomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SymptomViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class SymptomViewHolder(
        private val binding: ItemSymptomBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(symptom: Symptom, listener: SymptomInteractionListener) {
            // Format the date
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            binding.textDate.text = dateFormat.format(symptom.date)

            // Set severity
            binding.textSeverity.text = context.getString(R.string.severity_label, symptom.severity)

            // Set symptom description
            binding.textDescription.text = symptom.description

            // Clear existing chips and add new ones based on symptom data
            binding.symptomChips.removeAllViews()

            if (symptom.hotFlashes) {
                addChip(context.getString(R.string.hot_flashes))
            }
            if (symptom.nightSweats) {
                addChip(context.getString(R.string.night_sweats))
            }
            if (symptom.moodChanges) {
                addChip(context.getString(R.string.mood_changes))
            }
            if (symptom.sleepIssues) {
                addChip(context.getString(R.string.sleep_issues))
            }
            if (symptom.fatigue) {
                addChip(context.getString(R.string.fatigue))
            }

            // Set click listeners
            binding.buttonEdit.setOnClickListener {
                listener.onEditSymptom(symptom)
            }
            binding.buttonDelete.setOnClickListener {
                listener.onDeleteSymptom(symptom)
            }
        }

        private fun addChip(text: String) {
            val chip = Chip(context).apply {
                this.text = text
                isClickable = false
                isCheckable = false
            }
            binding.symptomChips.addView(chip)
        }
    }

    private class SymptomDiffCallback : DiffUtil.ItemCallback<Symptom>() {
        override fun areItemsTheSame(oldItem: Symptom, newItem: Symptom): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Symptom, newItem: Symptom): Boolean {
            return oldItem == newItem
        }
    }
}
