package com.abulnes16.purrtodo.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.abulnes16.purrtodo.database.TaskDao
import com.abulnes16.purrtodo.data.Task
import com.abulnes16.purrtodo.utils.DataTransformationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

/**
 * [TaskViewModel]
 * Manage all the actions of the tasks in the app
 */
class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    private var _error: MutableLiveData<Boolean> = MutableLiveData(false)
    val error: LiveData<Boolean>
        get() = _error

    fun allTodos(): Flow<List<Task>> = taskDao.getTasks()

    fun inProgressTasks(): Flow<List<Task>> = taskDao.getInProgressTasks()

    fun createTask(
        title: String,
        project: String,
        deadline: String,
        description: String
    ) {
        val newTask = Task(
            0,
            title,
            project,
            description,
            deadline,
            isDone = false,
            isInProgress = false
        )
        createTask(newTask)
    }

    fun markInProgress(task: Task) {
        if (!task.isInProgress && !task.isDone) {
            val inProgressTask = task.copy(isInProgress = true)
            updateTask(inProgressTask)
        }
    }

    fun markDone(task: Task) {
        if (!task.isDone) {
            val doneTask = task.copy(isDone = true)
            updateTask(doneTask)
        }
    }

    fun edit(title: String, project: String, deadline: String, description: String, task: Task) {
        val updatedTask = task.copy(
            title = title,
            deadline = deadline,
            description = description,
            project = project
        )
        updateTask(updatedTask)
    }

    fun delete(task: Task) {
        deleteTask(task)
    }

    fun retrieveTask(id: Int): LiveData<Task> {
        return taskDao.getTask(id).asLiveData()
    }

    private fun createTask(
        task: Task
    ) {
        viewModelScope.launch {
            try {
                taskDao.create(task)
                _error.value = false
            } catch (exception: Exception) {
                Log.e("[CREATE_TASK]", exception.toString())
                _error.value = true
            }
        }

    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskDao.update(task)
            } catch (exception: Exception) {
                Log.e("[UPDATE TASK]:", exception.toString())
                _error.value = true
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskDao.delete(task)
            } catch (exception: Exception) {
                Log.e("[DELETE TASK]:", exception.toString())
                _error.value = true
            }
        }
    }

    fun isEntryValid(
        title: String,
        description: String,
        project: String,
        deadline: String
    ): Boolean {
        if (title.isBlank() || description.isBlank() || project.isBlank() || deadline.isBlank()) {
            return false
        }
        return true
    }

    fun retrieveTimeFromDeadline(task: Task): Triple<Int, Int, Int> {
        val datePieces = task.deadline.split(",", " ")
        val day = datePieces[0].toInt()
        val month = DataTransformationUtil.getMonthFromString(datePieces[1])
        val year = datePieces[3].toInt()
        return Triple(day, month, year)
    }

}


/**
 * [TaskViewModelFactory]
 * Creates an instance of the TaskViewModel if is assignable
 */
class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }

        throw IllegalArgumentException("Unknown View Model Class")
    }
}