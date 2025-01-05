import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mysocialproject.R
import com.example.mysocialproject.ui.feature.chat.ChatActivity
import com.example.mysocialproject.ui.feature.home.CreatePostActivity
import com.example.mysocialproject.model.Reaction
import com.example.mysocialproject.model.Message
import com.example.mysocialproject.model.User

class NotificationService : Service() {

    companion object {
        private var notificationId = 1
        private var notifilikeId = 100
    }

    private val repository = NotificationRepository()

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        repository.listenForLatestMessage { message, sender ->
            if (message != null && sender != null) {
                showNotification(message, sender)
            }
        }
        repository.listenForNewLikes { like, user ->
            if (like != null && user != null) {
                showLikeNotification(like, user)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle("SnapMoment")
            .setContentText("Xin chào bạn!")
            .setSmallIcon(R.drawable.ic_heart)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Foreground service notifications"
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(1, notification)

        return START_STICKY
    }

    private fun showNotification(message: Message, sender: User) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh thông báo (nếu cần)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "message_channel",
                "Tin nhắn",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, ChatActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Tạo thông báo
        val notificationBuilder = NotificationCompat.Builder(this, "message_channel")
            .setSmallIcon(R.drawable.ic_heart) // Thay bằng icon của bạn
            .setContentTitle("Tin nhắn mới từ ${sender.nameUser}")
            .setContentText(message.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(notificationId++, notificationBuilder.build())
    }


    private fun showLikeNotification(reaction: Reaction, user: User) {

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh thông báo (nếu cần)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "like_channel",
                "Like mới",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Tạo PendingIntent để mở ứng dụng khi người dùng nhấn vào thông báo
        val intent = Intent(this, CreatePostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Tạo thông báo
        val notificationBuilder = NotificationCompat.Builder(this, "like_channel")
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("${user.nameUser}")
            .setContentText("Đã thêm một hoạt động vào bài viết của bạn ^^")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Hiển thị thông báo
        notificationManager.notify(notifilikeId++, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.cancelListen()
    }
}