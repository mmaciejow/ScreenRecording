package net.myenv.screenrecording;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout linearLayout;
    private Button intent;

    private String file;
    private String type;

    private ScreenRecording recording;
    private TakeScreenshot takeScreenshot;

    private static final int PERMISSIONS_MULTIPLE_REQUEST = 123;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.button);

        findViewById(R.id.screenshotApp).setOnClickListener(this);
        findViewById(R.id.screenshotView).setOnClickListener(this);
        findViewById(R.id.screenshotDisplay).setOnClickListener(this);
        findViewById(R.id.recodingDisplay).setOnClickListener(this);

        intent = findViewById(R.id.intent);
        intent.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermission();
        else
            linearLayout.setVisibility(View.VISIBLE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            + ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                Snackbar.make(this.findViewById(R.id.linearayout),
                        "Please Grant Permissions",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                Manifest.permission.RECORD_AUDIO},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap takeScreenshotView(View view){
        Bitmap bitmap = null;
        try {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache(true);
            bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onClick(View view) {

        Bitmap bitmap;

        switch (view.getId()){
            case R.id.screenshotApp:
                View rootView = getWindow().getDecorView().getRootView();
                bitmap = takeScreenshotView(rootView);
                if (bitmap != null) {
                    file = saveScreenshot.save(bitmap, this);
                    type = "image/*";
                    intent.setText("Open Image");
                    intent.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.screenshotView:
                bitmap = takeScreenshotView(view);
                if (bitmap != null) {
                    file = saveScreenshot.save(bitmap, this);
                    type = "image/*";
                    intent.setText("Open Image");
                    intent.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.screenshotDisplay:
                takeScreenshot = new TakeScreenshot(this);
                takeScreenshot.takescreenshot();
                takeScreenshot.setOnSendListener(new TakeScreenshot.MyInterface() {
                    @Override
                    public void onSending(Bitmap bitmap) {

                        if (bitmap != null) {
                            file = saveScreenshot.save(bitmap, MainActivity.this);
                            type = "image/*";
                            intent.setText("Open Image");
                            intent.setVisibility(View.VISIBLE);
                            takeScreenshot = null;
                        }
                    }
                });
                break;
            case R.id.recodingDisplay:
                if (((ToggleButton) view).isChecked()) {
                    recording = new ScreenRecording(this);
                    recording.startRecord();
                } else {
                    recording.stopRecord();
                    file = recording.getFile();
                    type = "video/*";
                    intent.setText("Open Video");
                    intent.setVisibility(View.VISIBLE);
                    recording = null;
                }
                break;
            case R.id.intent:
                Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(file));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, type);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean writeExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudio          = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeExternalStorage && recordAudio)
                    {
                        linearLayout.setVisibility(View.VISIBLE);
                    } else {
                        Snackbar.make(this.findViewById(R.id.linearayout),
                                "Please Grant Permissions",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(
                                                    new String[]{Manifest.permission
                                                            .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                    PERMISSIONS_MULTIPLE_REQUEST);
                                        }
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        switch (requestCode) {
            case TakeScreenshot.REQUEST_CODE_CAPTURE_IMAGE:
                takeScreenshot.onActivityResult(requestCode, resultCode, result);
                break;
            case ScreenRecording.REQUEST_CODE_CAPTURE_RECORDING:
                recording.onActivityResult(requestCode, resultCode, result);
                break;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        intent.setText(null);
        intent.setVisibility(View.INVISIBLE);
    }

}
