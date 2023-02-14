package com.misnaplib

import android.content.Intent
import android.util.Base64
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.miteksystems.misnap.camera.requireProfile
import com.miteksystems.misnap.camera.util.CameraUtil
import com.miteksystems.misnap.core.MiSnapSettings
import com.miteksystems.misnap.workflow.MiSnapFinalResult
import com.miteksystems.misnap.workflow.MiSnapWorkflowActivity
import com.miteksystems.misnap.workflow.MiSnapWorkflowError
import com.miteksystems.misnap.workflow.MiSnapWorkflowStep
import com.miteksystems.misnap.workflow.fragment.DocumentAnalysisFragment
import java.util.concurrent.Callable

interface MainActivityResult {
  fun registerForActivityResult(activityResult: Callable<Void>)
}

class MiSnapLibModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var frontCameraSupportResult: CameraUtil.CameraSupportResult.Success? = null
  private var backCameraSupportResult: CameraUtil.CameraSupportResult.Success? = null

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun openCamera(type: String, license: String, promise: Promise) {
    val array = arrayOf("b54Img","fileURI")

    var useCase = MiSnapSettings.UseCase.CHECK_FRONT

    if (type == "back")
      useCase = MiSnapSettings.UseCase.CHECK_BACK

    val settings = MiSnapSettings(
      useCase = useCase,
      license = license
    )

    val defaultWorkflowSettings = DocumentAnalysisFragment.getDefaultWorkflowSettings(settings)
    val workflowSettings = DocumentAnalysisFragment.buildWorkflowSettings(
      reviewCondition = DocumentAnalysisFragment.ReviewCondition.WARNINGS,
      misnapViewShouldShowBoundingBox = false,
      misnapViewShouldShowGlareBox = false,
      // "CameraInitialTimeoutInSeconds": 30,
      timeoutDuration = 30000,
      // "SmartBubbleAppearanceDelay": 3000,
      hintDuration = 3000,
      guideViewAlignedScalePercentage = defaultWorkflowSettings.guideViewUnalignedScalePercentage,
      guideViewUnalignedScalePercentage = defaultWorkflowSettings.guideViewUnalignedScalePercentage,
      // "CameraGuideImageStillCameraAlpha": 50,
      // "CameraGuideImageStillCameraEnabled": 1
      guideViewShowVignette = true,
      hintViewShouldShowBackground = defaultWorkflowSettings.hintViewShouldShowBackground,
      successViewShouldVibrate = defaultWorkflowSettings.successViewShouldVibrate,
    )

    settings.workflow.add(
      "Document Analysis Screen",
      workflowSettings
    )

    settings.analysis.apply {
      // "CameraVideoAutoCaptureProcess": 1,
      document.trigger = MiSnapSettings.Analysis.Document.Trigger.AUTO
      document.enableEnhancedManual = false
      document.orientation = MiSnapSettings.Analysis.Document.Orientation.LANDSCAPE // landscape
      document.documentExtractionRequirement = MiSnapSettings.Analysis.Document.ExtractionRequirement.OPTIONAL
      document.barcodeExtractionRequirement = MiSnapSettings.Analysis.Document.ExtractionRequirement.OPTIONAL
      document.check.geo = MiSnapSettings.Analysis.Document.Check.Geo.GLOBAL
      document.redactOptionalData = false
      document.advanced.cornerConfidence = 600
      document.advanced.minPadding = 7
      // "CameraViewfinderMinHorizontalFill": 800,
      document.advanced.minHorizontalFillUnaligned = 800
      // "CameraBrightness": 400,
      document.advanced.minBrightness = 400
      document.advanced.maxBrightness = 820
      document.advanced.minContrast = 600
      // "CameraViewfinderMinHorizontalFill": 800,
      document.advanced.minHorizontalFillAligned = 800
      document.advanced.minBusyBackground = 750
      // "CameraDegreesThreshold": 150,
      document.advanced.maxAngle = 150
      // "CameraSharpness": 300,
      document.advanced.minSharpness = 300
      document.advanced.minNoGlare = 0
      document.advanced.mrzConfidence = 800
    }

    settings.camera.apply {
      profile = MiSnapSettings.Camera.Profile.DOCUMENT_BACK_CAMERA
      // "LightingVideo": 1,
      // "LightingStillCamera": 1,
      torchMode = MiSnapSettings.Camera.TorchMode.AUTO // auto flash

      // "AllowVideoFrames": 0,
      videoRecord.recordSession = false
    }

    startMiSnapSession(
      MiSnapWorkflowStep(settings),
      promise
    )

    // promise.resolve(array)
  }

  private fun startMiSnapSession(
    misnapWorkflowStep: MiSnapWorkflowStep,
    promise: Promise
  ) {
    mutableListOf(misnapWorkflowStep).forEach {
      if (!hasCameraSettings(it.settings)) {
        when (it.settings.camera.requireProfile()) {
          MiSnapSettings.Camera.Profile.DOCUMENT_BACK_CAMERA -> {
            backCameraSupportResult?.let { cameraSupportResult ->
              applyCameraSettings(cameraSupportResult, it.settings)
            }
          }
          else -> {
            frontCameraSupportResult?.let { cameraSupportResult ->
              applyCameraSettings(cameraSupportResult, it.settings)
            }
          }
        }
      }
    }

    val intent = MiSnapWorkflowActivity.buildIntent(
      reactApplicationContext.applicationContext,
      misnapWorkflowStep,
      disableScreenshots = false // allow screenshots only because this is used for demos
    )

    val currentActivityListener = (currentActivity as MainActivityResult)

    currentActivityListener.registerForActivityResult {
      MiSnapWorkflowActivity.Result.results.forEachIndexed { index, stepResult ->
        when (stepResult) {
          is MiSnapWorkflowStep.Result.Success -> {
            if (stepResult.result is MiSnapFinalResult.DocumentSession) {
              val image = (stepResult.result as MiSnapFinalResult.DocumentSession).jpegImage

              val base64Image = Base64.encodeToString(image,Base64.DEFAULT)

              promise.resolve(base64Image)
            }
          }
          is MiSnapWorkflowStep.Result.Error -> {
            stepResult.errorResult.error
            when (val errorResult = stepResult.errorResult.error) {
              is MiSnapWorkflowError.Permission -> {
                promise.reject("Permission", errorResult.toString())
              }
              is MiSnapWorkflowError.Camera -> {
                promise.reject("Camera", errorResult.toString())

              }
              is MiSnapWorkflowError.Cancelled -> {
                promise.reject("Cancelled", errorResult.toString())

              }
              else -> {
                promise.reject("Other", errorResult.toString())

              }
            }
          }
        }
      }

      MiSnapWorkflowActivity.Result.clearResults()
      null
    }

    super.getCurrentActivity()?.startActivityForResult(intent, 123)
  }

  private fun hasCameraSettings(settings: MiSnapSettings) =
    when (settings.useCase) {
      MiSnapSettings.UseCase.FACE -> {
        settings.analysis.face.trigger != null
      }
      MiSnapSettings.UseCase.BARCODE -> {
        settings.analysis.barcode.trigger != null
      }
      else -> {
        settings.analysis.document.trigger != null
      }
    }

  /**
   * Mutates the [settings] by applying the corresponding camera parameters.
   */
  private fun applyCameraSettings(
    cameraSupportResult: CameraUtil.CameraSupportResult.Success,
    settings: MiSnapSettings
  ) {
    if (cameraSupportResult.cameraInfo.supportsAutoAnalysis) {
      settings.analysis.document.trigger = MiSnapSettings.Analysis.Document.Trigger.AUTO
    } else {
      // This camera does not support auto, set the trigger to manual
      settings.analysis.document.trigger =
        MiSnapSettings.Analysis.Document.Trigger.MANUAL
    }
  }

  companion object {
    const val NAME = "MiSnapLib"
  }
}
