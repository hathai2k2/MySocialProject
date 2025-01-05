package com.example.mysocialproject.ui.feature.home

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentRecordBinding
import com.example.mysocialproject.ui.feature.bottomdialogai.PromptDialogVoice
import com.example.mysocialproject.ui.feature.chat.FriendListPrivateBottomSheet
import com.example.mysocialproject.repository.PostRepository
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import rm.com.audiowave.OnProgressListener
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class RecordFragment : Fragment() {

    lateinit var binding: FragmentRecordBinding
    private var isRecording: Boolean = false
    private var isPlaying: Boolean = false
    private lateinit var fileName: String
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var postViewModel: PostViewModel
    private lateinit var mediaPlayer: MediaPlayer
    private val RECORDING_MEDIA_RECORDER_MAX_DURATION = 60000
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var totalTimer: String
    private val handler = Handler(Looper.getMainLooper())

    private var prompt: String = ""
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                binding.play.text = "Ghi âm"
                Toast.makeText(
                    context,
                    "Yêu cầu cấp quyền thu âm, vào cài đặt ứng dụng để cấp quyền!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                isRecording = false
                isPlaying = false
                // Proceed with recording setup
            }
        }

    private lateinit var speechRecognizer: SpeechRecognizer
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognizer", "onReadyForSpeech called")
        }

        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "onBeginningOfSpeech called")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d("SpeechRecognizer", "onRmsChanged: $rmsdB")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d("SpeechRecognizer", "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d("SpeechRecognizer", "onEndOfSpeech called")
        }

        override fun onError(error: Int) {
            speechRecognizer.stopListening()
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Lỗi âm "
                SpeechRecognizer.ERROR_CLIENT -> "Lỗi cliethanhnt"
                SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng"
                SpeechRecognizer.ERROR_NO_MATCH -> "Không tìm thấy kết quả phù hợp"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "SpeechRecognizer đang bận"
                SpeechRecognizer.ERROR_SERVER -> "Lỗi server"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian chờ"
                else -> "Lỗi không xác định"
            }
            Log.e("SpeechRecognizer", "Lỗi: $errorMessage")
        }


        override fun onResults(results: Bundle?) {
            Log.d("SpeechRecognizer", "onResults called")
            val recognizedText =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
            prompt = recognizedText
            if (prompt != "") {
                binding.btnGenativeAI.visibility = View.VISIBLE
            }
            postViewModel.genarateTemp(recognizedText)
            postViewModel.recognizedText.observe(viewLifecycleOwner) {
                binding.edt1.setText(it)
                Log.d("SpeechRecognizer", "Prompt nhận đc: $it")
            }

            Log.d("SpeechRecognizer", "Văn bản được nhận diện: $recognizedText")
            Log.d("SpeechRecognizer", "Prompt nhận đc: $prompt")
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialText =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    ?: ""
            Log.d("SpeechRecognizer", "Kết quả tạm thời: $partialText")


        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }
    }

    private fun setupSpeechRecognizer() {

        speechRecognizer.setRecognitionListener(recognitionListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, container, false)
        postViewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        requestPermissions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val audioFileName = UUID.randomUUID().toString() + ".aac"
        fileName = File(requireActivity().externalCacheDir, audioFileName).absolutePath
        requestPermissions()
        mediaPlayer = MediaPlayer()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        //record
        binding.btnrecord.setOnClickListener {
            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            if (vibrator?.hasAmplitudeControl() == true) {
                val vibrationEffect = VibrationEffect.createOneShot(
                    30, VibrationEffect.EFFECT_TICK
                )
                vibrator.vibrate(vibrationEffect)
            } else {

                vibrator?.vibrate(30)
            }
            toggleRecording()
        }


        binding.play.apply {
            text = "Ghi âm"

            if (isEnabled == false) {
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            binding.play.setOnClickListener {
                val vibrator = requireContext().getSystemService(Vibrator::class.java)
                if (vibrator?.hasAmplitudeControl() == true) {
                    val vibrationEffect = VibrationEffect.createOneShot(
                        40, VibrationEffect.EFFECT_TICK
                    )
                    vibrator.vibrate(vibrationEffect)
                } else {

                    vibrator?.vibrate(40)
                }
                if (!isPlaying) {
                    playAudio(fileName)
                    binding.play.text = "Dừng"
                } else {
                    stopPlaying()
                    binding.play.text = "Phát"
                }
            }
        }

        setupWaveformView()

        postViewModel.postResultLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PostRepository.PostResult.Success -> {
                    resetVoiceRecording()
                }

                else -> {
                    Toast.makeText(requireContext(), "Vui lòng thử lại sau", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        binding.btnLeft.setOnClickListener {
            resetVoiceRecording()
        }

        ///tom tat ghi am
        binding.btnGenativeAI.setOnClickListener {
            val dialog = PromptDialogVoice(prompt)
            dialog.show(childFragmentManager, "prompt_dialog")
            Log.d("TAG_voice", "onViewCreated: $prompt")
            postViewModel.contentvoice.observe(viewLifecycleOwner) { content ->
                binding.edt1.setText(content ?: "")
            }
        }
        postAudio()
    }


    //reset ghi am
    private fun resetVoiceRecording() {

        val audioFile = File(fileName)
        if (audioFile.exists()) {
            audioFile.delete()
        }
        binding.wave.setRawData(ByteArray(0))
        isRecording = false
        isPlaying = false
        binding.play.apply {
            text = "Ghi âm"
            isEnabled = false
        }
        mediaPlayer.release()
        binding.wave.progress = 0f
        binding.tvTimer.text = "00:59"
        binding.edt1.text.clear()
        binding.btnLeft.visibility = View.INVISIBLE
        binding.btnPost.visibility = View.GONE
        binding.btnrecord.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.btnGenativeAI.visibility = View.INVISIBLE
        speechRecognizer.destroy()
        postViewModel.clearContentvoice()
        postViewModel.clearContentemp()
        setupSpeechRecognizer()
    }

    private fun capquyencam() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        dialog.setTitle("Bật quyền truy cập Micro")
            .setMessage("Đến cài đặt ứng dụng và đảm bảo SnapMoment có quyền truy cập micro của bạn")
            .setPositiveButton("ĐẾN CÀI ĐẶT") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("HỦY") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun toggleRecording() {

        if (!allPermissionsGranted()) {
            capquyencam()
        } else {
            if (!isRecording) {
                startRecording()
                val scaleDownAnimator =
                    ValueAnimator.ofFloat(0.92f, 1f).apply { // Giả sử icon ban đầu là 80dp
                        duration = 200
                        interpolator = DecelerateInterpolator()
                        addUpdateListener { valueAnimator ->
                            val scale = valueAnimator.animatedValue as Float
                            binding.btnrecord.scaleX = scale
                            binding.btnrecord.scaleY = scale
                        }
                    }

                scaleDownAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.btnrecord.setImageResource(R.drawable.img_stop_voice)
                        binding.btnrecord.setBackgroundColor(Color.TRANSPARENT)
                    }
                })


                scaleDownAnimator.start()

                binding.play.text = "Đang ghi âm"
                binding.play.isEnabled = false
            } else {
                stopRecording()
                binding.btnrecord.scaleX = 1f
                binding.btnrecord.scaleY = 1f
                binding.btnrecord.setImageResource(R.drawable.img_start_voice)
                binding.btnrecord.setBackgroundColor(Color.TRANSPARENT)
                binding.play.text = "Phát"
            }
        }

    }

    //start nghe
    private fun startSpeechRecognition(filename: String) {

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putStringArrayListExtra(
                RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("vi-VN", "en-US")
            )
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, Uri.fromFile(File(filename)))
        }
        speechRecognizer.startListening(recognizerIntent)
    }

    private fun startRecording() {
        if (allPermissionsGranted()) {
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioChannels(1)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(64000)
                    setMaxDuration(RECORDING_MEDIA_RECORDER_MAX_DURATION)
                    setOutputFile(fileName)
                    prepare()
                    start()
                    speechRecognizer.setRecognitionListener(recognitionListener)
                }

                isRecording = true

                startTimer()
            } catch (e: IOException) {
                Log.e("TAGY", "startRecording: ${e.message}")
            }
        } else {
            requestPermissions()
            capquyencam()
        }
    }

    ////
    private fun stopRecording() {
        mediaRecorder.apply {

            Handler(Looper.getMainLooper()).postDelayed({
                stop()
                release()
            }, 100)
        }

        isRecording = false
        mediaRecorder = MediaRecorder()

        countDownTimer.cancel()
        binding.tvTimer.text = "$totalTimer"
        displayWaveform(fileName)
        Log.d("TAGY", "$fileName")

        binding.btnrecord.visibility = View.GONE
        binding.btnPost.visibility = View.VISIBLE
        binding.btnLeft.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.play.isEnabled = true
        }, 100)

    }


    //hien thi progress
    private val updateWaveform = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (isPlaying) {
                    val progress = (player.currentPosition.toFloat() / player.duration) * 100
                    Log.d("TAGY", "$progress , $player.duration")
                    binding.wave.progress = progress
                    handler.postDelayed(this, 1) // Update every 50ms for smooth animation
                }
            }
        }
    }

    //play
    private fun playAudio(audioFilePath: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
            isPlaying = true
            startSpeechRecognition(audioFilePath)
            handler.post(updateWaveform)
            mediaPlayer.setOnCompletionListener {
                stopPlaying()
            }
        } catch (e: IOException) {
            Log.e("TAGY", "Error playing audio: ${e.message}")
        }
    }

    private fun stopPlaying() {
        mediaPlayer.stop()
        binding.play.text = "Phát"
        isPlaying = false
        speechRecognizer.stopListening()
        handler.removeCallbacks(updateWaveform)
        binding.wave.progress = 0f
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        speechRecognizer?.destroy()
        handler.removeCallbacksAndMessages(null)
    }


    //way
    private fun setupWaveformView() {
        binding.wave.onProgressListener = object : OnProgressListener {
            override fun onProgressChanged(progress: Float, byUser: Boolean) {
                Log.d("TAGY", "Progress set: $progress, and it's $byUser that user did this")
                if (byUser && isPlaying) {
                    val seekToPosition = (mediaPlayer!!.duration * progress / 100.0).toInt()
//                        postRowVoiceBinding.wave.progress = progress
                    Log.d("TAGY", "Seeking to position: $seekToPosition")
                    mediaPlayer?.seekTo(seekToPosition)

                }
            }

            override fun onStartTracking(progress: Float) {
                Log.d("TAGY", "Started tracking from $progress")
            }

            override fun onStopTracking(progress: Float) {

                if (isPlaying) {
                    Log.d("TAGY", "Stopped tracking at $progress")
                    val seekToPosition = (mediaPlayer!!.duration * (progress / 100.0)).toInt()
                    Log.d("TAGY", "Seeking to position: $seekToPosition")
                    mediaPlayer?.seekTo(seekToPosition)
                }

            }
        }
    }

    //dem xem bn s
    private fun startTimer() {
        countDownTimer =
            object : CountDownTimer(RECORDING_MEDIA_RECORDER_MAX_DURATION.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val elapsedTime = RECORDING_MEDIA_RECORDER_MAX_DURATION - millisUntilFinished
                    val seconds = elapsedTime / 1000
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    val sd = millisUntilFinished / 1000
                    binding.tvTimer.text = String.format("%02d:%02d", minutes, sd % 60)
                    totalTimer = String.format("%02d:%02d", minutes, remainingSeconds)
                }

                override fun onFinish() {
                    if (isRecording) {
                        stopRecording()
                        binding.play.text = "phát"
                    }
                }

            }.start()
    }


    private fun displayWaveform(audioFilePath: String) {
        try {
            val inputStream = FileInputStream(audioFilePath)
            val audioData = inputStream.readBytes()

            binding.wave.setRawData(audioData)

            Log.d("TAGY", "Waveform ok")
            Log.d("TAGY", "$fileName")
        } catch (e: IOException) {
            Log.e("TAGY", "Error reading: ${e.message}")
        }
    }

    ///dang baiiii
    private fun postAudio() {
        binding.btnPost.setOnClickListener {
            binding.btnPost.isEnabled = false
            binding.btnLeft.visibility = View.INVISIBLE
            binding.btnPost.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            val fileUri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", File(fileName)
            )

            postViewModel.addPost(fileUri, binding.edt1.text.toString(), false)
        }
        binding.btnPost.setOnLongClickListener {
            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            vibrator?.vibrate(50)

            val content = binding.edt1.text.toString()

            val fileUri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", File(fileName)
            )
            val bottomSheet = FriendListPrivateBottomSheet(null, fileUri, content)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
            true
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
