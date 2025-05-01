package vcmsa.projects.budgettrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import vcmsa.projects.budgettrackerapp.R
import vcmsa.projects.budgettrackerapp.data.ExpenseDatabase
import vcmsa.projects.budgettrackerapp.data.Goal
import vcmsa.projects.budgettrackerapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val goalDao by lazy {
        ExpenseDatabase.getDatabase(requireContext()).goalDao()
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

        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
        }

        binding.btnViewSummary.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_summaryFragment)
        }

        binding.saveGoalButton.setOnClickListener {
            val min = binding.minGoalEditText.text.toString().toDoubleOrNull()
            val max = binding.maxGoalEditText.text.toString().toDoubleOrNull()

            if (min != null && max != null) {
                val goal = Goal(minGoal = min, maxGoal = max)
                lifecycleScope.launch {
                    goalDao.insertGoal(goal)
                    Toast.makeText(requireContext(), "Goal saved successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe and display latest goal
        goalDao.getLatestGoal().observe(viewLifecycleOwner) { goal ->
            binding.goalPreviewText.text = if (goal != null) {
                "Latest Goal:\nMin: ${goal.minGoal}, Max: ${goal.maxGoal}"
            } else {
                "No goal set yet."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
