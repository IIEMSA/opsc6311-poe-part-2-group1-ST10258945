package vcmsa.projects.budgettrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val amount: Double,
    val category: String? = null,
    val date: String? = null,
    val time: String? = null,
    val photoUri: String? = null
)
