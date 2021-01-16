package project.structures;

import android.com.i3center.rooholamini.mohsen.App;
import android.graphics.Bitmap;

import java.io.File;

import project.helper.FileHelper;

public class StructVideo {

    public int videoID;

    public String fileUrl;
    public String coverUrl;
    public String fileName;
    public String videoTitle;
    public String videoCity;
    public String videoDate;
    public Bitmap coverBitmap;
    public float rate;
    public boolean exists;
    public boolean isFavorite;
    public boolean infoState = false;
    public OnStateChangeListener listener;

    private String state = "";





    public interface OnStateChangeListener{
        void onChange(String newState, int position);
    }


    public void changeState(String newState , int position){
       // ArrayList<StructMusic> oldStruct = new ArrayList<>();
       // oldStruct = App.fullMusics;
        state = newState;
        if(listener !=null) {
            listener.onChange(newState, position);
        }

    }
    
    public void setOnStateChangeListener(OnStateChangeListener listener){
        this.listener = listener;

    }

    public String getState(){
        return state;
    }

}



