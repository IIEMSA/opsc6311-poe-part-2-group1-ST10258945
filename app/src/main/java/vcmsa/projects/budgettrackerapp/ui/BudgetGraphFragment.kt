package vcmsa.projects.budgettrackerapp.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.budgettrackerapp.R
import vcmsa.projects.budgettrackerapp.data.ExpenseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetGraphFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnApplyFilter: Button
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var tvSpentVsGoal: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvBudgetGoalBadge: TextView
    private lateinit var tvLoggingStreakBadge: TextView

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var startDateCal = Calendar.getInstance()
    private var endDateCal = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_graph, container, false)

        barChart = view.findViewById(R.id.barChart)
        tvStartDate = view.findViewById(R.id.tvStartDate)
        tvEndDate = view.findViewById(R.id.tvEndDate)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar)
        tvSpentVsGoal = view.findViewById(R.id.tvSpentVsGoal)
        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus)
        tvBudgetGoalBadge = view.findViewById(R.id.tvBudgetGoalBadge)
        tvLoggingStreakBadge = view.findViewById(R.id.tvLoggingStreakBadge)

        endDateCal = Calendar.getInstance()
        startDateCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -30) }

        tvStartDate.text = dateFormat.format(startDateCal.time)
        tvEndDate.text = dateFormat.format(endDateCal.time)

        tvStartDate.setOnClickListener {
            showDatePicker(startDateCal) { date ->
                startDateCal = date
                tvStartDate.text = dateFormat.format(date.time)
            }
        }

        tvEndDate.setOnClickListener {
            showDatePicker(endDateCal) { date ->
                endDateCal = date
                tvEndDate.text = dateFormat.format(date.time)
            }
        }

        btnApplyFilter.setOnClickListener {
            loadAndDisplayChart()
        }

        loadAndDisplayChart()
        return view
    }

    private fun showDatePicker(calendar: Calendar, onDateSet: (Calendar) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSet(newCal)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadAndDisplayChart() {
        val expenseDao = ExpenseDatabase.getDatabase(requireContext()).expenseDao()
        val goalDao = ExpenseDatabase.getDatabase(requireContext()).goalDao()

        lifecycleScope.launch {
            val expenses = withContext(Dispatchers.IO) {
                expenseDao.getAllExpensesOnce()
            }

            val goal = withContext(Dispatchers.IO) {
                goalDao.getLatestGoalOnce()
            }

            val filteredExpenses = expenses.filter { expense ->
                val expenseDate = expense.date?.let {
                    try {
                        dateFormat.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                expenseDate?.let {
                    !it.before(startDateCal.time) && !it.after(endDateCal.time)
                } ?: false
            }

            val categoryTotals = filteredExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

            val entries = categoryTotals.entries.mapIndexed { index, (_, total) ->
                BarEntry(index.toFloat(), total)
            }

            val dataSet = BarDataSet(entries, "Spending by Category").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
            }

            val barData = BarData(dataSet)
            barChart.data = barData
            barChart.description.text = "Spending from ${dateFormat.format(startDateCal.time)} to ${dateFormat.format(endDateCal.time)}"

            val leftAxis = barChart.axisLeft
            leftAxis.removeAllLimitLines()

            goal?.let {
                val minLine = LimitLine(it.minGoal.toFloat(), "Min Goal").apply {
                    lineColor = Color.GREEN
                    lineWidth = 2f
                    textColor = Color.GREEN
                }
                val maxLine = LimitLine(it.maxGoal.toFloat(), "Max Goal").apply {
                    lineColor = Color.RED
                    lineWidth = 2f
                    textColor = Color.RED
                }
                leftAxis.addLimitLine(minLine)
                leftAxis.addLimitLine(maxLine)
            }

            val totalSpent = filteredExpenses.sumOf { it.amount }.toFloat()

            goal?.let { g ->
                val min = g.minGoal.toFloat()
                val max = g.maxGoal.toFloat()

                val goalRange = max - min
                val progressPercent = when {
                    totalSpent <= min -> 0
                    totalSpent >= max -> 100
                    else -> ((totalSpent - min) / goalRange * 100).toInt()
                }

                budgetProgressBar.progress = progressPercent
                tvSpentVsGoal.text = "Spent: R%.2f / Goal Range: R%.2f - R%.2f".format(totalSpent, min, max)

                tvBudgetStatus.text = when {
                    totalSpent < min -> "Under Budget"
                    totalSpent in min..max -> "Within Budget 🎯"
                    else -> "Over Budget ⚠️"
                }

                tvBudgetStatus.setTextColor(
                    when {
                        totalSpent < min -> Color.BLUE
                        totalSpent in min..max -> Color.parseColor("#388E3C")
                        else -> Color.RED
                    }
                )

                // Show badge if within budget
                if (totalSpent in min..max) {
                    tvBudgetGoalBadge.text = "🏅 Great job! You stayed within your budget!"
                } else {
                    tvBudgetGoalBadge.text = ""
                }
            } ?: run {
                tvSpentVsGoal.text = "No goal data available."
                tvBudgetStatus.text = "Set a budget goal to see performance."
                budgetProgressBar.progress = 0
                tvBudgetGoalBadge.text = ""
            }

            // Streak badge logic
            val uniqueDates = filteredExpenses.mapNotNull {
                it.date?.let { dateStr -> dateFormat.parse(dateStr)?.time }
            }.toSet()

            val sortedDates = uniqueDates.sorted()
            var longestStreak = 1
            var currentStreak = 1

            for (i in 1 until sortedDates.size) {
                val prev = sortedDates[i - 1]
                val curr = sortedDates[i]
                val diff = (curr - prev) / (1000 * 60 * 60 * 24)
                if (diff == 1L) {
                    currentStreak++
                    longestStreak = maxOf(longestStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
            }

            if (longestStreak >= 3) {
                tvLoggingStreakBadge.text = "🔥 Logging Streak! $longestStreak days in a row!"
            } else {
                tvLoggingStreakBadge.text = ""
            }

            barChart.invalidate()
        }
    }
}
