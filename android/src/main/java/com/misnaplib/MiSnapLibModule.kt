package com.misnaplib

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
import java.util.concurrent.Callable

interface MainActivityResult {
  fun registerForActivityResult(activityResult: Callable<Void>)
  fun setupLocale(language: String, applied: () -> Unit)
}

class MiSnapLibModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var frontCameraSupportResult: CameraUtil.CameraSupportResult.Success? = null
  private var backCameraSupportResult: CameraUtil.CameraSupportResult.Success? = null

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun setLanguage(language: String) {
    val currentActivityListener = (currentActivity as MainActivityResult)
    currentActivityListener.setupLocale(language) {}
  }

  @ReactMethod
  fun openCamera(type: String, license: String, language: String, promise: Promise) {
    println("MYSNAP Parameters: type:$type - license:$license - language: $language")

    val currentActivityListener = (currentActivity as MainActivityResult)
    currentActivityListener.setupLocale(language) {
      var useCase = MiSnapSettings.UseCase.CHECK_FRONT
      if (type == "back")
        useCase = MiSnapSettings.UseCase.CHECK_BACK

      val settings = MiSnapSettings(
        useCase = useCase,
        license = license
      )

      settings.camera.apply {
        profile = MiSnapSettings.Camera.Profile.DOCUMENT_BACK_CAMERA
        videoRecord.recordSession = false
      }

      startMiSnapSession(
        MiSnapWorkflowStep(settings),
        promise
      )
    }
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
    println("MYSNAP Launching activity ")
    super.getCurrentActivity()?.startActivityForResult(intent, 123)
    println("MYSNAP launched activity ")

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
