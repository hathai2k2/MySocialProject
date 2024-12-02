package com.example.mysocialproject.ui.feature.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.BR
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentMessageBinding
import com.example.mysocialproject.ui.base.BaseFragmentWithViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageFragment : BaseFragmentWithViewModel<FragmentMessageBinding,MessageViewModel>() {
    override fun getViewModelClass() = MessageViewModel::class.java

    override fun getBindingVariable() = BR.viewModel

    override fun initViewModel() = viewModels<MessageViewModel>()

    override fun getLayoutId() = R.layout.fragment_message

    private lateinit var adapter: MessageAdapter
    private var currentUserId: String = "user_2"
    val args: MessageFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
// Initialize RecyclerView
        mViewBinding.rcvMessage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(emptyList(), currentUserId,WallPagerChat.NOTHING)
            adapter = adapter
        }
        mViewBinding.appHeader2.onClickLeftIcon {
            findNavController().popBackStack()
        }

        // Load messages from ViewModel
        val receiverId = "user_1" // ID của người nhận
        mViewModel.loadMessages(currentUserId, receiverId)

        // Observe messages and update UI
        mViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter = MessageAdapter(messages, currentUserId, args.typeWallPager) // Cập nhật adapter
            mViewBinding.rcvMessage.adapter = adapter
            mViewBinding.rcvMessage.scrollToPosition(adapter.itemCount - 1) // Tự động cuộn xuống
        }

        // Send a new message (giả lập gửi tin nhắn)
        mViewBinding.appEditText.setRightDrawableClick  {
            val messageContent = mViewBinding.appEditText.getText()
            if (messageContent.isNotEmpty()) {
                mViewModel.sendMessage(currentUserId, receiverId, messageContent)
                mViewBinding.appEditText.clearText()
            }
            hideKeyboard()
        }
        onChangeWallPager(args.typeWallPager)
    }

    private fun onChangeWallPager(typeWallPager: WallPagerChat) {
        // Lấy ảnh gốc
        var idImage =  when(typeWallPager){
            WallPagerChat.NOTHING -> R.color.white
            WallPagerChat.PW ->R.drawable.power
            WallPagerChat.EK -> R.drawable.ekko_wallpager
            WallPagerChat.EKPW -> R.drawable.ekko_power
            WallPagerChat.VK -> R.drawable.viktor
        }
        if (typeWallPager != WallPagerChat.NOTHING){
            val originalBitmap = BitmapFactory.decodeResource(resources,idImage )

            // Tạo matrix để xoay ảnh
            val matrix = Matrix()
            matrix.postRotate(-90f) // Xoay 90 độ

            // Tạo bitmap mới với chiều đã xoay
            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0, 0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )

            // Gán bitmap đã xoay vào ImageView
            mViewBinding.ivWallPager.setImageBitmap(rotatedBitmap)
        }
        else{
            mViewBinding.ivWallPager.setImageResource(idImage)
        }
    }

}

enum class WallPagerChat(index:Int){
    NOTHING(0),
    PW(1),
    EK(2),
    EKPW(3),
    VK(4)
}