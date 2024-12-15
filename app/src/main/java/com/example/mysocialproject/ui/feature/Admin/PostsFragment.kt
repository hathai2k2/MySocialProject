package com.example.mysocialproject.ui.feature.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraprovider.Admin.PostAdapter
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentPostsBinding

class PostsFragment : Fragment() {

    private lateinit var binding: FragmentPostsBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var adminViewModel: AdminViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_posts, container, false)
        adminViewModel = ViewModelProvider(requireActivity()).get(AdminViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        postAdapter = PostAdapter(
            emptyList(),
            onDeleteClick = { post -> adminViewModel.deletePost(post.postId, requireActivity()) })

        binding.ryc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        adminViewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.updatePosts(posts)
            binding.title.text = "Số lượng bài đăng ngày hôm nay(${posts.size})"
        }
    }
}