package com.roam2free.fludcs

import com.roam2free.fludcs.handler.FludcsAuthHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FludcsPlugin(registrar: Registrar, channel: MethodChannel) : MethodCallHandler {

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.roam2free/fludcs")
            channel.setMethodCallHandler(FludcsPlugin(registrar,channel))
        }

    }

    private val fludcsHandler = FludcsAuthHandler(channel,registrar.activity())

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "initDcs" -> fludcsHandler.initDcs()
            "dcsAuth" -> fludcsHandler.dcsAuth()
            "beginVoiceRequest" -> fludcsHandler.beginVoiceRequest()
            "speakOfflineRequest"->fludcsHandler.speakOfflineRequest(call.arguments as String)
            "speakRequest"->fludcsHandler.speakRequest(call.arguments as String)
            "dispose"->fludcsHandler.dispose()
            else -> result.notImplemented()
        }
    }


}
