package vcmsa.projects.budgettrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_table")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val minGoal: Double,
    val maxGoal: Double
)
