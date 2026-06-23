package com.oneplusdream.flutter_aws_chime

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** FlutterAwsChimePlugin */
class FlutterAwsChimePlugin : FlutterPlugin, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var methodChannel: MethodChannelCoordinator
    private lateinit var binaryMessenger: BinaryMessenger
    private var activityBinding: ActivityPluginBinding? = null

    // Delivers the system permission-dialog result back to PermissionManager.
    // Without this, manageAudioPermissions/manageVideoPermissions request a
    // permission and stash the Flutter Result, but nothing ever resolves it —
    // so requestVideoPermissions() never completes and join() hangs before the
    // meeting starts.
    private val permissionResultListener =
            PluginRegistry.RequestPermissionsResultListener { requestCode, _, _ ->
                val manager = methodChannel.permissionsManager
                when (requestCode) {
                    manager.AUDIO_PERMISSION_REQUEST_CODE -> {
                        manager.audioCallbackReceived(); true
                    }
                    manager.VIDEO_PERMISSION_REQUEST_CODE -> {
                        manager.videoCallbackReceived(); true
                    }
                    else -> false
                }
            }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        flutterPluginBinding.platformViewRegistry.registerViewFactory("videoTile", FlutterVideoTileFactory())
        binaryMessenger = flutterPluginBinding.binaryMessenger;

    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {


    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        methodChannel = MethodChannelCoordinator(binaryMessenger, binding.activity);
        methodChannel?.setupMethodChannel()
        binding.addRequestPermissionsResultListener(permissionResultListener)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding?.removeRequestPermissionsResultListener(permissionResultListener)
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activityBinding?.removeRequestPermissionsResultListener(permissionResultListener)
        activityBinding = null
    }


}
