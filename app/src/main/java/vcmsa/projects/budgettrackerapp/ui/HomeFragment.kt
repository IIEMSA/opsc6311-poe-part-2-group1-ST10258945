package vcmsa.projects.budgettrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.budgettrackerapp.R
import vcmsa.projects.budgettrackerapp.data.ExpenseDatabase
import vcmsa.projects.budgettrackerapp.data.Goal
import vcmsa.projects.budgettrackerapp.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val goalDao by lazy {
        ExpenseDatabase.getDatabase(requireContext()).goalDao()
    }

    private val expenseDao by lazy {
        ExpenseDatabase.getDatabase(requireContext()).expenseDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button: Add Expense
        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
        }

        // Button: View Summary
        binding.btnViewSummary.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_summaryFragment)
        }

        // Button: View Budget Graph (new)
        binding.btnViewBudgetGraph.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_budgetGraphFragment)
        }

        // Button: Search Expenses
        binding.btnSearchExpenses.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        // Button: Save Goal
        binding.saveGoalButton.setOnClickListener {
            val min = binding.minGoalEditText.text.toString().toDoubleOrNull()
            val max = binding.maxGoalEditText.text.toString().toDoubleOrNull()

            if (min != null && max != null && min <= max) {
                val goal = Goal(minGoal = min, maxGoal = max)
                lifecycleScope.launch(Dispatchers.IO) {
                    goalDao.insertGoal(goal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Goal saved successfully", Toast.LENGTH_SHORT).show()
                        evaluateMonthlySpending()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter valid goal values (min ≤ max)", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe and update goal preview and status
        goalDao.getLatestGoal().observe(viewLifecycleOwner) { goal ->
            binding.goalPreviewText.text = if (goal != null) {
                "Latest Goal:\nMin: ${goal.minGoal}, Max: ${goal.maxGoal}"
            } else {
                "No goal set yet."
            }

            lifecycleScope.launch {
                evaluateMonthlySpending()
            }
        }

        // Show daily spending summary
        lifecycleScope.launch {
            showDailySpendingSummary()
        }
    }

    private suspend fun evaluateMonthlySpending() {
        withContext(Dispatchers.IO) {
            val goal = goalDao.getLatestGoalOnce()
            val expenses = expenseDao.getAllExpensesOnce()

            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val expensesThisMonth = expenses.filter { expense ->
                expense.date?.let { dateStr ->
                    val date = sdf.parse(dateStr)
                    date?.let {
                        val cal = Calendar.getInstance()
                        cal.time = it
                        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                    } ?: false
                } ?: false
            }

            val totalThisMonth = expensesThisMonth.sumOf { it.amount }

            val result = when {
                goal == null -> "No goal set"
                totalThisMonth < goal.minGoal -> "Below Goal"
                totalThisMonth > goal.maxGoal -> "Over Goal"
                else -> "Within Goal"
            }

            withContext(Dispatchers.Main) {
                binding.statusTextView.text = "This month: $result\nSpent: $totalThisMonth"
            }
        }
    }

    private suspend fun showDailySpendingSummary() {
        withContext(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Calendar.getInstance().time)

            val expensesToday = expenseDao.getAllExpensesOnce().filter {
                it.date == todayStr
            }

            val totalToday = expensesToday.sumOf { it.amount }

            withContext(Dispatchers.Main) {
                binding.dailySpendingTextView.text = "Spent today: $totalToday"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
