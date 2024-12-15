package com.example.mysocialproject.ui.feature.post

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.LikesBottomSheetDialogBinding
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment
import com.example.mysocialproject.ui.feature.adapter.viewItemLikeAdapter
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel

@RequiresApi(Build.VERSION_CODES.O)
class LikesBottomSheetDialog(private val postId: String, private val viewModel: PostViewModel) :
    BaseBottomSheetFragment<LikesBottomSheetDialogBinding>() {
    private lateinit var binding: LikesBottomSheetDialogBinding
    override fun getLayoutId(): Int {
        return R.layout.likes_bottom_sheet_dialog
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