package vcmsa.projects.budgettrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("SELECT * FROM goal_table ORDER BY id DESC LIMIT 1")
    fun getLatestGoal(): LiveData<Goal?>
}
