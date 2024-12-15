package com.example.mysocialproject.ui.feature.bottomdialogai

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.databinding.LayoutPromtVoiceBinding
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PromptDialogVoice(private val prompt: String) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutPromtVoiceBinding
    private lateinit var postViewModel: PostViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPromtVoiceBinding.inflate(inflater, container, false)
        postViewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        binding.viewModel = postViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.genaAgain.apply {
            setOnClickListener {

                Log.d("Prompt", "onViewCreated: $prompt")

                binding.genaAgain.isEnabled = false
                binding.tvGeneratedContent.setText("Quá trình xử lý có thể mất thời gian, vui lòng đợi!")
                postViewModel.generateContentVoice(prompt)


                Handler(Looper.getMainLooper()).postDelayed({
                    binding.genaAgain.isEnabled = true
                }, 3500)
                text = "Tạo lại"

            }


            binding.btnOk.setOnClickListener {
                dismiss()
            }


        }

    }
}