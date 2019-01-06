package tv.letsrobot.android.api.components

import android.content.Context
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.JsonObjectUtils
import tv.letsrobot.android.api.utils.RecordingThread
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Brendon on 9/1/2018.
 */
class AudioComponent(contextA: Context, val cameraId : String, val cameraPass : String) : Component(contextA), FFmpegExecuteResponseHandler, RecordingThread.AudioDataReceivedListener {
    override fun getType(): ComponentType {
        return ComponentType.MICROPHONE
    }

    internal var ffmpegRunning = AtomicBoolean(false)

    val fFmpeg = FFmpeg.getInstance(context)

    private var process: Process? = null

    var UUID = java.util.UUID.randomUUID().toString()
    private var port: String? = null
    private var successCounter: Int = 0
    private var host: String? = null
    private val recordingThread = RecordingThread(this)

    override fun enableInternal(){
        port = JsonObjectUtils.getValueJsonObject(
            String.format("https://letsrobot.tv/get_audio_port/%s", cameraId),
                "audio_stream_port"
        )
        host = JsonObjectUtils.getValueJsonObject(
            String.format("https://letsrobot.tv/get_websocket_relay_host/%s", cameraId),
                "host"
        )

        if(host == null || port == null){
            status = ComponentStatus.ERROR
        }
        else
            recordingThread.startRecording()
    }

    override fun disableInternal(){
        recordingThread.stopRecording()
        process?.destroy()
        process = null
    }


    override fun onStart() {
        ffmpegRunning.set(true)
        Log.d(TAG, "onStart")
    }

    fun ShortToByte_ByteBuffer_Method(input: ShortArray): ByteArray {
        var index= 0
        val iterations = input.size

        val bb = ByteBuffer.allocate(input.size * 2)

        while (index != iterations) {
            bb.putShort(input[index])
            ++index
        }

        return bb.array()
    }

    override fun onAudioDataReceived(data: ShortArray?) {
        try {
            if(!ffmpegRunning.get()){
                successCounter = 0
                status = ComponentStatus.CONNECTING
                val audioCommandLine2 = String.format("-f s16be -i - -f mpegts -codec:a mp2 -b:a 32k -ar 44100 -muxdelay 0.001 http://%s:%s/%s/640/480/", host, port, cameraPass)
                fFmpeg.execute(UUID, null, audioCommandLine2.split(" ").toTypedArray(), this)
            }
        } catch (e: Exception) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
        }
        data?.let { d->
            try {
                val buffer = ShortToByte_ByteBuffer_Method(d)
                buffer.let { b -> process?.outputStream?.write(b) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onProgress(message: String?) {
        successCounter++
        status = when {
            successCounter > 5 -> ComponentStatus.STABLE
            successCounter > 2 -> ComponentStatus.INTERMITTENT
            else -> ComponentStatus.CONNECTING
        }
    }

    override fun onFailure(message: String?) {
        status = ComponentStatus.ERROR
        Log.e(TAG, "progress : $message")
    }

    override fun onSuccess(message: String?) {
        Log.d(TAG, "onSuccess : $message")
    }

    override fun onFinish() {
        Log.d(TAG, "onFinish")
        status = ComponentStatus.DISABLED
        ffmpegRunning.set(false)
    }

    override fun onProcess(p0: Process?) {
        process = p0
        Log.d(TAG, "onProcess")
    }

    companion object {
        const val TAG = "Audio"
    }
}