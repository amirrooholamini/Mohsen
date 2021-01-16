package project.structures;

import android.com.i3center.rooholamini.mohsen.App;
import android.graphics.Bitmap;
import android.support.v7.util.DiffUtil;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import project.helper.FileHelper;

public class StructMusic {

    public int musicID;
    public int albumID;
    public int fullMusicsIndex;
    public String fileUrl;
    public String coverUrl;
    public String fileName;
    public String musicName;
    public Bitmap coverBitmap;
    public float rate;
    public OnStateChangeListener listener;
    public boolean exists;
    public float downloadPercentage;
    public boolean isFavorite;

    private String state;


    public interface OnStateChangeListener {
        void onChange(String newState, int position);
        void onChange(String newState, int position , float downloaded , float total);
    }

    public StructMusic(int id,int albumID, String musicName, float rate, String coverUrl, String fileUrl, boolean isFavorite) {
        this.musicID = id;
        this.albumID = albumID;
        this.musicName = musicName;
        this.coverUrl = coverUrl;
        this.fileUrl = fileUrl;
        this.rate = rate;
        this.fileName = FileHelper.getFileName(fileUrl);
        File file = new File(App.DIR_MUSIC + "/" + fileName);
        exists = file.exists();
        this.isFavorite = isFavorite;
        coverBitmap = null;
        downloadPercentage = 0;
        this.state = "";
    }

    public void changeState(String newState , int position){
        state = newState;
        if(listener !=null) {
            listener.onChange(newState, position);
        }

    }

    public void changeState(String newState , int position , float downloaded , float total){
        state = newState;
        if(listener !=null) {
            listener.onChange(newState, position , downloaded , total);
        }

    }

//

//    public void changeState(String newState) {
//        this.state = newState;
//
//    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;

    }

    public String getState() {
        return state;
    }

}



