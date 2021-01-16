package project.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.File;

import project.activities.ActivityMusicsList;
import project.activities.ActivityMusicDetails;
import project.broadcast_receiver.NotificationReceiver;
import project.structures.StructMusic;

public class NotificationHelper {
    private static RemoteViews collapsedView;
    private static RemoteViews expandededView;
    private static Notification notification;
    private static NotificationCompat.Builder builder;

    public static Notification createNotification(String path, StructMusic music) {

        File musicFile = new File(path);
        if (musicFile.exists()) {
        }
        Bitmap largIcon = getCoverImage(musicFile);
        if (largIcon == null) {
            BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover);
        }
        Bitmap largIcon2 = Bitmap.createScaledBitmap(largIcon, largIcon.getWidth() / 4, largIcon.getHeight() / 4, false);

        collapsedView = new RemoteViews(App.packageName, R.layout.media_notification_collapsed);
        expandededView = new RemoteViews(App.packageName, R.layout.media_notification_expanded);


        Intent toggleIntent = new Intent(App.getContext(), NotificationReceiver.class);
        toggleIntent.putExtra("ACTION", "TOGGLE");
        PendingIntent togglePendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent removeIntent = new Intent(App.getContext(), NotificationReceiver.class);
        removeIntent.putExtra("ACTION", "REMOVE");
        PendingIntent removePendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                2,
                removeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(App.getContext(), NotificationReceiver.class);
        nextIntent.putExtra("ACTION", "NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                3,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(App.getContext(), NotificationReceiver.class);
        previousIntent.putExtra("ACTION", "PREVIOUS");
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                4,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent activityMain = new Intent(App.getContext(), ActivityMusicsList.class);
        activityMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent activityMusicDetails = new Intent(App.getContext(), ActivityMusicDetails.class);
        activityMusicDetails.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        Intent[] intents = {activityMain, activityMusicDetails};
        PendingIntent contentIntent = PendingIntent.getActivities(
                App.getContext(),
                0,
                intents,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        collapsedView.setImageViewBitmap(R.id.imgBackground, largIcon);
        collapsedView.setTextViewText(R.id.txtTitle, music.musicName);
        collapsedView.setImageViewBitmap(R.id.imgCover, largIcon2);
        collapsedView.setImageViewResource(R.id.imgAction, R.drawable.notification_pause);

        expandededView.setImageViewBitmap(R.id.imgCover, largIcon);
        expandededView.setTextViewText(R.id.txtTitle, music.musicName);
        expandededView.setImageViewResource(R.id.imgAction, R.drawable.notification_pause);

        collapsedView.setOnClickPendingIntent(R.id.imgAction, togglePendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.imgClose, removePendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.imgPrevious, previousPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.imgNext, nextPendingIntent);

        expandededView.setOnClickPendingIntent(R.id.imgAction, togglePendingIntent);
        expandededView.setOnClickPendingIntent(R.id.imgClose, removePendingIntent);
        expandededView.setOnClickPendingIntent(R.id.imgPrevious, previousPendingIntent);
        expandededView.setOnClickPendingIntent(R.id.imgNext, nextPendingIntent);

        builder = new NotificationCompat.Builder(App.getContext(), App.CHANNEL_2_ID)
                .setSmallIcon(R.mipmap.logo)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandededView)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notification = builder.build();


        return notification;
    }

    private static Bitmap getCoverImage(File musicFile) {
        try {
            Uri uri = Uri.fromFile(musicFile);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            byte[] rawArt;
            BitmapFactory.Options bfo = new BitmapFactory.Options();

            mmr.setDataSource(App.getContext(), uri);
            rawArt = mmr.getEmbeddedPicture();
            if (null != rawArt) {
                return BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
            } else {
                return BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover);
            }
        } catch (Exception e) {
            return BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover);
        }
    }

    public static Notification toggleState(int actionIcon) {
        if(builder == null){
            builder = new NotificationCompat.Builder(App.getContext(), App.CHANNEL_2_ID);
        }
        collapsedView.setImageViewResource(R.id.imgAction, actionIcon);
        expandededView.setImageViewResource(R.id.imgAction, actionIcon);
        notification = builder
                .setSmallIcon(R.mipmap.logo)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandededView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        return notification;
    }
}
