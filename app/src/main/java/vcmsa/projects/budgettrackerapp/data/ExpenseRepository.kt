package vcmsa.projects.budgettrackerapp.data

class ExpenseRepository(private val dao: ExpenseDao) {
    val allExpenses = dao.getAllExpenses()

    suspend fun insert(expense: Expense) {
        dao.insertExpense(expense)
    }
}
