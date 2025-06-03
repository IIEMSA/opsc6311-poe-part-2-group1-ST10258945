package vcmsa.projects.budgettrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import vcmsa.projects.budgettrackerapp.data.ExpenseDatabase
import vcmsa.projects.budgettrackerapp.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val expenseDao by lazy {
        ExpenseDatabase.getDatabase(requireContext()).expenseDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.isNotEmpty()) {
                lifecycleScope.launch {
                    val results = withContext(Dispatchers.IO) {
                        expenseDao.getAllExpensesOnce().filter {
                            it.description?.contains(keyword, ignoreCase = true) == true
                        }
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        results.map { "${it.description}: R${it.amount}" }
                    )
                    binding.listResults.adapter = adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
