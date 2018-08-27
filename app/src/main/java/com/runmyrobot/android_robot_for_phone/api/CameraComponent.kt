package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.os.Environment
import android.util.Log

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.runmyrobot.android_robot_for_phone.RobotApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.CertificatePinner



/**
 * Class that contains only the camera components for streaming to letsrobot.tv
 *
 * Created by Brendon on 8/25/2018.
 */
class CameraComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
internal constructor(val context: Context, val cameraId: String) {
    internal var ffmpegWorking = AtomicBoolean(false)

    fun enable() {
        var port: String? = null
        var host: String? = null
        val ffmpeg = FFmpeg.getInstance(context)

        try {
            /*val certificatePinner = CertificatePinner.Builder()
                    .add("letsrobot.tv", "sha256/qVev9udC8GFYcbR1R/IfDYH59RzNscMoN0ethtDz2T0=")
                    .add("letsrobot.tv", "sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=")
                    .add("letsrobot.tv", "sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=")
                    .add("letsrobot.tv", "sha256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU=")
                    .build()*/
            val client = OkHttpClient.Builder()
                    //.certificatePinner(certificatePinner)
                    .build()
            var call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_video_port/%s", cameraId)).build())
            var response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                port = `object`.getString("mpeg_stream_port")
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
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val list = ArrayList<String>()
            val xres = "400"
            val yres = "400"

            val video_device_number = "1"

            val rotation_option = "" //leave blank
            val kbps = "20"
            val video_host = host
            val video_port = port
            val stream_key = RobotApplication.getCameraPass()///dev/video${video_device_number}
            
            val command = "-f v4l2 -threads 4 -video_size ${xres}x${yres} -i \"/sdcard/test.mp4\" -f mpegts -framerate 25 -codec:v mpeg1video -b:v ${kbps}k -bf 0 -muxdelay 0.001 http://${video_host}:${video_port}/${stream_key}/${xres}/${yres}/"
//Do not hardcode "/sdcard/"; use `Environment.getExternalStorageDirectory().getPath()` instead
            ffmpeg.execute(command.split(" ").toTypedArray(), object : ExecuteBinaryResponseHandler() {
                override fun onStart() {
                    Log.d("FFMpeg", "onStart")
                }

                override fun onProgress(message: String?) {
                    Log.d("FFMpeg", message)
                }

                override fun onFailure(message: String?) {
                    Log.d("FFMpeg", message)
                }

                override fun onSuccess(message: String?) {
                    Log.d("FFMpeg", message)
                }

                override fun onFinish() {}
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            // Handle if FFmpeg is already running
        }

    }

    fun disable() {

    }
}
