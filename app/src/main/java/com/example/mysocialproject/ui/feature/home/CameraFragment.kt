package com.example.mysocialproject.ui.feature.home

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.FragmentCameraBinding
import com.example.mysocialproject.ui.feature.bottomdialogai.PromptDialog
import com.example.mysocialproject.ui.feature.chat.FriendListprivateBottomSheet
import com.example.mysocialproject.ui.feature.repository.PostRepository
import com.example.mysocialproject.ui.feature.viewmodel.AuthViewModel.Companion.REQUEST_IMAGE_GET
import com.example.mysocialproject.ui.feature.viewmodel.PostViewModel
import java.io.File
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.O)
class CameraFragment : Fragment() {

    lateinit var viewBinding: FragmentCameraBinding

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    lateinit var cameraSelector: CameraSelector

    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo

    private var iszoom = false
    private var currentFlashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var postViewModel: PostViewModel
    private lateinit var cameraProvider: ProcessCameraProvider
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    context,
                    "Quyền truy cập máy ảnh đã bị từ chối",
                    Toast.LENGTH_SHORT
                ).show()
                disiablebtn()
            } else {
                startCamera()
            }
        }
    private val hideSeekBarRunnable = Runnable { viewBinding.brightnessSb.visibility = View.GONE }
    private val hideSeekBarHandler = Handler(Looper.getMainLooper())
    private fun disiablebtn() {
        viewBinding.buttonSwitchCamera.isEnabled = false
        viewBinding.buttonFlash.isEnabled = false
        viewBinding.Btnnratio1x.isEnabled = false
        viewBinding.btnExposure.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        postViewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
// kiem tra xem tat ca quyen da ok chua
        if (allPermissionsGranted()) {
            startCamera()

        } else {
            requestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        viewBinding.btnChoose.setOnClickListener {
            choosePhotoFromGallery()
        }

        viewBinding.buttonCapture.setOnClickListener {
            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            if (vibrator?.hasAmplitudeControl() == true) {
                val vibrationEffect = VibrationEffect.createOneShot(
                    40,
                    VibrationEffect.EFFECT_TICK
                )
                vibrator.vibrate(vibrationEffect)
            } else {

                vibrator?.vibrate(40)
            }
            val scaleDownAnimator =
                ValueAnimator.ofFloat(0.92f, 1f).apply {
                    duration = 200
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { valueAnimator ->
                        val scale = valueAnimator.animatedValue as Float
                        viewBinding.buttonCapture.scaleX = scale
                        viewBinding.buttonCapture.scaleY = scale
                    }
                }

            scaleDownAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {

                }
            })
            scaleDownAnimator.start()


            takePhoto()
        }

        viewBinding.buttonSwitchCamera.setOnClickListener {
            swipcam()
        }
        viewBinding.buttonFlash.setOnClickListener {
            flash()
        }

        viewBinding.Btnnratio1x.setOnClickListener {

            zoom()
        }
        tabtofocus()

        postViewModel.postResultLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PostRepository.PostResult.Success -> {
                    deleImg()
                }
                else -> {
                    Toast.makeText(requireContext(), "Vui lòng thử lại sau", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        viewBinding.btnLeft.setOnClickListener {
            startCamera()
            deleImg()
        }
        viewBinding.btnExposure.setOnClickListener {
            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            if (vibrator?.hasAmplitudeControl() == true) {
                val vibrationEffect = VibrationEffect.createOneShot(
                    40,
                    VibrationEffect.EFFECT_TICK
                )
                vibrator.vibrate(vibrationEffect)
            } else {

                vibrator?.vibrate(40)
            }

            viewBinding.brightnessSb.visibility = View.VISIBLE
        }

    }


    private fun deleImg() {
        viewBinding.btnPost.isEnabled = true
        viewBinding.imageViewCaptured.setImageDrawable(null)
        viewBinding.edt1.text.clear()
        viewBinding.imageViewCaptured.visibility = View.GONE
        viewBinding.viewFinder.visibility = View.VISIBLE
        viewBinding.btnPost.visibility = View.GONE
        viewBinding.edt1.visibility = View.GONE
        viewBinding.fncLauout.visibility = View.VISIBLE
        viewBinding.buttonCapture.visibility = View.VISIBLE
        viewBinding.btnLeft.visibility = View.GONE
        viewBinding.btnGenativeAI.visibility = View.GONE
        viewBinding.progressBar.visibility = View.GONE
        viewBinding.brightnessSb.visibility = View.VISIBLE
        viewBinding.btnChoose.visibility = View.VISIBLE
        viewBinding.buttonSwitchCamera.visibility = View.VISIBLE
        postViewModel.clearContentgena()
    }

    private fun zoom() {
        if (iszoom == true) {
            cameraControl.setZoomRatio(1.0f)
            viewBinding.Btnnratio1x.setImageResource(R.drawable.ic_zoom)
            iszoom = false
        } else {
            cameraControl.setZoomRatio(2.0f)
            viewBinding.Btnnratio1x.setImageResource(R.drawable.ic_nonzoom)
            iszoom = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun tabtofocus() {
        viewBinding.viewFinder.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val factory = viewBinding.viewFinder.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point).build()
                cameraControl.startFocusAndMetering(action)


                val focusCircle = viewBinding.focusCircle
                focusCircle.x = event.x - focusCircle.width / 2
                focusCircle.y = event.y
                focusCircle.visibility = View.VISIBLE

                Handler(Looper.getMainLooper()).postDelayed({
                    focusCircle.visibility = View.GONE
                }, 1000)
            }
            true
        }
    }

    private fun flash() {
        if (cameraProvider == null) {
            Log.e(TAG, "cameraprovider chưa được khởi tạo. Không thể bật flash")
            return
        }
        currentFlashMode = when (currentFlashMode) {
            ImageCapture.FLASH_MODE_ON -> {
                viewBinding.buttonFlash.setImageResource(R.drawable.ic_flashoff)
                ImageCapture.FLASH_MODE_OFF
            }

            ImageCapture.FLASH_MODE_OFF -> {
                viewBinding.buttonFlash.setImageResource(R.drawable.ic_flashon)
                ImageCapture.FLASH_MODE_ON
            }

            else -> throw IllegalStateException("Unexpected")
        }
        imageCapture?.setFlashMode(currentFlashMode)
    }

    private fun requestCameraPermission() {
        val dialog =
            androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        dialog.setTitle("Bật quyền truy cập Máy ảnh")
            .setMessage("Đến cài đặt ứng dụng cấp quyền để ứng dụng được hoạt động đúng đắn!")
            .setPositiveButton("ĐẾN CÀI ĐẶT") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("HỦY") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun takePhoto() {
        if (!allPermissionsGranted()) {
            requestCameraPermission()
        }
        val imageCapture = imageCapture ?: return
        val metadata = createImageMetadata()
        val outputOptions = createOutputOptions(metadata)
        Log.d("TAG", "$outputOptions")
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext().applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    handleImageCaptureError(exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    handleImageSaved(output)
                }
            }
        )
    }

    private fun createImageMetadata(): ImageCapture.Metadata {
        return ImageCapture.Metadata().apply {
            isReversedHorizontal = isFrontCamera()
        }
    }

    private fun createOutputOptions(metadata: ImageCapture.Metadata): ImageCapture.OutputFileOptions {
        val name = UUID.randomUUID().toString() + ".jpg"
        val file = File(requireActivity().externalCacheDir, name)
        return ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()
    }

    private fun handleImageCaptureError(exc: ImageCaptureException) {
        Log.e("TAGY", "chup fail: ${exc.message}", exc)
    }

    private fun handleImageSaved(output: ImageCapture.OutputFileResults) {
        val savedUri = output.savedUri ?: return
        showCapturedImage(savedUri)
        setupPostControls(savedUri)
    }

    private fun showCapturedImage(savedUri: Uri) {
        viewBinding.imageViewCaptured.apply {
            visibility = View.VISIBLE
            viewBinding.viewFinder.visibility = View.INVISIBLE
            setImageURI(savedUri)
        }
        cameraProvider.unbindAll()
    }

    private fun setupPostControls(savedUri: Uri) {
        viewBinding.apply {
            btnLeft.visibility = View.VISIBLE
            edt1.visibility = View.VISIBLE
            fncLauout.visibility = View.INVISIBLE
            btnPost.visibility = View.VISIBLE
            buttonCapture.visibility = View.GONE
            btnGenativeAI.visibility = View.VISIBLE
            brightnessSb.visibility = View.GONE
            btnChoose.visibility = View.GONE
            buttonSwitchCamera.visibility = View.GONE
        }

        viewBinding.btnPost.setOnClickListener {
            handlePostButtonClick(savedUri)
        }

        viewBinding.btnPost.setOnLongClickListener {
            handlePostButtonLongClick(savedUri)
        }

        setupGenerativeAI(savedUri)
    }

    private fun handlePostButtonClick(savedUri: Uri) {
        viewBinding.btnPost.isEnabled = false
        viewBinding.btnLeft.visibility = View.INVISIBLE
        viewBinding.btnGenativeAI.visibility = View.INVISIBLE
        viewBinding.btnPost.visibility = View.GONE
        viewBinding.progressBar.visibility = View.VISIBLE
        val content = viewBinding.edt1.text.toString()
        postViewModel.addPost(savedUri, content, true)
    }

    private fun handlePostButtonLongClick(savedUri: Uri): Boolean {
        val vibrator = requireContext().getSystemService(Vibrator::class.java)
        vibrator?.vibrate(50)

        val content = viewBinding.edt1.text.toString()
        val bottomSheet = FriendListprivateBottomSheet(savedUri, null, content)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
        return true
    }

    private fun setupGenerativeAI(savedUri: Uri) {
        val imageBitmap = MediaStore.Images.Media.getBitmap(
            requireContext().contentResolver,
            savedUri
        )
        viewBinding.btnGenativeAI.setOnClickListener {
            val dialog = PromptDialog(imageBitmap)
            dialog.show(childFragmentManager, "prompt_dialog")

            postViewModel.contentgena.observe(viewLifecycleOwner) { content ->
                viewBinding.edt1.setText(content ?: "")
            }
        }
    }


    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            showCapturedImage(uri)
            setupPostControls(uri)
        } else {
            Log.e("GalleryError", "Không nhận được hình ảnh từ trình chọn")
        }
    }

    private fun choosePhotoFromGallery() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun isFrontCamera(): Boolean {
        return cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
    }

    private fun swipcam() {
        if (cameraProvider == null) {
            Log.e(TAG, "cameraprovider chưa được khởi tạo. Không thể xoay cam")
            return
        }

        cameraSelector = when (cameraSelector) {
            CameraSelector.DEFAULT_BACK_CAMERA -> {
                viewBinding.buttonSwitchCamera.animate().rotationBy(-180f).start()
                CameraSelector.DEFAULT_FRONT_CAMERA

            }

            CameraSelector.DEFAULT_FRONT_CAMERA -> {
                viewBinding.buttonSwitchCamera.animate().rotationBy(180f).start()
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            else -> throw IllegalStateException("Unexpected")
        }

        try {
            cameraProvider.unbindAll()

            bindCameraUseCases()

        } catch (exc: Exception) {
            Log.e(TAG, "xoay loi", exc)
        }
    }

    private fun brightnessSlider() {

        var currentExposureIndex = 0
        var minExposureIndex = 0
        var maxExposureIndex = 0


        cameraInfo?.exposureState?.let {
            minExposureIndex = it.exposureCompensationRange.lower
            maxExposureIndex = it.exposureCompensationRange.upper
            currentExposureIndex = it.exposureCompensationIndex
        }

        viewBinding.brightnessSb.apply {
            max = maxExposureIndex - minExposureIndex
            progress = 0
            progress = currentExposureIndex - minExposureIndex
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {

                        hideSeekBarHandler.removeCallbacks(hideSeekBarRunnable)
                        hideSeekBarHandler.postDelayed(hideSeekBarRunnable, 2000)

                        val newExposureIndex = progress + minExposureIndex
                        cameraControl.setExposureCompensationIndex(newExposureIndex).addListener({
                            currentExposureIndex = newExposureIndex
                        }, ContextCompat.getMainExecutor(requireContext()))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    hideSeekBarHandler.removeCallbacks(hideSeekBarRunnable)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    hideSeekBarHandler.postDelayed(hideSeekBarRunnable, 2000)
                }
            })
        }
    }


    fun startCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext().applicationContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                bindCameraUseCases()

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext().applicationContext))
    }

    private fun bindCameraUseCases() {

        val targetResolution = Size(1080, 1080)
        val preview = Preview.Builder()
            .setTargetResolution(Size(720, 720))
            .build()
            .also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

        // Khởi tạo ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetResolution(targetResolution)
            .setFlashMode(currentFlashMode)
            .setJpegQuality(100)
            .build()


        val extensionsManagerFuture =
            ExtensionsManager.getInstanceAsync(requireContext(), cameraProvider)
        extensionsManagerFuture.addListener(
            {
                val extensionsManager = extensionsManagerFuture.get()
                val cameraSelectorToUse = when {
                    extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.AUTO) -> {
                        extensionsManager.getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.AUTO

                        )
                    }

                    extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.HDR) -> {
                        extensionsManager.getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.HDR
                        )


                    }

                    extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.BOKEH) -> {
                        extensionsManager.getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.BOKEH
                        )
                    }

                    extensionsManager.isExtensionAvailable(
                        cameraSelector,
                        ExtensionMode.FACE_RETOUCH
                    ) -> {
                        extensionsManager.getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.FACE_RETOUCH
                        )
                    }

                    extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.NIGHT) -> {
                        extensionsManager.getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.NIGHT
                        )
                    }

                    else -> cameraSelector
                }




                try {
                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelectorToUse,
                        preview,
                        imageCapture
                    )

                    Log.d("CameraCheck", "HDR enabled: ${(ExtensionMode.HDR)}")
                    Log.d("CameraCheck", "Bokeh enabled: ${(ExtensionMode.BOKEH)}")
                    Log.d("CameraCheck", "Face Retouch enabled: ${(ExtensionMode.FACE_RETOUCH)}")
                    Log.d("CameraCheck", "Night enabled: ${(ExtensionMode.NIGHT)}")

                    cameraControl = camera.cameraControl
                    cameraInfo = camera.cameraInfo
                    brightnessSlider()
                } catch (e: Exception) {
                    Log.e("CameraCheckloi", "Camera không khả dụng ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        "Camera không khả dụng, Vui lòng thử đổi camera trước",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }, ContextCompat.getMainExecutor(requireContext())
        )


    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cameraProvider.unbindAll()


    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        cameraProvider.unbindAll()
    }

    fun getCameraProvider(): ProcessCameraProvider? {
        return cameraProvider
    }


    companion object {

        const val GALLERY_REQUEST_CODE = 1234
        private const val TAG = "CameraXApp"

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun getRequiredPermissions(): Array<String> {
            return REQUIRED_PERMISSIONS
        }
    }

}