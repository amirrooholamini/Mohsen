package project.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;

import project.activities.ActivityStart;
import project.helper.NotificationHelper;
import project.structures.StructMusic;

public class MusicPlayer extends Service {

    private static MediaPlayer player;
    private static int musicDuration;
    private static String path = "";
    public static StructMusic music;
    private static OnStateChangedListener listener;
    private static File musicFile;
    private static int actionIcon;

    private static Notification notification;

    public static boolean closeApp = false;
    public static boolean repeatMode = true;
    public static boolean shuffleMode = false;
    private static boolean randomSelection = false;


    public static int position = -1;




    public interface OnStateChangedListener {
        void state(boolean isPlaying);
        void changeMusic(boolean nextMusic);
        void changeMode(boolean isShuffleMode , boolean isRepeatMode);
    }



    public void setOnStateListener(OnStateChangedListener listener) {
        this.listener = listener;
    }

    public static String getMusicPath() {
        return path;
    }

    public static boolean isMusicPlaying() {
        if (player == null) {
            return false;
        }
        return player.isPlaying() ? true : false;
    }


    public static MediaPlayer getPlayer() {
        return player;
    }

    public static void seekPlayerTo(int seek) {
        player.seekTo(seek);
    }

    public static int getDuration() {
        if (player != null) {
            return player.getDuration();
        }
        return 1;
    }

    public static int getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        actionIcon = R.drawable.notification_pause;
        super.onCreate();
    }



    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (player == null || !player.isPlaying()) {
            path = intent.getExtras().getString("PATH");
            musicFile = new File(path);
            Uri uri = Uri.fromFile(musicFile);
            player = MediaPlayer.create(App.getContext(), uri);

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                   if(shuffleMode){
                       listener.changeMode(true,false);
                   }else if(repeatMode){
                       listener.changeMode(false,true);
                   }else{
                       listener.changeMode(false,false);
                   }
                }
            });
        }
        notification = NotificationHelper.createNotification(path,music);
        startForeground(App.musicNotificationId, notification);

        player.start();
        listener.state(true);
        return START_REDELIVER_INTENT;
    }



    public static void stop() {
        if (player != null) {
            player.stop();
            listener.state(false);

        }
    }

    public static void pause() {
        if (player != null) {
            player.pause();
            listener.state(false);
        }
    }

    public static void resume() {
        if (player != null) {
            player.start();
            listener.state(true);
        }
    }


    public static void setVolume(float percent) {
        if (player != null) {
            float volume = percent / 100;
            player.setVolume(volume, volume);
        }
    }

    public static void playNextMusic(){
        listener.changeMusic(true);
    }

    public static void playPreviousMusic(){
        listener.changeMusic(false);
    }

    public static void toggleState() {
        if (player.isPlaying()) {
            actionIcon = R.drawable.notification_play;
            player.pause();
            listener.state(false);
        } else {
            actionIcon = R.drawable.notification_pause;
            player.start();
            listener.state(true);
        }
       notification = NotificationHelper.toggleState(actionIcon);

        NotificationManager notificationManager = (NotificationManager) App.getContext().getSystemService(App.getContext().NOTIFICATION_SERVICE);
        notificationManager.notify(App.musicNotificationId, notification);

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        path = "";
        stopSelf();
        if(closeApp) {
            closeApp = false;
            stopForeground(true);
            Intent intent = new Intent(App.getCurrentActivity(), ActivityStart.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            App.getCurrentActivity().finishAffinity();

        }
    }
}
