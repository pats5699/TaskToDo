package com.abulnes16.purrtodo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.abulnes16.purrtodo.R
import com.abulnes16.purrtodo.TaskApplication
import com.abulnes16.purrtodo.data.ProfileDataStore
import com.abulnes16.purrtodo.data.Task
import com.abulnes16.purrtodo.databinding.FragmentHomeBinding
import com.abulnes16.purrtodo.utils.DataTransformationUtil
import com.abulnes16.purrtodo.viewmodels.TaskViewModel
import com.abulnes16.purrtodo.viewmodels.TaskViewModelFactory
import kotlinx.coroutines.launch

/**
 * [HomeFragment]
 * Manages the main screen of the app
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var profilePreferences: ProfileDataStore
    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory((activity?.application as TaskApplication).database.taskDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        bind()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profilePreferences = ProfileDataStore(requireContext())
        setupListeners()
    }

    private fun bind() {
        binding.floatingActionButton.setOnClickListener { goToAddTask() }
        binding.imgUser.setOnClickListener { goToProfile() }
        binding.txtWelcome.text = DataTransformationUtil.getGreetingFromHour()
        bindTodosAdapter()
    }

    private fun setupListeners() {
        profilePreferences.profilePreferences.asLiveData().observe(viewLifecycleOwner) { user ->
            binding.txtName.setText(user.name, TextView.BufferType.SPANNABLE)
            if (user.profilePicture.isBlank()) {
                binding.imgUser.setImageResource(R.drawable.blank_user)
            } else {
                binding.imgUser.setImageURI(user.profilePicture.toUri())
            }
        }
    }

    private fun goToAddTask() {
        val action = R.id.action_homeFragment_to_addTaskFragment
        findNavController().navigate(action)
    }

    private fun goToProfile() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToUserProfileFragment())
    }

    private fun bindTodosAdapter() {
        val onClick = { it: Task ->
            val action =
                HomeFragmentDirections.actionHomeFragmentToTaskDetailFragment(taskId = it.id)
            findNavController().navigate(action)
        }
        val taskAdapter = TaskItemAdapter(onClick)
        val inProgressAdapter = TaskItemAdapter(onClick)
        with(binding) {

            this.recyclerTodos.adapter = taskAdapter
            this.recyclerTodos.layoutManager =
                StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL)
            this.recyclerInProgress.adapter = inProgressAdapter
            this.recyclerInProgress.layoutManager =
                StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        }
        lifecycle.coroutineScope.launch {
            viewModel.allTodos().collect() {
                taskAdapter.submitList(it)
                binding.txtNumTodos.text = it.size.toString()
            }
        }

        lifecycle.coroutineScope.launch {
            viewModel.inProgressTasks().collect() {
                inProgressAdapter.submitList(it)
                binding.txtInProgressNum.text = it.size.toString()
            }
        }

    }


}