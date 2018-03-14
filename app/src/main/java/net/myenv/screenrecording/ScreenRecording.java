package net.myenv.screenrecording;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenRecording {

    private Activity activity;
    private MediaRecorder mMediaRecorder;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    public static final int REQUEST_CODE_CAPTURE_RECORDING = 456;
    private static final String TAG = "Screen Recording App";

    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    public String file;

    public ScreenRecording(Activity ativity) {
        this.activity = ativity;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_CAPTURE_RECORDING) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Screen Cast Permission Denied" );
            return;
        }

        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        startRecord();

    }

    public void startRecord(){
        mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mMediaProjection != null) {
            try {
                getSize();
                mMediaRecorder = new MediaRecorder();
                initRecorder();
                mMediaProjection.createVirtualDisplay(
                        "Recording Screen",
                        screenWidth,
                        screenHeight,
                        screenDensity,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mMediaRecorder.getSurface(),
                        null,
                        null);

                mMediaRecorder.start();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
            activity.startActivityForResult(intent,REQUEST_CODE_CAPTURE_RECORDING);
        }

    }

    public void stopRecord(){


        if (mMediaRecorder != null) {
            try{
                SystemClock.sleep(500);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }catch(RuntimeException e){
                e.printStackTrace();
            }


        }

        if (mMediaProjectionManager != null) {
            mMediaProjectionManager = null;
        }

        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }

    }


    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(getPath(".mp4"));
            mMediaRecorder.setVideoSize(screenWidth, screenHeight);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPath(String format ) {
        String path = Environment.getExternalStorageDirectory() + "/Recorded Screen/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
        file = path + date + format;
        return file;
    }

    private void getSize(){
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
    }

    public String getFile() {
        return file;
    }
}

