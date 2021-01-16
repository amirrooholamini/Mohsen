package project.helper;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import project.adapters.AdapterMusicRecycler;
import project.custom.CustomToast;
import project.structures.StructMusic;

import static java.lang.System.out;

public class FileHelper {


    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);

    }

    public static void copyFile(String sourcePath, String destinationPath) {

        if(new File(destinationPath).exists()){
            CustomToast.showToast(App.getContext().getString(R.string.fileSaved));
            return;
        }

        boolean fail = false;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
             inputStream = new FileInputStream(sourcePath);
             outputStream = new FileOutputStream(destinationPath);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            CustomToast.showToast(App.getContext().getString(R.string.fail));
            fail = true;
        } catch (IOException e) {
            e.printStackTrace();
            CustomToast.showToast(App.getContext().getString(R.string.fail));
            fail = true;
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                CustomToast.showToast(App.getContext().getString(R.string.fail));
                fail = true;
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                CustomToast.showToast(App.getContext().getString(R.string.fail));
                fail = true;
                e.printStackTrace();
            }
        }
        if(!fail){
            CustomToast.showToast(App.getContext().getString(R.string.fileSaved));
        }
    }


}