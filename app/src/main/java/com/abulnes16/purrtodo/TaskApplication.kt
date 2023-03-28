package com.abulnes16.purrtodo

import android.app.Application
import com.abulnes16.purrtodo.database.TaskDatabase

class TaskApplication : Application() {
    val database by lazy { TaskDatabase.getDatabase(this) }
}