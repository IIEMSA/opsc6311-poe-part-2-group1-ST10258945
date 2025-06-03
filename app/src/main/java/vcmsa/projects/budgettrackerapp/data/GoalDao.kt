package vcmsa.projects.budgettrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vcmsa.projects.budgettrackerapp.data.Goal

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("SELECT * FROM goal_table ORDER BY id DESC LIMIT 1")
    fun getLatestGoal(): LiveData<Goal?>

    @Query("SELECT * FROM goal_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestGoalOnce(): Goal?
}
