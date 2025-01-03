package com.example.mysocialproject.ui.feature.post

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.LikesBottomSheetDialogBinding
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment
import com.example.mysocialproject.ui.feature.adapter.viewItemLikeAdapter
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@RequiresApi(Build.VERSION_CODES.O)
class LikesBottomSheetDialog(private val postId: String, private val viewModel: PostViewModel) : BottomSheetDialogFragment() {
    private lateinit var binding: LikesBottomSheetDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LikesBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.likesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getLikes(postId).observe(viewLifecycleOwner) { likesData ->
            val adapter = viewItemLikeAdapter(likesData)
            binding.likesRecyclerView.adapter = adapter
        }
    }
}