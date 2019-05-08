package com.roam2free.fludcs.handler

import android.app.Activity
import android.media.AudioManager
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.baidu.duer.dcs.api.*
import com.baidu.duer.dcs.api.config.DcsConfig
import com.baidu.duer.dcs.api.config.DefaultSdkConfigProvider
import com.baidu.duer.dcs.api.recorder.AudioRecordImpl
import com.baidu.duer.dcs.api.wakeup.BaseWakeup
import com.baidu.duer.dcs.api.wakeup.IWakeupProvider
import com.baidu.duer.dcs.devicemodule.custominteraction.CustomUserInteractionDeviceModule
import com.baidu.duer.dcs.devicemodule.custominteraction.message.ClickLinkPayload
import com.baidu.duer.dcs.devicemodule.custominteraction.message.HandleUnknownUtterancePayload
import com.baidu.duer.dcs.framework.DcsSdkImpl
import com.baidu.duer.dcs.framework.ILoginListener
import com.baidu.duer.dcs.framework.InternalApi
import com.baidu.duer.dcs.framework.internalapi.IDirectiveReceivedListener
import com.baidu.duer.dcs.framework.internalapi.IErrorListener
import com.baidu.duer.dcs.oauth.api.code.OauthCodeImpl
import com.baidu.duer.dcs.tts.TtsImpl
import com.baidu.duer.dcs.util.DcsErrorCode
import com.baidu.duer.dcs.util.api.IDcsRequestBodySentListener
import com.baidu.duer.dcs.util.util.StandbyDeviceIdUtil
import com.baidu.duer.kitt.KittWakeUpServiceImpl
import com.baidu.duer.kitt.WakeUpConfig
import com.baidu.duer.kitt.WakeUpWord
import com.baidu.turbonet.net.RequestPriority.IDLE
import com.roam2free.fludcs.BuildConfig
import com.roam2free.fludcs.devicemodule.asr.AsrDeviceModule
import com.roam2free.fludcs.devicemodule.devicecontrol.DeviceControlDeviceModule
import io.flutter.plugin.common.MethodChannel
import java.util.ArrayList

internal class FludcsAuthHandler(private val methodChannel: MethodChannel, private val activity: Activity) {

    companion object {
        const val TAG = "Fludcs"
        const val CLIENT_ID = "sgYXkNGVXtyo2NrrPSBW6GkUtUuKnGYy"
        lateinit var dcsSdk: IDcsSdk
        lateinit var internalApi: InternalApi
        var currentDialogState = IDialogStateListener.DialogState.IDLE
    }

    private val dcsAuthListener by lazy {
        object : ILoginListener {
            override fun onSucceed(accessToken: String?) {
                dcsSdk.run {}
                methodChannel.invokeMethod("onAuthSucceed", mapOf(
                        "accessToken" to accessToken
                ))
            }

            override fun onFailed(errorMessage: String?) {
                methodChannel.invokeMethod("onAuthFailed", mapOf(
                        "errorMessage" to errorMessage
                ))
            }

            override fun onCancel() {
                methodChannel.invokeMethod("onAuthCancel", null)
            }

        }
    }

    private val connectionStatusListener by lazy {
        IConnectionStatusListener {
            Log.d(TAG, "onConnectionStatusChange :$it")
        }
    }

    private val errorListener by lazy {
        IErrorListener {
            Log.e(TAG,"dcs on error : $it")
            when (it.error) {
                DcsErrorCode.VOICE_REQUEST_EXCEPTION -> {
                    if (it.subError == DcsErrorCode.NETWORK_UNAVAILABLE)
                        Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(activity, "识别失败，请稍后再试", Toast.LENGTH_SHORT).show()
                }
                DcsErrorCode.LOGIN_FAILED -> {
                    Toast.makeText(activity, "未登录", Toast.LENGTH_SHORT).show()
                }
                DcsErrorCode.UNAUTHORIZED_REQUEST -> {

                }

            }
        }

    }

    private val dcsRequestBodySentListener by lazy {
        IDcsRequestBodySentListener {
            val eventName = it.event.header.name
            Log.d(TAG, "event name : $eventName")

        }
    }

    private val dialogStateListener by lazy {
        IDialogStateListener {
            Log.d(TAG, "onDialogStateChanged : $it")
            currentDialogState = it
            methodChannel.invokeMethod("onDialogStateChanged",it.name)
        }
    }

    //DeviceModule Listener
    private val userInteractionDirectiveListener by lazy {
        object : CustomUserInteractionDeviceModule.ICustomUserInteractionListener {
            override fun onHandleUnknownUtterance(p0: HandleUnknownUtterancePayload?) {

            }

            override fun onClickLink(p0: ClickLinkPayload?) {

            }

        }
    }

    //    {"directive":{"header":{"name":"RenderVoiceInputText","namespace":"ai.dueros.device_interface.screen","messageId":"1556078485_4105ktfcr","dialogRequestId":"cc5698dc-2422-4d67-acb1-567ee36e156a"},
//    "payload":{"text":"小度小度","type":"FINAL"}}}
    private val directiveReceivedListener by lazy {
        IDirectiveReceivedListener {
            methodChannel.invokeMethod("onDirective", it.jsonObjectDirective.toString())
        }
    }

    private val handler by lazy {
        Handler()
    }

