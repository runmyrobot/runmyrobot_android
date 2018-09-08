package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.runmyrobot.android_robot_for_phone.utils.RecordingThread
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Brendon on 9/1/2018.
 */
class AudioComponent(private val contextA: Context, val cameraId : String) : Component(contextA), FFmpegExecuteResponseHandler, RecordingThread.AudioDataReceivedListener {

    /*audioCommandLine1 = '%s -f mp3 -ar 44100 -ac %d -i pipe: -f mpegts -codec:a mp2 -b:a 32k -muxdelay 0.001 http://%s:%s/%s/640/480/' % (ffmpegLocation, robotSettings.mic_channels, audioDevNum, audioHost, audioPort, robotSettings.stream_key)
    */
    //cat file.mp3 | ffmpeg -f mp3 -i pipe: -c:a pcm_s16le -f s16le pipe:
    internal var ffmpegRunning = AtomicBoolean(false)

    val fFmpeg = FFmpeg.getInstance(context)

    private var process: Process? = null

    var UUID = java.util.UUID.randomUUID().toString()
    private var port: String? = null

    private var host: String? = null

    override fun enable() {
        super.enable()
        try {
            val client = OkHttpClient.Builder()
                    .build()
            var call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_audio_port/%s", cameraId)).build())
            var response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                port = `object`.getString("audio_stream_port")
            }
            call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_websocket_relay_host/%s", cameraId)).build())
            response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                host = `object`.getString("host")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if(host == null || port == null){
            throw Exception("Unable to form URL")
        }
        val recordingThread = RecordingThread(this)
        recordingThread.startRecording()
    }

    override fun disable() {
        super.disable()
    }


    override fun onStart() {
        ffmpegRunning.set(true)
        Log.d(AudioComponent.LOGTAG, "onStart")
    }

    fun ShortToByte_ByteBuffer_Method(input: ShortArray): ByteArray {
        var index: Int
        val iterations = input.size

        val bb = ByteBuffer.allocate(input.size * 2)

        index = 0
        while (index != iterations) {
            bb.putShort(input[index])
            ++index
        }

        return bb.array()
    }

    override fun onAudioDataReceived(data: ShortArray?) {
        val audioDevNum = 1
        val mic_channels = 1
        val audioHost = host
        val audioPort = port
        val stream_key = com.runmyrobot.android_robot_for_phone.BuildConfig.CAMERA_PASS
        val audioCommandLine2 = String.format("-f s16be -i - -f mpegts -codec:a mp2 -b:a 32k -ar 44100 -muxdelay 0.001 http://%s:%s/%s/640/480/", audioHost, audioPort, stream_key)
        try {
            if(!ffmpegRunning.get())
                fFmpeg.execute(UUID, null, audioCommandLine2.split(" ").toTypedArray(), this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        data?.let {
            try {
                process?.outputStream?.write(ShortToByte_ByteBuffer_Method(it))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onProgress(message: String?) {
        Log.d(AudioComponent.LOGTAG, "onProgress : $message")
    }

    override fun onFailure(message: String?) {
        Log.e(AudioComponent.LOGTAG, "progress : $message")
    }

    override fun onSuccess(message: String?) {
        Log.d(AudioComponent.LOGTAG, "onSuccess : $message")
    }

    override fun onFinish() {
        Log.d(AudioComponent.LOGTAG, "onFinish")
        ffmpegRunning.set(false)
    }

    override fun onProcess(p0: Process?) {
        process = p0
        Log.d(AudioComponent.LOGTAG, "onProcess")
    }

    companion object {
        const val LOGTAG = "Audio"
    }
}