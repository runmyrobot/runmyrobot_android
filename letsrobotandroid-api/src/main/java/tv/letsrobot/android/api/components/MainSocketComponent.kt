package tv.letsrobot.android.api.components

import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.ComponentEventObject
import tv.letsrobot.android.api.utils.JsonObjectUtils
import tv.letsrobot.android.api.utils.RobotConfig
import java.util.concurrent.TimeUnit

/**
 * App Socket for LetsRobot. Will send updates on camera status
 * and IP, and publishes other useful information
 */
class MainSocketComponent(context: Context) : Component(context) {
    var robotId = RobotConfig.RobotId.getValue(context) as String
    private var cameraStatus: ComponentStatus? = null

    override fun getType(): ComponentType {
        return ComponentType.APP_SOCKET
    }

    override fun enableInternal() {
        setOwner()
        setupAppWebSocket()
        handler.sendEmptyMessage(DO_SOME_WORK)
    }

    override fun disableInternal() {
        appServerSocket?.disconnect()
        userAppSocket?.disconnect()
    }

    private var appServerSocket: Socket? = null
    private var userAppSocket: Socket? = null

    private fun setOwner(){
        owner = JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)
        )?.let {
            it.getString("owner")
        }
        //maybe this should rest somewhere else for lookup
        eventDispatcher?.handleMessage(getType(), ROBOT_OWNER, owner, this)
    }

    private fun setupAppWebSocket() {
        userAppSocket = IO.socket("http://letsrobot.tv:8000")
        appServerSocket = IO.socket("http://letsrobot.tv:8022")
        appServerSocket?.on(Socket.EVENT_CONNECT_ERROR){
            status = ComponentStatus.ERROR
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            status = ComponentStatus.STABLE
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        appServerSocket?.on(Socket.EVENT_DISCONNECT){
            status = ComponentStatus.DISABLED
        }
        userAppSocket?.on("message_removed"){
            onMessageRemoved(it)
        }?.on("channel_users_list"){
            Log.d("TEST","test")
        }?.on(Socket.EVENT_CONNECT){
            Log.d("TEST","test")
        }?.on(Socket.EVENT_PONG){
            Log.d("TEST","test")
        }
        appServerSocket?.connect()
        userAppSocket?.connect()
    }

    /**
     * {
    "message_id": "5c5e45cf4101e7503a4eb148"
    }
     */
    private fun onMessageRemoved(params: Array<out Any>){
        if (params[0] is JSONObject) {
            val `object` = params[0] as JSONObject
            try {
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent(ChatSocketComponent.LR_CHAT_MESSAGE_REMOVED_BROADCAST)
                                .also { intent ->
                                    intent.fillWithJson(`object`)
                                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun maybeSendVideoStatus() {
        cameraStatus?.let { //don't bother if we have not received camera status
            val obj = JSONObject()
            obj.put("send_video_process_exists",true)
            obj.put("ffmpeg_process_exists", it == ComponentStatus.STABLE)
            obj.put("camera_id", RobotConfig.CameraId.getValue(context) as String)
            appServerSocket?.emit("send_video_status", obj)
        }
    }

    /**
     * Update server properties every minute
     */
    private fun onUpdateServer() {
        appServerSocket?.emit("identify_robot_id", robotId)
        maybeSendVideoStatus()
        handler.sendEmptyMessageDelayed(DO_SOME_WORK, TimeUnit.MINUTES.toMillis(1))
    }

    override fun handleMessage(message: Message): Boolean {
        return when(message.what){
            DO_SOME_WORK -> {
                onUpdateServer()
                true
            }
            else ->{
                /*return*/super.handleMessage(message)
            }
        }
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CAMERA && message.what == Component.STATUS_EVENT)
            cameraStatus = message.data as ComponentStatus
        return super.handleExternalMessage(message)
    }

    companion object {
        const val ROBOT_OWNER = 0
        var owner : String? = null
    }

    /**
     * Fill the intent with top level of the json. Does not work when it is nested
     */
    private fun Intent.fillWithJson(jsonObject: JSONObject) {
        jsonObject.keys().forEach { key ->
            try {
                putExtra(key, jsonObject.getString(key))
            } catch (e: Exception) { //in case the API changes
            }
        }
    }
}