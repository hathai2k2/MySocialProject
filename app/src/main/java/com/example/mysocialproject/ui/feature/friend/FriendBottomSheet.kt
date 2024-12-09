package com.example.mysocialproject.ui.feature.friend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewFriendBottomSheetBinding
import com.example.mysocialproject.ui.base.BaseBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

class FriendBottomSheet(
    private val onOpenShareLink: () -> Unit,
    private val friendViewModel: FriendViewModel
) : BaseBottomSheetFragment<ViewFriendBottomSheetBinding>() {

    private lateinit var addFriendAdapter: AddFriendAdapter
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    override fun getLayoutId(): Int {
        return R.layout.view_friend_bottom_sheet
    }

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            onOpenShareLink: () -> Unit,
            friendViewModel: FriendViewModel
        ) {
            val fragment = FriendBottomSheet(onOpenShareLink, friendViewModel)
            fragment.show(fragmentManager, FriendBottomSheet::class.java.name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
// Get the BottomSheet view
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        // Add custom callback to BottomSheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Handle state changes
                Log.d("BottomSheet", "State changed: $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Handle sliding
                Log.d("BottomSheet", "Sliding offset: $slideOffset")
            }
        })
        // Set up the share link button
        mViewBinding.btnShareLink.setButtonClickListener {
            onOpenShareLink()
        }

        // Observe dynamic link from ViewModel
        friendViewModel.dynamicLink.observe(viewLifecycleOwner) { link ->
            if (link?.isNotEmpty() == true) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, link)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)

                Log.d("dynamiclink", "Share intent started with link: $link")
            }
        }

        // Observe error messages
        friendViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the RecyclerViews for Friend requests and Friend list
        mViewBinding.rcvAddFriend.layoutManager = LinearLayoutManager(requireContext())
        mViewBinding.rcvFriend.layoutManager = LinearLayoutManager(requireContext())

        // Set up Request Friend Adapter
        addFriendAdapter = AddFriendAdapter(
            requireContext(),
            mutableListOf(),
            onAcceptClick = { friendship -> friendViewModel.onAcceptClick(friendship) },
            onDeclineClick = { friendship -> friendViewModel.onDeclineClick(friendship) }
        )
        mViewBinding.rcvAddFriend.adapter = addFriendAdapter

        // Set up Friends Adapter
        friendAdapter = FriendAdapter(
            requireContext(),
            mutableListOf(),
            onRemoveFriend = { friendId, position -> friendViewModel.onRemove(friendId, position) }
        )
        mViewBinding.rcvFriend.adapter = friendAdapter

        // Get data from ViewModel
        friendViewModel.getFriendship()
        friendViewModel.getFriendAccepted()

        // Observe Friend requests and update the adapter
        friendViewModel.listFriendData.observe(viewLifecycleOwner) { requests ->
            if (requests != null) {
                addFriendAdapter.updateFriendships(requests)
                mViewBinding.tv1.text = "Lời mời kết bạn (${requests.size})"
            } else {
                mViewBinding.tv1.text = "Lời mời kết bạn (0)"
            }
        }

        // Observe Friend list and update the adapter
        friendViewModel.listFriend.observe(viewLifecycleOwner) { friends ->
            if (friends != null) {
                friendAdapter.updateFriends(friends)
                mViewBinding.tv1.text = "Bạn bè (${friends.size}/20)"
            } else {
                mViewBinding.tv1.text = "Bạn bè (0)"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Optionally remove the callback if no longer needed
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Handle state changes
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Handle sliding
        }
    }
}
