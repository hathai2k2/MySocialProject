package com.example.mysocialproject.ui.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.EditProfileBottomdialogBinding
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : BottomSheetDialogFragment() {
    companion object {
        const val DIALOG_TYPE_PASSWORD = 0
        const val DIALOG_TYPE_NAME = 1
        const val KEY_DIALOG_TYPE = "dialogType"
    }

    private var dialogType: Int = DIALOG_TYPE_PASSWORD


    private lateinit var binding: EditProfileBottomdialogBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.edit_profile_bottomdialog, container, false)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = authViewModel
        arguments?.getInt(KEY_DIALOG_TYPE)?.let {
            dialogType = it
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {



            if (dialogType == DIALOG_TYPE_PASSWORD) {
                etNewName.visibility = View.GONE


                authViewModel.password.observe(viewLifecycleOwner, { password ->
                    authViewModel.updatePasswordHelperText(binding.newpw.text.toString() ?: "")
                })

                authViewModel.loading.observe(viewLifecycleOwner, { loading ->
                    binding.apply {
                        if (loading) {
                            btnConfirm.text = ""
                            progressBar.visibility = View.VISIBLE
                        } else {
                            btnConfirm.text = "Lưu"
                        }

                    }
                })


                authViewModel.passwordHelperText.observe(viewLifecycleOwner, { pw ->
                    binding.apply {
                        if (pw == null) {
                            btnConfirm.isEnabled = true
                            btnConfirm.backgroundTintList = ActivityCompat.getColorStateList(
                                requireContext(),
                                R.color.color_active
                            )
                        } else {
                            btnConfirm.isEnabled = false
                            btnConfirm.backgroundTintList = ActivityCompat.getColorStateList(
                                requireContext(),
                                R.color.colorbtnctive
                            )
                        }
                    }
                })
                btnConfirm.setOnClickListener {
                    authViewModel.updatepw()
                }

            } else {
                tvTitle.text = "Chọn tên mới"
                etNewName.visibility = View.VISIBLE
                etInput.visibility = View.GONE


                authViewModel.loading.observe(viewLifecycleOwner, { loading ->
                    binding.apply {
                        if (loading) {
                            btnConfirm.text = ""
                            progressBar.visibility = View.VISIBLE
                        } else {
                            progressBar.visibility = View.INVISIBLE
                            btnConfirm.text = "Tiếp tục"
                        }

                    }
                })
                authViewModel.nameUser.observe(viewLifecycleOwner, { name ->
                    authViewModel.updatenamehelper(binding.nameCreate.text.toString() ?: "")
                })

                authViewModel.namehelper.observe(viewLifecycleOwner, { name ->
                    binding.apply {
                        if (name == "") {
                            btnConfirm.isEnabled = true
                            btnConfirm.backgroundTintList = ActivityCompat.getColorStateList(
                                requireContext(),
                                R.color.color_active
                            )
                        } else {
                            btnConfirm.isEnabled = false
                            btnConfirm.backgroundTintList = ActivityCompat.getColorStateList(
                                requireContext(),
                                R.color.colorbtnctive
                            )
                        }
                    }
                })

                btnConfirm.setOnClickListener {
                    authViewModel.updatename()
                }
            }
        }

    }

    fun dismissDialogWithMessage(message: String) {
        dismiss()
        Snackbar.make(requireView(), "$message thành công", Snackbar.LENGTH_SHORT).show()
    }
}