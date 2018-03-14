package net.myenv.screenrecording;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class saveScreenshot {


    public static String save (Bitmap bitmap, Context context) {

        String path = Environment.getExternalStorageDirectory() + "/Screenshot/";
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
        String fullName = path + date + ".jpg";;
        File imageFile = new File (fullName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    Toast.makeText(context, "Save ok", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fullName;

    }

}
