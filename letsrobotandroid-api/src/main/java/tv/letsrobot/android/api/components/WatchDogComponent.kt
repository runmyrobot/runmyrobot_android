package tv.letsrobot.android.api.components

import android.content.Context
import android.os.Handler
import android.os.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.JsonObjectUtils
import tv.letsrobot.android.api.utils.RobotConfig

/**
 * Watchdog service for LetsRobot. Contains main app socket, and will send updates on camera status
 * and IP
 */
class WatchDogComponent(val serviceHandler: Handler, context: Context) : Component(context) {
    var owner : String? = null
    var robotId = RobotConfig.RobotId.getValue(context) as String

    override fun enableInternal() {
        setOwner()
        setupAppWebSocket()
    }

    override fun disableInternal() {

    }

    private var appServerSocket: Socket? = null

    private fun setOwner(){
        owner = JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)
        )?.let {
            it.getString("owner")
        }
    }

    private fun setupAppWebSocket() {
        appServerSocket = IO.socket("http://letsrobot.tv:8022")
        appServerSocket?.connect()
        appServerSocket?.on(Socket.EVENT_CONNECT_ERROR){
            EventManager.invoke(javaClass.simpleName, ComponentStatus.ERROR)
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            EventManager.invoke(javaClass.simpleName, ComponentStatus.STABLE)
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        appServerSocket?.on(Socket.EVENT_DISCONNECT){
            EventManager.invoke(javaClass.simpleName, ComponentStatus.DISABLED)
        }
    }

    private var count = 0
    private val onUpdateServer: () -> Unit = {
        appServerSocket?.emit("identify_robot_id", robotId)
        if(count % 60 == 0){
            camera?.let {
                val obj = JSONObject()
                obj.put("send_video_process_exists",true)
                obj.put("ffmpeg_process_exists", getCameraRunning())
                obj.put("camera_id",cameraId)
                appServerSocket?.emit("send_video_status", obj)
            }
            val ipInfo = JSONObject()
            ipInfo.put("ip", "169.254.25.110") //TODO actually use a different ip...
            ipInfo.put("robot_id", robotId)
            appServerSocket?.emit("ip_information", ipInfo)
        }
        handler.sendEmptyMessage(DO_SOME_WORK)
        count++
    }

    override fun handleMessage(it: Message?): Boolean {
        return when(it?.what){
            DO_SOME_WORK -> {
                onUpdateServer()
                true
            }
            else ->{
                /*return*/super.handleMessage(it)
            }
        }
    }
}