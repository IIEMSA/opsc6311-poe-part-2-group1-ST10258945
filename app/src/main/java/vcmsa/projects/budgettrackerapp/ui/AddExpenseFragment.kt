package vcmsa.projects.budgettrackerapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import vcmsa.projects.budgettrackerapp.databinding.FragmentAddExpenseBinding
import vcmsa.projects.budgettrackerapp.viewmodel.ExpenseViewModel
import vcmsa.projects.budgettrackerapp.data.Expense
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null
    private val calendar = Calendar.getInstance()

    // ✅ Register image picker (using local storage)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            binding.imageViewPhoto.setImageURI(it)  // Referencing imageViewPhoto correctly
            binding.imageViewPhoto.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Category spinner setup
        val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Date picker
        binding.textDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    updateDateLabel()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time picker
        binding.textTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    updateTimeLabel()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // ✅ Launch image picker (opens local storage)
        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Save expense
        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.textDate.text = format.format(calendar.time)
    }

    private fun updateTimeLabel() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.textTime.text = format.format(calendar.time)
    }

    private fun saveExpense() {
        val description = binding.editDescription.text.toString()
        val amountText = binding.editAmount.text.toString()

        if (description.isBlank() || amountText.isBlank()) {
            Toast.makeText(requireContext(), "Description and amount are required", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val category = binding.spinnerCategory.selectedItem?.toString()
        val date = binding.textDate.text.toString()
        val time = binding.textTime.text.toString()
        val photoUriString = selectedPhotoUri?.toString()

        val expense = Expense(
            description = description,
            amount = amount,
            category = category,
            date = date,
            time = time,
            photoUri = photoUriString
        )

        expenseViewModel.insert(expense)
        Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