    fun initDcs() {
        val audioRecorder = AudioRecordImpl()
        val oauth = OauthCodeImpl(CLIENT_ID, activity)
        val wakeup = KittWakeUpServiceImpl(audioRecorder)
        val builder = DcsSdkBuilder()
        val sdkConfigProvider = object : DefaultSdkConfigProvider() {
            override fun clientId(): String {
                // CLIENT_ID 是申请产品时的client_id
                return CLIENT_ID
            }

            override fun pid(): Int {
                // 语音PID
                return BuildConfig.PID
            }

            override fun appKey(): String {
                //语音key
                return BuildConfig.APP_KEY
            }

        }

        val wakeupProvider = object : IWakeupProvider {

            override fun audioType() = AudioManager.STREAM_MUSIC

            override fun wakeUpConfig(): WakeUpConfig {
                val wakeupWordList = ArrayList<WakeUpWord>()
                wakeupWordList.add(WakeUpWord(1, "小度小度"))
                wakeupWordList.add(WakeUpWord(2, "小度小度"))
                wakeupWordList.add(WakeUpWord(3, "小度小度"))
                val umdlPaths = ArrayList<String>()
                umdlPaths.add("snowboy/xiaoduxiaodu_all_11272017.umdl")
                return WakeUpConfig.Builder()
                        .resPath("snowboy/common.res")
                        .umdlPath(umdlPaths)
                        .sensitivity("0.35,0.35,0.40")
                        .highSensitivity("0.45,0.45,0.55")
                        .wakeUpWords(wakeupWordList)
                        .build()
            }

            override fun wakeAlways() = true

            override fun wakeupImpl() = wakeup

            override fun volume() = 0.8f

            override fun enableWarning() = true

            override fun warningSource() = "assets://ding.wav"

        }

        dcsSdk = builder
                .withSdkConfig(sdkConfigProvider)
                .withWakeupProvider(wakeupProvider)
                .withOauth(oauth)
                .withAudioRecorder(audioRecorder)
                //获取设备ID
                .withDeviceId(StandbyDeviceIdUtil.getStandbyDeviceId())
                .build()
        internalApi = (dcsSdk as DcsSdkImpl).internalApi
//        internalApi.setSupportOneshot(false)
        internalApi.initWakeUp()
        internalApi.setAsrMode(DcsConfig.ASR_MODE_ONLINE)


        //配置TTS
        handler.postDelayed({
            val impl = internalApi.initLocalTts(activity.applicationContext,null,null,
                    "sgYXkNGVXtyo2NrrPSBW6GkUtUuKnGYy","lU6cDxYKpYsiE3q6PVTV05WLGVruLeG3"
                    , "15781527",null
            )
            impl.setSpeaker(2)
            val textFile = activity.applicationInfo.nativeLibraryDir + "/libbd_etts_text.dat.so"
            val speechMode = activity.applicationInfo.nativeLibraryDir + "/" + TtsImpl.SPEECH_MODEL_NAME_GEZI
            impl.loadSpeechModel(speechMode, textFile)
            internalApi.setVolume(0.8f)
        },300)

        // 第二步：可以按需添加内置端能力和用户自定义端能力（需要继承BaseDeviceModule）
        val messageSender = internalApi.messageSender
        val dialogRequestIdHandler = (dcsSdk as DcsSdkImpl).provider.dialogRequestIdHandler
        val customUserInteractionDeviceModule = CustomUserInteractionDeviceModule(messageSender, dialogRequestIdHandler)
        customUserInteractionDeviceModule.addCustomUserInteractionDirectiveListener(userInteractionDirectiveListener)
        dcsSdk.putDeviceModule(customUserInteractionDeviceModule)
        addOtherDeviceModule(messageSender)
        //初始化监听器
        initListener()

    }

    private fun addOtherDeviceModule(messageSender: IMessageSender) {
        val asrDeviceModule = AsrDeviceModule(messageSender)
        asrDeviceModule.addAsrListener {
            methodChannel.invokeMethod("onHandleAsrResult", it.content)
        }
        dcsSdk.putDeviceModule(asrDeviceModule)
        val deviceControl = DeviceControlDeviceModule(messageSender)
        dcsSdk.putDeviceModule(deviceControl)
    }

    private fun initListener() {
        dcsSdk.addConnectionStatusListener(connectionStatusListener)
        dcsSdk.voiceRequest.addDialogStateListener(dialogStateListener)

        internalApi.addErrorListener(errorListener)
        internalApi.addRequestBodySentListener(dcsRequestBodySentListener)
        // 所有指令透传，建议在各自的DeviceModule中处理
        internalApi.addDirectiveReceivedListener(directiveReceivedListener)

    }


    fun dcsAuth() {
        internalApi.login(dcsAuthListener)
    }

    fun setAsrMode(asrMode: Int = DcsConfig.ASR_MODE_ONLINE) {
        internalApi.setAsrMode(asrMode)
    }

    /**
     * @param vad 是否开启语音尾点检测 true:开启 false:关闭
     */
    fun beginVoiceRequest() {
        if (currentDialogState == IDialogStateListener.DialogState.LISTENING) run { dcsSdk.voiceRequest.endVoiceRequest { } }
        else dcsSdk.voiceRequest.cancelVoiceRequest {
            dcsSdk.voiceRequest.beginVoiceRequest(true)
        }
    }

    fun speakOfflineRequest(text: String) {
        internalApi.speakOfflineRequest(text)
    }

    fun speakRequest(text: String){
        internalApi.speakRequest(text)
    }

    fun dispose(){
        internalApi.pauseSpeaker()
        // 如果有唤醒，则停止唤醒
        internalApi.stopWakeup(null)
        dcsSdk.voiceRequest.cancelVoiceRequest {
            Log.d(TAG,"cancel voice request success.")
        }
        handler.removeCallbacksAndMessages(null)
        dcsSdk.voiceRequest.removeDialogStateListener(dialogStateListener)
        dcsSdk.removeConnectionStatusListener(connectionStatusListener)
        internalApi.removeErrorListener(errorListener)
        internalApi.removeRequestBodySentListener(dcsRequestBodySentListener)
        dcsSdk.release()
    }

}