package com.abulnes16.purrtodo.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abulnes16.purrtodo.BuildConfig
import com.abulnes16.purrtodo.R
import com.abulnes16.purrtodo.data.ProfileDataStore
import com.abulnes16.purrtodo.databinding.FragmentUserProfileBinding
import com.abulnes16.purrtodo.utils.DataTransformationUtil
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileFragment : Fragment() {

    private lateinit var profileDataStore: ProfileDataStore
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var imageResult: ActivityResultLauncher<Array<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageResult = registerForActivityResult(ActivityResultContracts.OpenDocument()) { image ->
            if (image != null) {
                binding.profilePicture.setImageDrawable(null)
                requireActivity().contentResolver.takePersistableUriPermission(
                    image,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = image
                binding.profilePicture.setImageURI(image)
            }
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    chooseImage()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.photos_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        bind()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileDataStore = ProfileDataStore(requireContext())
        setupListeners()
    }

    private fun bind() {
        binding.apply {
            this.btnEditPicture.setOnClickListener { checkForContentPermission() }
            this.btnGoBackProfile.setOnClickListener { goBack() }
            this.btnSaveProfile.setOnClickListener { saveUserPreferences() }
            this.txtVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        }
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    private fun setupListeners() {
        profileDataStore.profilePreferences.asLiveData().observe(viewLifecycleOwner) { user ->
            binding.txtUserName.setText(user.name, TextView.BufferType.SPANNABLE)
            if (user.profilePicture.isBlank()) {
                binding.profilePicture.setImageResource(R.drawable.blank_user)
            } else {
                binding.profilePicture.setImageURI(user.profilePicture.toUri())
                imageUri = user?.profilePicture?.toUri()
            }
        }

    }

    private fun chooseImage() {
        try {
            imageResult.launch(arrayOf("image/*"))
        } catch (exception: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_open_gallery),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkForContentPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                chooseImage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // TODO: Add layout when the user doesn't grant the permission

            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveUserPreferences() {
        DataTransformationUtil.hideKeyboard(activity)
        with(binding) {
            val userProfile = imageUri?.toString() ?: ""
            val userName = this.txtUserName.text.toString()
            lifecycleScope.launch {
                profileDataStore.saveProfilePreferences(userName, userProfile, requireContext())
            }
            Toast.makeText(
                requireContext(),
                getString(R.string.preferences_save),
                Toast.LENGTH_LONG
            ).show()
        }
    }

}