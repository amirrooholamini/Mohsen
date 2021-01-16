package project.broadcast_receiver;

import android.com.i3center.rooholamini.mohsen.App;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import project.activities.ActivityMusicDetails;
import project.services.MusicPlayer;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getExtras().getString("ACTION");
        if(action.equals("REMOVE")) {
            MusicPlayer.closeApp = true;
            App.getCurrentActivity().stopService(App.musicServiceIntent);
        }else if(action.equals("TOGGLE")){
            MusicPlayer.toggleState();
        }else if(action.equals("NEXT")){
            MusicPlayer.playNextMusic();
        }else if(action.equals("PREVIOUS")){
            MusicPlayer.playPreviousMusic();
        }

    }
}
