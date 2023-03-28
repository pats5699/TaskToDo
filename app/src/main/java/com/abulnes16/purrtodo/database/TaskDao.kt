package com.abulnes16.purrtodo.database

import androidx.room.*
import com.abulnes16.purrtodo.data.Task
import kotlinx.coroutines.flow.Flow

/**
 * [TaskDao]
 * Data Access Object for the tasks in the database
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE is_done != 1 AND is_in_progress != 1")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id=:id")
    fun getTask(id: Int): Flow<Task>

    @Query("SELECT * FROM tasks WHERE is_in_progress = 1 AND is_done !=1")
    fun getInProgressTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun create(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}