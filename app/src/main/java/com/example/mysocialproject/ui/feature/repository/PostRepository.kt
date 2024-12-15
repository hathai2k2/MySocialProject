package com.example.mysocialproject.ui.feature.repository

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mysocialproject.BuildConfig
import com.example.mysocialproject.ui.feature.model.Like
import com.example.mysocialproject.ui.feature.model.LikeStatus
import com.example.mysocialproject.ui.feature.model.Post
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class PostRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var postPagingSource: PostPagingSource? = null


    //add pót
    suspend fun addPost(contentUri: Uri, content: String, isImage: Boolean): PostResult {
        return try {
            val uniqueID = UUID.randomUUID().toString()
            val fileName = "post_${auth.currentUser!!.uid}$uniqueID"

            val storageReference = if (isImage) {
                storage.reference.child("${auth.currentUser!!.uid}/post_image/$fileName.jpeg")
            } else {
                storage.reference.child("${auth.currentUser!!.uid}/post_voice/$fileName.aac")
            }
            val uploadTask = storageReference.putFile(contentUri).await()
            val downloadUrl = storageReference.downloadUrl.await()
            Log.d("TAGY", "Download URL obtained: $downloadUrl")
            val currentUID = auth.currentUser?.uid
            val timeStamp = Timestamp(Date())
            if (currentUID != null) {
                val docRef = fireStore.collection("users").document(currentUID).get().await()
                val currenName = docRef.getString("nameUser")
                val currentAvt = docRef.getString("avatarUser")
                val newPostRef = fireStore.collection("posts").document()
                val postId = newPostRef.id
                val post = Post(
                    postId = postId,
                    userId = currentUID,
                    userName = currenName,
                    userAvatar = currentAvt,
                    content = content,
                    imageURL = if (isImage) downloadUrl.toString() else "",
                    voiceURL = if (!isImage) downloadUrl.toString() else "",
                    createdAt = timeStamp,
                    hiddenForUsers = emptyList(),
                    viewedBy = emptyList()
                )
                newPostRef.set(post).await()
                PostResult.Success(postId = "")
            } else {
                PostResult.Failure("User ID is null")
            }
        } catch (e: FirebaseNetworkException) {
            PostResult.Failure(e.message ?: "Kiểm tra kết nối mạng")
        }
    }

    fun iscurrentId(): String {
        return auth.currentUser!!.uid
    }

    //cau hinh paging3
    fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 1,
                enablePlaceholders = false,
                prefetchDistance = 20,
                initialLoadSize = 20,
                maxSize = 100,
                jumpThreshold = 10
            ),
            pagingSourceFactory = { PostPagingSource(fireStore, auth) }
        ).flow
    }

    fun invalidatePagingSource() {
        postPagingSource?.invalidate()
    }

    private val likeMutex = Mutex()

    suspend fun updateLikePost(postId: String, icon: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val fireStore = FirebaseFirestore.getInstance()
        val timeStamp = Timestamp(Date())

        likeMutex.withLock {
            try {
                fireStore.runTransaction { transaction ->
                    // Xem bản ghi "like" hiện có
                    val likeQuerySnapshot = Tasks.await(
                        fireStore.collection("likes")
                            .whereEqualTo("postId", postId)
                            .whereEqualTo("userId", userId)
                            .get()
                    )

                    if (likeQuerySnapshot.isEmpty) {
                        // Tạo bản ghi "like" mới nếu chưa tồn tại
                        val postDocument =
                            transaction.get(fireStore.collection("posts").document(postId))
                        val ownerId = postDocument.getString("userId") ?: ""
                        val newLike = Like(
                            postId = postId,
                            userId = userId,
                            ownerId = ownerId,
                            reactions = listOf(icon),
                            createdAt = timeStamp,
                            status = LikeStatus.NEW
                        )
                        transaction.set(fireStore.collection("likes").document(), newLike)
                    } else {
                        // Cập nhật bản ghi "like" hiện có
                        val likeDocumentSnapshot = likeQuerySnapshot.documents[0]
                        val existingLike = likeDocumentSnapshot.toObject(Like::class.java)

                        if (existingLike != null) {
                            val updatedReactions = if (existingLike.reactions.size >= 4) {
                                val updatedList = existingLike.reactions.toMutableList()
                                updateReactions(updatedList, icon)
                                updatedList
                            } else {
                                existingLike.reactions.toMutableList().apply { add(icon) }
                            }

                            transaction.update(
                                likeDocumentSnapshot.reference, mapOf(
                                    "reactions" to updatedReactions,
                                    "createdAt" to timeStamp
                                )
                            )
                        } else {
                            throw IllegalStateException("Lỗi")
                        }
                    }
                }.await() // Đây là nơi bạn chờ đợi giao dịch hoàn thành
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateReactions(reactions: MutableList<String>, newIcon: String) {
        if (reactions.size >= 4) {
            reactions.removeAt(0)
        }
        reactions.add(newIcon)
    }


    fun getlikepost(postId: String, listinfolike: (List<Pair<String, List<String>>>) -> Unit) {
        val likesRef = fireStore.collection("likes").whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        likesRef.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }
            val reactionList = mutableListOf<Pair<String, List<String>>>()

            querySnapshot?.documents?.forEach { likeDoc ->
                val like = likeDoc.toObject(Like::class.java)
                like?.let {
                    val userId = like.userId
                    //laya username
                    fireStore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { userSnapshot ->
                            if (userSnapshot.exists()) {
                                val userName = userSnapshot.getString("nameUser") ?: ""
                                val reactions = like.reactions

                                // Tạo pair userName và reactions
                                val userReactions = Pair(userName, reactions)
                                reactionList.add(userReactions)

                                if (reactionList.size == querySnapshot.size()) {
                                    listinfolike(reactionList)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.e(
                                "PostRepository",
                                "Lỗi khi lấy like: ${exception.message}",
                                exception
                            )
                        }
                }
            }
        }
    }

    private val imageGenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apikey,

        generationConfig = generationConfig {
            temperature = 0.8f
            maxOutputTokens = 120
            topK = 40
        }
    )

    val currentTime = LocalTime.now()
    val currentDate = LocalDate.now()
    val dayOfWeek = when (currentDate.dayOfWeek) {
        DayOfWeek.MONDAY -> "thứ Hai"
        DayOfWeek.TUESDAY -> "thứ Ba"
        DayOfWeek.WEDNESDAY -> "thứ Tư"
        DayOfWeek.THURSDAY -> "thứ Năm"
        DayOfWeek.FRIDAY -> "thứ Sáu"
        DayOfWeek.SATURDAY -> "thứ Bảy"
        DayOfWeek.SUNDAY -> "Chủ nhật"
    }
    val dayOfMonth = currentDate.dayOfMonth

    val month = when (currentDate.monthValue) {
        1 -> "Tháng Một"
        2 -> "Tháng Hai"
        3 -> "Tháng Ba"
        4 -> "Tháng Tư"
        5 -> "Tháng Năm"
        6 -> "Tháng Sáu"
        7 -> "Tháng Bảy"
        8 -> "Tháng Tám"
        9 -> "Tháng Chín"
        10 -> "Tháng Mười"
        11 -> "Tháng Mười Một"
        else -> "Tháng Mười Hai"
    }
    val year = currentDate.year
    val timeOfDay = when {
        currentTime.isAfter(LocalTime.of(4, 0)) && currentTime.isBefore(
            LocalTime.of(
                11,
                0
            )
        ) -> "buổi sáng thứ $dayOfWeek, ngày $dayOfMonth tháng $month"

        currentTime.isAfter(LocalTime.of(11, 0)) && currentTime.isBefore(
            LocalTime.of(
                13,
                30
            )
        ) -> "buổi trưa thứ $dayOfWeek, ngày $dayOfMonth tháng $month"

        currentTime.isAfter(LocalTime.of(13, 30)) && currentTime.isBefore(
            LocalTime.of(
                18,
                30
            )
        ) -> "buổi chiều thứ $dayOfWeek, ngày $dayOfMonth tháng $month"

        currentTime.isAfter(LocalTime.of(18, 30)) && currentTime.isBefore(
            LocalTime.of(
                23,
                59
            )
        ) -> "buổi tối thứ $dayOfWeek, ngày $dayOfMonth tháng $month"

        else -> "đêm khuya thứ $dayOfWeek, ngày $dayOfMonth tháng $month"
    }

    suspend fun generateContentFromImage(imageBytes: Bitmap): String {
        val inputContent = content {
            image(imageBytes)
            text(
                """
            Này AI, giúp mình tạo caption này để chia sẻ lên mạng xã hội nhé! 😎

            ## Nội dung:
            - **Phân tích bức ảnh**: Mô tả ngắn gọn về nội dung chính của bức ảnh, tập trung vào các yếu tố nổi bật như màu sắc chủ đạo, bố cục, chủ thể, và không khí của bức ảnh.
            - **Xác định cảm xúc**: Dựa trên phân tích trên, cho mình biết cảm xúc chủ đạo mà bức ảnh này truyền tải (ví dụ: vui vẻ, buồn bã, hoài niệm, phấn khích, ...).

            ## Hiệu ứng ngôn ngữ:
            - Dùng ngôn ngữ hình ảnh, ẩn dụ để tạo sự sinh động.
            - Thêm một chút hài hước châm biếm, nhưng cũng đừng quên giữ sự sâu sắc nhé!
            - Đừng quên thêm emoji để làm caption thêm phần sinh động và dễ thương 🥰.

            ## Ngữ cảnh:
            - Viết bằng Tiếng Việt, ngôi thứ nhất "Mình" để tạo cảm giác gần gũi.
            - Sử dụng ngôn ngữ tự nhiên, phù hợp để chia sẻ lên mạng xã hội, cho mọi người cùng xem nhé!

            ## Điều kiện bắt buộc:
            - Caption chỉ cần dưới 150 ký tự, ngắn gọn mà vẫn thú vị.
            - Phù hợp để chia sẻ lên mạng xã hội.
            - Không tạo hashtag.
            - Chỉ sử dụng thông tin về thời gian ($timeOfDay) hoặc ngày tháng ($dayOfWeek, ngày $dayOfMonth tháng $month năm $year) khi thật sự cần thiết nhé! ✌️
            """
            )
        }
        val response = imageGenerativeModel.generateContent(inputContent)
        return response.text.toString()
    }


    suspend fun generateContentFromText(prompt: String): String {

        val punctuatedPrompt = addPunctuation(prompt)

        val contentType = analyzeContentType(punctuatedPrompt)


        val caption = generateCaption(punctuatedPrompt, contentType)

        return caption
    }

    suspend fun addPunctuation(prompt: String): String {
        // Nội dung quy tắc thêm dấu câu
        val addPunctuationRules = """
        Bạn là một trợ lý AI thông minh có khả năng phân tích và thêm dấu câu vào văn bản để đảm bảo tính chính xác ngữ pháp và dễ đọc. Khi thêm dấu câu, hãy tuân theo các quy tắc sau:

        1. Dùng dấu chấm (.) để kết thúc câu hoàn chỉnh.
        2. Dùng dấu phẩy (,) để tách các cụm từ, danh sách, hoặc thêm thông tin bổ sung.
        3. Dùng dấu chấm than (!) để biểu thị sự ngạc nhiên hoặc cảm xúc mạnh mẽ.
        4. Dùng dấu hỏi (?) cho câu hỏi.
        5. Đảm bảo rằng mỗi câu có cấu trúc ngữ pháp hoàn chỉnh với chủ ngữ và vị ngữ.
        6. Dùng dấu ngoặc kép ("") để bao quanh lời nói trực tiếp.
        7. Dùng dấu gạch ngang (-) để chỉ sự ngắt quãng hoặc dừng lại trong suy nghĩ.
        8. Lưu ý đến ngữ cảnh và ý nghĩa của câu khi thêm dấu câu.
        9. Linh hoạt và sáng tạo khi thêm dấu câu để văn bản trở nên tự nhiên và dễ đọc.

        ## Ví dụ:

        ### Bài hát (Tiếng Việt):

        **Input:** đường dài hun hút gió mưa giăng lối anh bước đi lẻ loi tìm em giữa đêm tối
        **Output:** Đường dài hun hút, gió mưa giăng lối, anh bước đi lẻ loi, tìm em giữa đêm tối.

        ### Thơ (Tiếng Việt):

        **Input:** mưa rơi trên phố nhỏ em bước đi lặng lẽ bóng dáng ai xa mờ trong màn mưa chiều
        **Output:** Mưa rơi trên phố nhỏ, em bước đi lặng lẽ. Bóng dáng ai xa mờ trong màn mưa chiều.

        ### Bài hát (Tiếng Việt):

        **Input:** chiều nay không có em anh lang thang trên phố vắng nhớ về những kỷ niệm xưa
        **Output:** Chiều nay không có em, anh lang thang trên phố vắng, nhớ về những kỷ niệm xưa.

        ### Bài hát (Tiếng Anh):

        **Input:** yesterday all my troubles seemed so far away now it looks as though theyre here to stay oh i believe in yesterday
        **Output:** Yesterday, all my troubles seemed so far away. Now it looks as though they're here to stay. Oh, I believe in yesterday.

        ### Câu chuyện kịch (Tiếng Việt):

        **Input:** cô ấy bước vào phòng với vẻ mặt đầy tức giận anh ta ngồi trên ghế sofa không nói một lời không khí căng thẳng bao trùm cả căn phòng
        **Output:** Cô ấy bước vào phòng với vẻ mặt đầy tức giận. Anh ta ngồi trên ghế sofa, không nói một lời. Không khí căng thẳng bao trùm cả căn phòng.

        ### Ví dụ chung (Tiếng Anh và Tiếng Việt):

        **Input:** i love you anh yêu em too what do you want to eat today hôm nay em muốn ăn gì
        **Output:** "I love you." "Anh yêu em too." "What do you want to eat today?" "Hôm nay em muốn ăn gì?"

        ### Ví dụ với tiếng lóng và emoji:

        **Input:** omg trời ơi cái váy này xinh quá đi à tui phải mua nó thôi 👗
        **Output:** OMG! Trời ơi, cái váy này xinh quá đi à! Tui phải mua nó thôi! 👗

        ## Văn bản cần thêm dấu câu:
        
        Đây là văn bản không có dấu câu: "$prompt"
        Hãy thêm dấu câu vào văn bản trên.
    """

        // Tạo content cho yêu cầu thêm dấu câu
        val analysisInputContent = content { text(addPunctuationRules) }

        // Gửi yêu cầu đến mô hình để xử lý văn bản
        val analysisResponse = imageGenerativeModel.generateContent(analysisInputContent)

        // Trả về kết quả đã được thêm dấu câu, cắt bỏ phần dư thừa trước văn bản kết quả
        return analysisResponse.text.toString()
            .substringAfter("Đoạn văn đã được thêm dấu câu: ")
            .trim()
    }

    suspend fun analyzeContentType(punctuatedPrompt: String): String {
        // Quy tắc phân tích loại nội dung của đoạn văn
        val analyzeContentRules = """
        Bạn là một trợ lý AI thông minh, có khả năng phân tích đoạn văn đã được thêm dấu câu để xác định loại nội dung. 
        Hãy phân tích đoạn văn sau và cho biết đó là bài hát, câu chuyện, thơ, drama, hay các thể loại liên quan đến chính trị, tôn giáo, chuyện vui, chuyện buồn, chuyện nhạy cảm, chuyện kinh dị, chuyện tâm linh.

        ## Quy tắc phân biệt:

        **Bài hát:**
        * Thường có vần điệu, nhịp điệu rõ ràng.
        * Chia thành các khổ, có thể có điệp khúc lặp lại.
        * Thường tập trung vào cảm xúc như tình yêu, nỗi buồn, niềm vui.
        * Sử dụng nhiều hình ảnh, ẩn dụ, so sánh.
        * Ví dụ: "Em ơi Hà Nội phố, phố ta còn đó, nhà tôi vẫn thế..." (Nhạc sĩ Phú Quang)

        **Câu chuyện:**
        * Viết dưới dạng văn xuôi, không có vần điệu hay nhịp điệu cố định.
        * Câu chuyện có cốt truyện rõ ràng với các tình tiết phức tạp.
        * Thường có nhân vật, bối cảnh, và diễn biến rõ ràng.
        * Ví dụ: "Chuyện người con gái Nam Xương" (Nguyễn Dữ)

        **Thơ:**
        * Ngắn gọn, súc tích, giàu hình ảnh.
        * Thường có vần điệu, nhưng không nhất thiết phải có nhịp điệu rõ ràng.
        * Thường tập trung vào cảm xúc, suy tư, hoặc miêu tả cảnh vật.
        * Ví dụ: "Tình yêu như cánh chim bay xa, để lại trong lòng ta nỗi nhớ thiết tha."

        **Drama (Kịch):**
        * Kể về một câu chuyện với nhiều xung đột, kịch tính.
        * Thường có nhiều nhân vật và đối thoại.
        * Tập trung vào các mối quan hệ, tình cảm, và sự thay đổi của nhân vật.
        * Ví dụ: "Romeo và Juliet" (Shakespeare)

        **Chính trị, tôn giáo:**
        * Đề cập đến các vấn đề liên quan đến chính trị, tôn giáo, hoặc các vấn đề xã hội nhạy cảm.
        * Thường có tính chất tranh luận, phản biện, hoặc tuyên truyền.
        * Ví dụ: "Tuyên ngôn Độc lập" (Hồ Chí Minh)

        **Chuyện vui:**
        * Có tính chất hài hước, gây cười.
        * Thường có tình huống bất ngờ, lời nói đùa, hoặc hành động hài hước.
        * Ví dụ: "Một con vịt đi vào quán bar..."

        **Chuyện buồn:**
        * Kể về những sự kiện đau buồn, mất mát, hoặc thất bại.
        * Thường gợi lên cảm xúc buồn bã, thương cảm, hoặc đồng cảm.
        * Ví dụ: "Chiếc lá cuối cùng" (O. Henry)

        **Chuyện nhạy cảm:**
        * Đề cập đến các chủ đề nhạy cảm như tình dục, bạo lực, hoặc các vấn đề cá nhân.
        * Cần cân nhắc kỹ trước khi chia sẻ hoặc thảo luận.
        * Ví dụ: Một đoạn văn về các trải nghiệm cá nhân đau buồn, các mối quan hệ phức tạp.

        **Chuyện kinh dị:**
        * Thường có yếu tố gây sợ hãi, kinh hoàng, hoặc ghê rợn.
        * Mục đích thường nhằm mục đích giải trí, kích thích cảm giác mạnh.
        * Ngôn ngữ mô tả chi tiết, sống động để tạo ra cảm giác sợ hãi.
        * Ví dụ: "Cánh cửa cọt kẹt mở ra, và từ đó là một bóng đen."

        **Chuyện tâm linh:**
        * Thường đề cập đến các khía cạnh siêu nhiên, tâm linh, tôn giáo, hoặc triết học.
        * Mục đích thường là khai sáng hoặc tìm kiếm ý nghĩa cuộc sống.
        * Cảm xúc có thể là kinh ngạc, tò mò, hoặc bình an.
        * Ngôn ngữ sử dụng biểu tượng, ẩn dụ hoặc triết lý.
        * Ví dụ: "Một đêm khuya, tôi cảm nhận được sự hiện diện của ai đó bên cạnh."

        ## Lưu ý:
        * Cần phân biệt giữa "chuyện vui" và "drama" dựa trên cảm xúc và ngữ cảnh, không chỉ dựa vào cách diễn đạt.
        
        ## Đoạn văn cần phân tích:
        
        "$punctuatedPrompt"
        
        ## Trả lời:
        Loại nội dung: [Loại nội dung bạn xác định]
        Nếu không xác định được thể loại, hãy trả lời: Tóm tắt "$punctuatedPrompt"
    """

        // Tạo nội dung yêu cầu phân tích
        val analysisInputContent = content { text(analyzeContentRules) }

        // Gửi yêu cầu tới mô hình AI để phân tích loại nội dung
        val analysisResponse = imageGenerativeModel.generateContent(analysisInputContent)

        // Trả về kết quả phân tích loại nội dung
        return analysisResponse.text.toString()
            .substringAfter("Loại nội dung: ")
            .trim()
    }


    suspend fun generateCaption(punctuatedPrompt: String, contentType: String): String {
        // Xác định phong cách caption dựa trên loại nội dung
        val captionStyle = when (contentType.lowercase()) {
            "bài hát" -> "giật gân, giật tít, âm nhạc 🎵🎶"
            "câu chuyện" -> "kể chuyện, hấp dẫn, lôi cuốn 📖📚"
            "drama" -> "kịch tính, căng thẳng, hồi hộp 🎭🎬"
            "thơ" -> "lãng mạn, sâu lắng, trữ tình 🖋️📝"
            "chính trị" -> "thông tin, chính xác, khách quan 📰📢"
            "tôn giáo" -> "tôn kính, trang nghiêm, thành kính 🙏✝️"
            "chuyện vui" -> "hài hước, vui nhộn, cười thả ga 😂🤣"
            "chuyện buồn" -> "cảm động, xúc động, lắng đọng 😔😢"
            "chuyện nhạy cảm" -> "cân nhắc, tế nhị, kín đáo 🤫🤐"
            "chuyện kinh dị" -> "rùng rợn, ám ảnh, lạnh sống lưng 😱👻"
            "chuyện tâm linh" -> "huyền bí, sâu sắc, suy ngẫm 🤔✨"
            else -> "thân mật, hài hước 😊😉"
        }

        // Quy tắc viết caption cho mỗi loại nội dung
        val captionRules = """
        Viết caption cho đoạn văn sau: "$punctuatedPrompt" (đã được thêm dấu câu).
        Đây là đoạn văn về [$contentType].
        Hãy phân tích nội dung và viết caption phù hợp với phong cách: $captionStyle.
        Viết caption không quá 200 ký tự, ưu tiên khoảng 100 ký tự.
        Thêm 1-2 emoji vào mỗi câu để làm cho caption thêm sinh động.
        Kết thúc caption bằng hashtag #$contentType.
        Có thể thêm thông tin về thời gian hiện tại ($timeOfDay) hoặc ngày tháng ($dayOfWeek, ngày $dayOfMonth tháng $month năm $year) nếu phù hợp.
    """

        // Tạo nội dung yêu cầu để gửi cho mô hình
        val inputContent = content { text(captionRules) }

        // Gửi yêu cầu và lấy phản hồi từ mô hình
        val response = imageGenerativeModel.generateContent(inputContent)

        // Trả về caption đã được tạo
        return response.text.toString().trim()
    }

    sealed class PostResult {
        data class Success(val postId: String) : PostResult()
        data class Failure(val error: String) : PostResult()
    }


    fun observePost(postId: String) {
        val postRef = fireStore.collection("posts").document(postId)
        postRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val post = snapshot.toObject(Post::class.java)


            } else {
                Log.d("postrepo", "Current data: null")
            }
        }
    }


    suspend fun getLatestPost(): Post? {
        val snapshot = fireStore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        return if (!snapshot.isEmpty) {
            snapshot.documents[0].toObject(Post::class.java)
        } else {
            null
        }
    }


    suspend fun deletePost(postId: String): Boolean {
        val postRef = fireStore.collection("posts").document(postId)
        val currentId = auth.currentUser?.uid

        return if (currentId != null) {
            try {
                val postSnapshot = postRef.get().await()
                if (postSnapshot.exists()) {
                    val post = postSnapshot.toObject(Post::class.java)
                    if (post != null) {
                        if (post.userId == currentId) {
                            val decodedImageUrl = URLDecoder.decode(post.imageURL, "UTF-8")
                            val decodedVoiceUrl = URLDecoder.decode(post.voiceURL, "UTF-8")
                            when {
                                post.imageURL == "" && post.voiceURL != "" -> {
                                    val voiceRef = storage.getReferenceFromUrl(decodedVoiceUrl)
                                    Log.d("XOAVOICE", "delete URL obtained: $voiceRef")
                                    voiceRef.delete().await()
                                }

                                post.imageURL != "" && post.voiceURL == "" -> {
                                    val imageRef = storage.getReferenceFromUrl(decodedImageUrl)
                                    imageRef.delete().await()
                                }
                            }

                            //xoa like
                            val likesSnapshot = fireStore.collection("likes")
                                .whereEqualTo("postId", postId)
                                .get()
                                .await()
                            if (!likesSnapshot.isEmpty) {
                                likesSnapshot.documents.forEach { it.reference.delete().await() }
                            }

                            // xoa noi dung cmt
                            val messagesSnapshot = fireStore.collection("messages")
                                .whereEqualTo("postId", postId)
                                .get()
                                .await()
                            if (!messagesSnapshot.isEmpty) {
                                messagesSnapshot.documents.forEach {
                                    it.reference.update(
                                        mapOf(
                                            "postId" to "",
                                            "imageUrl" to "",
                                            "voiceUrl" to "",
                                            "timestamp" to "",
                                            "content" to "",
                                            "avtpost" to ""
                                        )
                                    ).await()
                                }
                            }


                            postRef.delete().await()

                            true
                        } else {
                            postRef.update("hiddenForUsers", FieldValue.arrayUnion(currentId))
                                .await()
                            true
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.d("PostRepository", "Error deleting post: ${e}")
                false
            }
        } else {
            false
        }
    }

    suspend fun updateViewedBy(postId: String): Boolean {
        val postRef = fireStore.collection("posts").document(postId)
        val currentId = auth.currentUser?.uid ?: return false // Handle no user logged in

        return try {
            postRef.update("viewedBy", FieldValue.arrayUnion(currentId)).await()
            Log.d("PostRepository", "Post $postId viewed by $currentId")
            true
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                Log.e("PostRepository", "Document $postId not found", e)
            } else {
                Log.e("PostRepository", "Error updating viewedBy for $postId", e)
            }
            false
        }
    }

//    fun updateViewedBy(postId: String, onResult: (Boolean) -> Unit) {
//        val postRef = fireStore.collection("posts").document(postId)
//        val currentId = auth.currentUser?.uid
//        postRef.update("viewedBy", FieldValue.arrayUnion(currentId))
//            .addOnSuccessListener {
//                Log.d("PostRepository", "Post $postId viewed by $currentId")
//                onResult(true)
//            }
//            .addOnFailureListener { e ->
//                Log.e("PostRepository", "Error $postId add viewed", e)
//                onResult(false)
//            }
//    }


    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    private var postListenerRegistration: ListenerRegistration? = null

    fun listenForNewPosts(onNewPostCount: (Int) -> Unit): ListenerRegistration? {
        val currentUserId = auth.currentUser?.uid ?: return null

        // Launch a coroutine to fetch friend IDs
        repositoryScope.launch {
            val friendIds = getFriendIds(currentUserId)

            if (friendIds.isEmpty()) {
                onNewPostCount(0)
                return@launch
            }

            // Perform the query only on posts from friends
            val postRef = fireStore.collection("posts")
                .whereIn("userId", friendIds)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            // Add snapshot listener to the query
            postListenerRegistration = postRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostRepository", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.toObjects(Post::class.java)
                    // Filter posts that have not been viewed by the current user
                    val newPosts = posts.filter { post ->
                        !post.viewedBy.contains(currentUserId)
                    }
                    onNewPostCount(newPosts.size)
                }
            }
        }
        return postListenerRegistration
    }

    fun stopListeningForNewPosts() {
        postListenerRegistration?.remove()
        postListenerRegistration = null
    }

    private suspend fun getFriendIds(currentUserId: String): List<String> {
        return try {
            val query1 = fireStore.collection("friendships")
                .whereEqualTo("uid1", currentUserId)
                .whereEqualTo("state", "Accepted")

            val query2 = fireStore.collection("friendships")
                .whereEqualTo("uid2", currentUserId)
                .whereEqualTo("state", "Accepted")

            val combinedTask = Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())
                .await()

            val friendIds = mutableSetOf<String>()
            for (result in combinedTask) {
                result.documents.forEach { doc ->
                    val uid1 = doc.getString("uid1")
                    val uid2 = doc.getString("uid2")
                    if (uid1 != currentUserId && uid1 != null) {
                        friendIds.add(uid1)
                    }
                    if (uid2 != currentUserId && uid2 != null) {
                        friendIds.add(uid2)
                    }
                }
            }
            friendIds.toList()
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching friend IDs", e)
            emptyList()
        }
    }

}
