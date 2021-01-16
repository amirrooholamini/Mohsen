package android.com.i3center.rooholamini.mohsen;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.TransitionRes;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.TelephonyManager;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

import project.structures.StructAlbum;
import project.structures.StructMusic;
import project.structures.StructVideo;

public class App extends Application {


    private static Context context;
    private static Activity currentActivity;
    public static Activity musicsActivity;
    private static LayoutInflater layoutInflater;
    private static TransitionInflater transitionInflater;
    private static Handler handler;
    private static DisplayMetrics displayMetrics;
    private static App base;
    public static Intent musicServiceIntent;
    public static Intent downloadServiceIntent;
    public static SQLiteDatabase musicDatabase;
    public static SQLiteDatabase albumDatabase;

    public static final String SERVER_ADDRESS = "https://mohsen-app.ir/";
    //    public static final String SERVER_ADDRESS = "https://i3center.com/files/";
    public static String DIR_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String DIR_PUBLIC_MUSIC;
    public static String DIR_APP = DIR_SDCARD + "/Mohsen/";
    public static String DIR_MUSIC = DIR_APP + "/Music/";
    public static String DIR_VIDEO = DIR_APP + "/.Video/";
    public static String DIR_COVER = DIR_APP + "/.Cover/";
    public static String DIR_DATABASE = DIR_APP + "/.Database/";
    public static int selectedMusicIndex = 0;
    public static StructMusic selectedMusic ;

    public static Typeface englishFont;
    public static Typeface persianFont;
    public static String packageName;

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final int musicNotificationId = 1;

    public static SharedPreferences preferences;
    public static final String REPEAT_MODE_KEY = "REPEAT_MODE";
    public static final String SHUFFLE_MODE_KEY = "SHUFFLE_MODE";

    public static ArrayList<StructAlbum> albums = new ArrayList<>();
    public static ArrayList<StructMusic> fullMusics = new ArrayList<>();
    public static ArrayList<StructMusic> fullClassMusics = new ArrayList<>();
    public static ArrayList<StructMusic> favoriteMusics;
    public static ArrayList<StructVideo> videos;

    public static String IMEI = "";

    private static Object systemService;


    @Override
    public void onCreate() {
        super.onCreate();
        base = this;
        context = getApplicationContext();
        DIR_PUBLIC_MUSIC =  context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        handler = new Handler();
        displayMetrics = getResources().getDisplayMetrics();
        layoutInflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            transitionInflater = TransitionInflater.from(context);
        }
        packageName = getPackageName();
        englishFont = ResourcesCompat.getFont(context, R.font.english);
        persianFont = ResourcesCompat.getFont(context, R.font.persian);
        systemService = getSystemService(Context.CONNECTIVITY_SERVICE);
//        new File(DIR_MUSIC).mkdirs();
//        new File(DIR_DATABASE).mkdirs();
//        createDatabases();
        createNotificationChannels();
    }


    public static App get() {
        return base;
    }

    public static Context getContext() {
        if (currentActivity != null) {
            return currentActivity;
        }

        return context;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public static TransitionInflater getTransitionInflater() {
        return transitionInflater;
    }

    public static Transition inflateTransition(@TransitionRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return transitionInflater.inflateTransition(res);
        }

        return null;
    }

    public static View inflateLayout(@LayoutRes int res) {
        return layoutInflater.inflate(res, null);
    }

    public static View inflateLayout(@LayoutRes int res, @Nullable ViewGroup root) {
        return layoutInflater.inflate(res, root);
    }

    public static Handler getHandler() {
        return handler;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH);
            channel1.enableLights(true);
            channel1.setLightColor(Color.parseColor(getResources().getString(R.color.golden)));

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW);


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }

    public static void createDatabases() {
        musicDatabase = SQLiteDatabase.openOrCreateDatabase(DIR_DATABASE + "/Musics.sqlite", null);
        musicDatabase.execSQL("CREATE TABLE IF NOT EXISTS MUSICS (" +
                "music_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ," +
                "album_id INTEGER ,"+
                "music_name TEXT ," +
                "music_rate FLOAT ," +
                "music_coverUrl TEXT ," +
                "music_fileUrl TEXT ," +
                "music_favorite INTEGER)");

        albumDatabase = SQLiteDatabase.openOrCreateDatabase(DIR_DATABASE + "/Albums.sqlite", null);
        albumDatabase.execSQL("CREATE TABLE IF NOT EXISTS ALBUMS (" +
                "album_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ," +
                "album_name TEXT ," +
                "album_date TEXT ," +
                "album_cover_url TEXT )");
    }

    public boolean isInternetAvailable() {
        try {
            ConnectivityManager connectivityManager= (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        } catch (Exception e) {
            Log.i("LOGG","Exception");
            return false;
        }
    }

    public static String getIMEI(){
        TelephonyManager telephonyManager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
