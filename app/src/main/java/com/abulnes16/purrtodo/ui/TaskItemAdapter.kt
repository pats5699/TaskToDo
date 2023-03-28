package com.abulnes16.purrtodo.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abulnes16.purrtodo.R
import com.abulnes16.purrtodo.data.Task
import com.abulnes16.purrtodo.databinding.TaskListItemBinding

/**
 * [TaskItemAdapter]
 * Adapts the information of the task in the database and
 * shows them in the lists on the [HomeFragment]
 */
class TaskItemAdapter(private val onItemCliked: (Task) -> Unit) :
    ListAdapter<Task, TaskItemAdapter.TaskItemHolder>(DiffCallback) {

    class TaskItemHolder(private var binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, position: Int) {
            with(task) {
                binding.txtTaskDeadline.text = this.deadline
                binding.txtTaskName.text = this.title
                binding.txtProjectTitle.text = this.project
            }

            changeCardColorByPosition(binding, position)

        }

        private fun changeCardColorByPosition(binding: TaskListItemBinding, position: Int) {

            val context = binding.root.context
            val secondaryColor = ContextCompat.getColor(context, R.color.secondary)
            val primaryColor = ContextCompat.getColor(context, R.color.primary)
            val whiteColor = ContextCompat.getColor(context, R.color.white)

            // If the position is divisible by 3 we left the layout as it is
            if (position % 3 == 0) {
                return
            }
            // But if the position remainder is distinct from 0 we change the background color
            // and the text color
            if (position % 3 == 1) {
                binding.taskCard.setCardBackgroundColor(secondaryColor)

            }

            if (position % 3 == 2) {
                binding.taskCard.setCardBackgroundColor(primaryColor)
            }

            binding.txtTaskName.setTextColor(whiteColor)
            binding.txtTaskDeadline.setTextColor(whiteColor)
            binding.txtProjectTitle.setTextColor(whiteColor)

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskItemAdapter.TaskItemHolder {
        val viewHolder = TaskItemHolder(
            TaskListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemCliked(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: TaskItemHolder, position: Int) {
        holder.bind(getItem(position), position)
    }


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }

        }
    }

}