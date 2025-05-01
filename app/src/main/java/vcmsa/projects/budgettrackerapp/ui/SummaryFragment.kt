package vcmsa.projects.budgettrackerapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.budgettrackerapp.databinding.FragmentSummaryBinding
import vcmsa.projects.budgettrackerapp.viewmodel.ExpenseViewModel
import vcmsa.projects.budgettrackerapp.ui.adapters.ExpenseAdapter
import vcmsa.projects.budgettrackerapp.R
import java.text.SimpleDateFormat
import java.util.*

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var selectedCategory: String? = null

    private lateinit var adapter: ExpenseAdapter
    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        adapter = ExpenseAdapter()
        binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExpenses.adapter = adapter

        // Set up category spinner (example categories)
        val categories = listOf("Food", "Transport", "Entertainment", "Bills")
        categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Handle category selection
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle when nothing is selected
            }
        }

        // Observe expenses from ViewModel
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            toggleEmptyState(expenses.isEmpty())
        }

        // Start Date Picker
        binding.btnSelectStartDate.setOnClickListener {
            showDatePicker { year, month, day ->
                startDate = getDateInMillis(year, month, day)
                updateDateRangeText()
            }
        }

        // End Date Picker
        binding.btnSelectEndDate.setOnClickListener {
            showDatePicker { year, month, day ->
                endDate = getDateInMillis(year, month, day)
                updateDateRangeText()
            }
        }

        // Apply Filter
        binding.btnApplyFilter.setOnClickListener {
            applyFilter()
        }

        // Clear Filter
        binding.btnClearFilter.setOnClickListener {
            startDate = null
            endDate = null
            selectedCategory = null
            updateDateRangeText()
            expenseViewModel.allExpenses.value?.let {
                adapter.submitList(it)
                toggleEmptyState(it.isEmpty())
            }
        }

        // Back to Home
        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_summaryFragment_to_homeFragment)
        }

        // Calculate Category Total
        binding.btnCalculateCategoryTotal.setOnClickListener {
            calculateCategoryTotal(selectedCategory)
        }
    }

    private fun applyFilter() {
        expenseViewModel.allExpenses.value?.let { expenses ->
            val filtered = expenses.filter { expense ->
                val dateInMillis = expense.date?.let {
                    getDateInMillisFromString(it)
                } ?: return@filter false

                val isStartDateValid = startDate?.let { it <= dateInMillis } ?: true
                val isEndDateValid = endDate?.let { it >= dateInMillis } ?: true

                val isCategoryValid = selectedCategory?.let { expense.category == it } ?: true

                isStartDateValid && isEndDateValid && isCategoryValid
            }

            adapter.submitList(filtered)
            toggleEmptyState(filtered.isEmpty())
        }
    }

    private fun calculateCategoryTotal(category: String?) {
        expenseViewModel.allExpenses.value?.let { expenses ->
            val filteredByCategory = expenses.filter { it.category == category }
            val categoryTotal = filteredByCategory.sumOf { it.amount ?: 0.0 }
            binding.txtCategoryTotal.text = "Category Total: $${"%.2f".format(categoryTotal)}"
        }
    }

    private fun updateDateRangeText() {
        val start = formatDate(startDate)
        val end = formatDate(endDate)
        binding.txtDateRange.text = "Selected Range: $start - $end"
    }

    private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day -> onDateSet(year, month, day) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun getDateInMillis(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDateInMillisFromString(dateString: String): Long? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDate(dateMillis: Long?): String {
        return dateMillis?.let {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(it))
        } ?: "Not set"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.recyclerViewExpenses.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.txtEmptyMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.btnBackHome.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
