package com.runmyrobot.android_robot_for_phone.activities;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.common.util.concurrent.RateLimiter;
import com.runmyrobot.android_robot_for_phone.BuildConfig;
import com.runmyrobot.android_robot_for_phone.R;
import com.runmyrobot.android_robot_for_phone.RobotApplication;
import com.runmyrobot.android_robot_for_phone.Util;

public class MJPEGFFMPEGTest extends Activity implements OnClickListener,
        SurfaceHolder.Callback, Camera.PreviewCallback {

    public static final String LOGTAG = "MJPEG_FFMPEG";

    private SurfaceHolder holder;
    private CamcorderProfile camcorderProfile;
    private Camera camera;

    byte[] previewCallbackBuffer;

    boolean recording = false;
    boolean previewRunning = false;
    FFmpeg ffmpeg;
    File jpegFile;
    int fileCount = 0;

    FileOutputStream fos;
    BufferedOutputStream bos;
    Button recordButton;

    Camera.Parameters p;
    private File savePath;
    private RateLimiter limiter;
    private String filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ffmpeg = FFmpeg.getInstance(getApplicationContext());
        limiter = RateLimiter.create(30);
        savePath = new File(getApplicationContext().getExternalCacheDir(), "Files");
        savePath.mkdirs();
        jpegFile = new File(savePath, "/frame" /*+ formattedFileCount*/ + ".data");
        filePath = jpegFile.getAbsolutePath();
        jpegFile.delete();
        if(!jpegFile.exists()) {
            try {
                jpegFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fos = new FileOutputStream(jpegFile, false);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_mjpegffmpegtest);

        recordButton = (Button) this.findViewById(R.id.RecordButton);
        recordButton.setOnClickListener(this);

        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (recording)
        {
            recording = false;
            Log.v(LOGTAG, "Recording Stopped");

            // Convert to video
            //processVideo = new ProcessVideo();
            //processVideo.execute();
        }
        else
        {
            recording = true;
            Log.v(LOGTAG, "Recording Started");
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceCreated");

        camera = Camera.open();

		/*
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			previewRunning = true;
		}
		catch (IOException e) {
			Log.e(LOGTAG,e.getMessage());
			e.printStackTrace();
		}
		*/
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(LOGTAG, "surfaceChanged");

        if (!recording) {
            if (previewRunning){
                camera.stopPreview();
            }

            try {
                p = camera.getParameters();
                List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();

                // You need to choose the most appropriate previewSize for your app
                Camera.Size previewSize = previewSizes.get(0); // .... select one of previewSizes here
                //p.setPreviewSize(previewSize.width, previewSize.height);
                p.setPreviewSize(640, 480);
                camera.setParameters(p);

                camera.setPreviewDisplay(holder);

				/*
				Log.v(LOGTAG,"Setting up preview callback buffer");
				previewCallbackBuffer = new byte[(camcorderProfile.videoFrameWidth * camcorderProfile.videoFrameHeight *
													ImageFormat.getBitsPerPixel(p.getPreviewFormat()) / 8)];
				Log.v(LOGTAG,"setPreviewCallbackWithBuffer");
				camera.addCallbackBuffer(previewCallbackBuffer);
				camera.setPreviewCallbackWithBuffer(this);
				*/

                camera.setPreviewCallback(this);

                Log.v(LOGTAG,"startPreview");
                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e) {
                Log.e(LOGTAG,e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceDestroyed");
        if (recording) {
            recording = false;

            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        previewRunning = false;
        camera.release();
        finish();
    }
    public static final String command = "-f rawvideo -pix_fmt nv21 -s 640x480 -r 30 -i %s -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 http://letsrobot.tv:11225/"+ RobotApplication.getCameraPass()+"/%s/%s/";
    public void onPreviewFrame(byte[] b, Camera c) {
        //Log.v(LOGTAG,"onPreviewFrame");
        if (recording && limiter.tryAcquire()) {
            // Assuming ImageFormat.NV21
            if (p.getPreviewFormat() == ImageFormat.NV21) {
                //Log.v(LOGTAG,"Started Writing Frame");
                try {
                    //bos = new BufferedOutputStream(fos);
                    /*Camera.Size size = p.getPreviewSize();
                    int width = size.width;
                    int height = size.height;*/
                    //YuvImage im = new YuvImage(b, ImageFormat.NV21, p.getPreviewSize().width, p.getPreviewSize().height, null);
                    //Rect r = new Rect(0,0,p.getPreviewSize().width,p.getPreviewSize().height);

                    fos.write(b);
                    fos.flush();

                    try {
                        //ffmpeg.killRunningProcesses();
                        ffmpeg.execute(String.format(command, filePath, 640, 480).split(" "), new FFmpegExecuteResponseHandler() {
                            @Override
                            public void onSuccess(String message) {
                                //Log.d("FFMpeg", message);
                            }

                            @Override
                            public void onProgress(String message) {
                                //Log.d("FFMpeg", message);
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.d("FFMpeg", message);
                            }

                            @Override
                            public void onStart() {
                                //Log.d("FFMpeg", "onStart");
                            }

                            @Override
                            public void onFinish() {
                                //Log.d("FFMpeg", "onFinish");
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        e.printStackTrace();
                    }
                    /*bos.flush();
                    bos.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }

                //Log.v(LOGTAG,"Finished Writing Frame");
            } else {
                Log.v(LOGTAG,"NOT THE RIGHT FORMAT");
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration conf)
    {
        super.onConfigurationChanged(conf);
    }

    private class ProcessVideo extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {



            try {

                //ffmpeg -r 10 -b 1800 -i %03d.jpg test1800.mp4
                // 00000
                // /data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg -r p.getPreviewFrameRate() -b 1000 -i frame_%05d.jpg video.mov

                //String[] args2 = {"/data/data/com.mobvcasting.ffmpegcommandlinetest/ffmpeg", "-y", "-i", "/data/data/com.mobvcasting.ffmpegcommandlinetest/", "-vcodec", "copy", "-acodec", "copy", "-f", "flv", "rtmp://192.168.43.176/live/thestream"};


                File file = new File(savePath, "frame_%05d.jpg");
                File mov = new File(savePath, "video.mov");
                //
                String args222 = "ffmpeg -y -loop 1 -t 3.03 -i " + file.getAbsolutePath() + " -r 1 -vcodec libx264 -b:v 200k -bt 350k -f mp4 " + mov.getAbsolutePath();
                String args22 = "-r 60 -f image2 -s 1920x1080 -start_number 1 -i " + file.getAbsolutePath() + " -vcodec libx264 -crf 25  -pix_fmt yuv420p " + mov.getAbsolutePath();
                String args = "-f v4l2 -r p.getPreviewFrameRate() -i "+ file.getAbsolutePath() +" -b:v 1000 " + mov.getAbsolutePath();
                String[] ffmpegCommand = {"-r", ""+p.getPreviewFrameRate(), "-i", file.getAbsolutePath() ,"-vcodec", "mjpeg","-b:v", "1000000", mov.getAbsolutePath()};
                Log.d("TAG", args22);
                ffmpeg.execute(args222.split(" "), new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d("FFMpeg", message);
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.d("FFMpeg", message);
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.d("FFMpeg", message);
                    }

                    @Override
                    public void onStart() {
                        Log.d("FFMpeg", "onStart");
                    }

                    @Override
                    public void onFinish() {
                        Log.d("FFMpeg", "onFinish");
                    }
                });


            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void... result) {
            Toast toast = Toast.makeText(MJPEGFFMPEGTest.this, "Done Processing Video", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}