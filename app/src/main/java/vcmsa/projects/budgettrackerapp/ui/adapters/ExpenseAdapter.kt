package vcmsa.projects.budgettrackerapp.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vcmsa.projects.budgettrackerapp.data.Expense
import vcmsa.projects.budgettrackerapp.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).format(expense.amount)

            binding.txtDescription.text = expense.description
            binding.txtAmount.text = formattedAmount
            binding.txtCategory.text = "Category: ${expense.category ?: "None"}"
            binding.txtDate.text = "Date: ${expense.date ?: "Not set"}"

            if (!expense.photoUri.isNullOrEmpty()) {
                // Use Glide to load image
                Glide.with(binding.imgExpensePhoto.context)
                    .load(Uri.parse(expense.photoUri))
                    .into(binding.imgExpensePhoto)

                binding.imgExpensePhoto.visibility = View.VISIBLE

                // Open image in default viewer on click
                binding.imgExpensePhoto.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(expense.photoUri), "image/*")
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    binding.imgExpensePhoto.context.startActivity(intent)
                }
            } else {
                binding.imgExpensePhoto.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
    }
}
