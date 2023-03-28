package com.abulnes16.purrtodo.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.abulnes16.purrtodo.R
import com.abulnes16.purrtodo.TaskApplication
import com.abulnes16.purrtodo.data.Task
import com.abulnes16.purrtodo.databinding.FragmentAddTaskBinding
import com.abulnes16.purrtodo.utils.DataTransformationUtil
import com.abulnes16.purrtodo.viewmodels.TaskViewModel
import com.abulnes16.purrtodo.viewmodels.TaskViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


/**
 * [AddTaskFragment]
 * Manage the form to create a new task in the application
 */
class AddTaskFragment : Fragment() {

    private lateinit var binding: FragmentAddTaskBinding
    private lateinit var task: Task
    private val arguments: AddTaskFragmentArgs by navArgs()
    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory((activity?.application as TaskApplication).database.taskDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)

        val taskId = arguments.taskId
        if (taskId != 0) {
            viewModel.retrieveTask(taskId).observe(this.viewLifecycleOwner) {
                task = it
                bind(it)
            }
        } else {
            bind()
        }
        setupListeners()
        return binding.root
    }


    private fun setupListeners() {
        viewModel.error.observe(viewLifecycleOwner) { hasError ->
            if (hasError) {
                val message = if (arguments.taskId != 0) "update" else "create"
                showToast(getString(R.string.failed_task, message))
            }
        }
    }


    private fun bind() {
        binding.apply {
            this.btnGoBackAddTask.setOnClickListener { goBack() }
            this.btnSave.setOnClickListener { saveTask() }
            this.txtDeadline.setOnClickListener { showDateDialog() }
            this.btnDelete.visibility = View.GONE
        }
    }

    private fun bind(task: Task) {
        binding.apply {
            this.btnGoBackAddTask.setOnClickListener { goBack() }
            this.btnSave.setOnClickListener { editTask() }
            this.btnDelete.setOnClickListener { showConfirmationDialog() }
            this.txtDeadline.setOnClickListener { showDateDialog() }
            this.txtTaskAdd.text = getString(R.string.edit_task)
            this.btnDelete.visibility = View.VISIBLE
            this.txtTaskTitle.setText(task.title, TextView.BufferType.SPANNABLE)
            this.txtProject.setText(task.project, TextView.BufferType.SPANNABLE)
            this.txtDeadline.setText(task.deadline, TextView.BufferType.SPANNABLE)
            this.txtDescription.setText(task.description, TextView.BufferType.SPANNABLE)
        }
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    private fun saveTask() {
        DataTransformationUtil.hideKeyboard(activity)
        with(binding) {
            val title = this.txtTaskTitle.text.toString()
            val description = this.txtDescription.text.toString()
            val project = this.txtProject.text.toString()
            val deadline = this.txtDeadline.text.toString()
            if (viewModel.isEntryValid(title, description, project, deadline)) {
                viewModel.createTask(title, project, description, deadline)
                clearErrors()
                showToast(getString(R.string.successful_task))
                goBack()
            } else {
                manageFormError()
            }
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.RoundShapeTheme)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteTask()
            }.setNegativeButton(getString(R.string.no)) { _, _ ->
            }
            .show()
    }

    private fun deleteTask() {
        viewModel.delete(task)
        findNavController().navigate(R.id.action_addTaskFragment_to_homeFragment)
    }

    private fun editTask() {
        DataTransformationUtil.hideKeyboard(activity)
        with(binding) {
            val title = this.txtTaskTitle.text.toString()
            val description = this.txtDescription.text.toString()
            val project = this.txtProject.text.toString()
            val deadline = this.txtDeadline.text.toString()
            if (viewModel.isEntryValid(title, description, project, deadline)) {
                viewModel.edit(title, project, deadline, description, task)
                clearErrors()
                showToast(getString(R.string.updated_successful_task))
                goBack()
            } else {
                manageFormError()
            }
        }
    }

    private fun manageFormError() {
        binding.apply {
            if (this.txtTaskTitle.length() == 0) {
                this.txtTaskTitle.error = formatFieldValidation("task title")
            }

            if (this.txtProject.length() == 0) {
                this.txtProject.error = formatFieldValidation("project")
            }

            if (this.txtDeadline.length() == 0) {
                this.txtDeadline.error = formatFieldValidation("deadline")
            }

            if (this.txtDescription.length() == 0) {
                this.txtDescription.error = formatFieldValidation("description")
            }
        }
    }

    private fun formatFieldValidation(fieldName: String): String {
        return getString(R.string.field_empty, fieldName)
    }

    private fun clearErrors() {
        binding.apply {
            this.txtTaskTitle.error = null
            this.txtProject.error = null
            this.txtDescription.error = null
            this.txtDeadline.error = null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            context?.applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDateDialog() {
        DataTransformationUtil.hideKeyboard(activity)
        val calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        // If we are going to edit the task we want that the day of the date picker
        // is the date of the deadline of the task
        if (arguments.taskId != 0) {
            val (taskDay, taskMonth, taskYear) = viewModel.retrieveTimeFromDeadline(task)
            year = taskYear
            month = taskMonth
            day = taskDay
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, yearPicked, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.MONTH, monthOfYear)
                val date = "$dayOfMonth ${
                    calendar.getDisplayName(
                        Calendar.MONTH,
                        Calendar.SHORT,
                        Locale.getDefault()
                    )
                }, $yearPicked"
                binding.txtDeadline.setText(date)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()

    }

}