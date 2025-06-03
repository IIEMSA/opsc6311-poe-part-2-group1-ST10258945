package vcmsa.projects.budgettrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    suspend fun getAllExpensesOnce(): List<Expense>

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :keyword || '%'")
    suspend fun searchExpensesByKeyword(keyword: String): List<Expense>
}
