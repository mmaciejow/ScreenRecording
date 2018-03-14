package net.myenv.screenrecording;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class TakeScreenshot implements ImageReader.OnImageAvailableListener {

    private Activity activity;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Bitmap bitmap;
    public String file;
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    public static final int REQUEST_CODE_CAPTURE_IMAGE = 789;
    private static final String TAG = "Screen Recording App";







    public TakeScreenshot(Activity act) {
        this.activity = act;
    }

    private MyInterface myInterface;

    public interface MyInterface {
        void onSending(Bitmap bitmap);
    }

    public void setOnSendListener(MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_CAPTURE_IMAGE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Screen Cast Permission Denied" );
            return;
        }

        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        takescreenshot();

    }

    public void takescreenshot(){

        mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mMediaProjection != null) {

            try {
                getSize();
                mImageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
                mMediaProjection.createVirtualDisplay(
                        "Screenshot",
                        screenWidth,
                        screenHeight,
                        screenDensity,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mImageReader.getSurface(),
                        null,
                        null );

                mImageReader.setOnImageAvailableListener( this, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
            activity.startActivityForResult(intent,REQUEST_CODE_CAPTURE_IMAGE);
        }

    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image;
        image = reader.acquireLatestImage();
        if (image == null) return;
        final Image.Plane[] planes = image.getPlanes();
        final Buffer buffer = planes[0].getBuffer().rewind();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * screenWidth;

        // create bitmap
        bitmap = Bitmap.createBitmap(screenWidth+rowPadding/pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        if (bitmap != null) {
            image.close();
            myInterface.onSending(bitmap);
            clean();
        }
    }

public void clean(){

    if (mMediaProjectionManager != null) {
        mMediaProjectionManager = null;
    }

    if (mMediaProjection != null) {
        mMediaProjection.stop();
    }

    if (mImageReader != null) {
        mImageReader.setOnImageAvailableListener(null, null);
        mImageReader.close();
    }

    if (bitmap != null) {
        bitmap = null;
    }
}

    private void getSize(){
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
    }




}







