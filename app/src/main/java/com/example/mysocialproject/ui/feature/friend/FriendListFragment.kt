package com.example.mysocialproject.ui.feature.friend

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentFriendListBinding
import com.example.mysocialproject.ui.feature.adapter.FriendsAdapter
import com.example.mysocialproject.ui.feature.adapter.RequestFriendAdapter
import com.example.mysocialproject.ui.feature.viewmodel.FriendViewmodel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendListFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFriendListBinding
    private lateinit var frViewModel: FriendViewmodel
    private lateinit var frRequestAdapter: RequestFriendAdapter
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_list, container, false)
        frViewModel = ViewModelProvider(requireActivity()).get(FriendViewmodel::class.java)
        binding.lifecycleOwner = this
        binding.vmodel = frViewModel


        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.btnSenlink.setOnClickListener {
            frViewModel.createDynamicLink()

        }
        frViewModel.dynamicLink.observe(viewLifecycleOwner, Observer { link ->
            if (link != "") {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "$link")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)

                Log.d("dynamiclink", "Share intent started with link: $link")
            }
        })


        frViewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), "Vui lòng thử lại", Toast.LENGTH_SHORT)
            }
        })

        binding.listrequestFriend.layoutManager = LinearLayoutManager(requireContext())
        binding.listfriend.layoutManager = LinearLayoutManager(requireContext())


        frRequestAdapter = RequestFriendAdapter(requireContext(),
            mutableListOf(),
            onAcceptClick = { friendship -> frViewModel.onAcceptClick(friendship) },
            onDeclineClick = { friendship -> frViewModel.onDeclineClick(friendship) }
        )
        binding.listrequestFriend.adapter = frRequestAdapter
        //friends recycler
        friendsAdapter = FriendsAdapter(requireContext(),
            mutableListOf(),
            onRemoveFriend = { friendId, postion -> frViewModel.onRemove(friendId, postion) })

        binding.listfriend.adapter = friendsAdapter


        frViewModel.getFriendship()
        frViewModel.getFriendAccepted()
        //friendshipthay doi trangf thairecycler

        frViewModel.listFriendrequest.observe(viewLifecycleOwner) {
            if (it != null) {
                frRequestAdapter.updateFriendships(it)
                binding.totalRequest.text = "Lời mời kết bạn (${it.size})"
            } else {
                binding.totalRequest.text = "Lời mời kết bạn (0)"
            }
        }

        //friendlist recycler

        frViewModel.listFriend.observe(viewLifecycleOwner) { friends ->
            if (friends != null) {
                friendsAdapter.updateFriends(friends)
                binding.totalFriends.text = "Bạn bè (${friends.size}/15)"
            } else {
                binding.totalFriends.text = "Bạn bè (0)"
            }
        }


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        val layoutParams = bottomSheet?.layoutParams

        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet?.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


}