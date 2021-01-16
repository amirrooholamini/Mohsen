package project.activities;

import android.app.ActivityManager;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import project.custom.CustomMusicBottomBar;
import project.custom.WaterProgressBar;
import project.helper.BlurBuilder;
import project.services.MusicPlayer;
import project.structures.StructMusic;

public class ActivityMusicDetails extends CAppCompatActivity {

    public int position = -1;
    private int currentVolume;
    private int maxVolume;
    private AudioManager audioManager;
    private CustomMusicBottomBar bottomBar;

    private byte playerState;
    private int duration;

    private Thread thread;


    private boolean progressCanUpdate;
    private boolean isMusicPlaying;
    private int now;


    private ImageView imgCover;
    private ImageView imgBackground;
    private ImageView imgShuffle;
    private ImageView imgRepeat;
    private ImageButton imgAction;
    private ImageButton imgNext;
    private ImageButton imgPrevious;
    private ImageView imgFavorite;
    private SeekBar volumeSeekBar;
    private TextView txtNow;
    private TextView txtDuration;
    private TextView txtName;
    private WaterProgressBar progressBar;

    private File musicFile;
    private String fileName;

    private String musicPath;
    private MusicPlayer childPlayer;
    private MusicPlayer.OnStateChangedListener listener;
    private boolean killThreads;
    private boolean activityIsRunning = false;
    private boolean canRepeateSame;

    private ArrayList<Integer> threadIds = new ArrayList<>();

    StructMusic music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        removeActionBar();
        setContentView(R.layout.activity_music_details);


        childPlayer = new MusicPlayer();
        killThreads = false;
        progressCanUpdate = true;
        canRepeateSame = false;

        Intent intent = getIntent();
        if (intent.hasExtra("POSITION")) {
            position = intent.getExtras().getInt("POSITION");
        } else {
            position = MusicPlayer.position;
        }

        if (intent.hasExtra("ALL")) {
            if (!intent.getExtras().getBoolean("ALL")) {
                String name = App.favoriteMusics.get(position).musicName;
                for (int i = 0; i < App.fullMusics.size(); i++) {
                    if (App.fullMusics.get(i).musicName.equals(name)) {
                        position = i;
                        break;
                    }
                }

            }
        }

        MusicPlayer.position = position;
        music = App.fullClassMusics.get(position);
        fileName = music.fileName;
        musicPath = App.DIR_MUSIC + "/" + fileName;
        musicFile = new File(musicPath);

        ConstraintLayout root = (ConstraintLayout) findViewById(R.id.root);
        bottomBar = (CustomMusicBottomBar) findViewById(R.id.customMusicBottomBar);
        imgAction = (ImageButton) findViewById(R.id.imgAction);
        imgNext = (ImageButton) findViewById(R.id.imgNext);
        imgPrevious = (ImageButton) findViewById(R.id.imgPrevious);
        imgCover = (ImageView) findViewById(R.id.imgCover);
        imgRepeat = (ImageView) findViewById(R.id.imgRepeat);
        imgShuffle = (ImageView) findViewById(R.id.imgShuffle);
        imgCover = (ImageView) findViewById(R.id.imgCover);
        imgBackground = (ImageView) findViewById(R.id.imgBackground);
        volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        txtNow = (TextView) findViewById(R.id.txtNow);
        txtName = (TextView) findViewById(R.id.txtEmpty);
        txtDuration = (TextView) findViewById(R.id.txtDuration);
        progressBar = (WaterProgressBar) findViewById(R.id.progressBar);
        imgFavorite = (ImageView) findViewById(R.id.imgFavorite);

        MusicPlayer.shuffleMode = App.preferences.getBoolean(App.SHUFFLE_MODE_KEY, false);
        MusicPlayer.repeatMode = App.preferences.getBoolean(App.REPEAT_MODE_KEY, false);

        if (MusicPlayer.shuffleMode) {
            imgShuffle.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.repeat_shuffle_background));

        }

        if (MusicPlayer.repeatMode) {
            imgRepeat.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.repeat_shuffle_background));
        }

        if (music.isFavorite) {
            imgFavorite.setImageResource(R.drawable.favorite_on);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgPrevious.setBackgroundResource(R.drawable.music_buttons_ripple);
            imgAction.setBackgroundResource(R.drawable.music_buttons_ripple);
            imgNext.setBackgroundResource(R.drawable.music_buttons_ripple);
        } else {
            imgPrevious.setBackgroundResource(R.drawable.music_buttons_selection);
            imgAction.setBackgroundResource(R.drawable.music_buttons_selection);
            imgNext.setBackgroundResource(R.drawable.music_buttons_selection);
        }

        imgShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = App.preferences.edit();
                if (MusicPlayer.shuffleMode) {
                    MusicPlayer.shuffleMode = false;
                    imgShuffle.setBackground(new BitmapDrawable());
                    editor.putBoolean(App.SHUFFLE_MODE_KEY, false);
                } else {
                    MusicPlayer.shuffleMode = true;
                    imgShuffle.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.repeat_shuffle_background));
                    editor.putBoolean(App.SHUFFLE_MODE_KEY, true);
                }
                editor.commit();
            }
        });

        imgRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = App.preferences.edit();
                if (MusicPlayer.repeatMode) {
                    canRepeateSame = false;
                    MusicPlayer.repeatMode = false;
                    imgRepeat.setBackground(new BitmapDrawable());
                    editor.putBoolean(App.REPEAT_MODE_KEY, false);
                } else {
                    canRepeateSame = true;
                    MusicPlayer.repeatMode = true;
                    imgRepeat.setBackground(ContextCompat.getDrawable(App.getContext(), R.drawable.repeat_shuffle_background));
                    editor.putBoolean(App.REPEAT_MODE_KEY, true);
                }
                editor.commit();
            }
        });

        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (music.isFavorite) {
                    imgFavorite.setImageResource(R.drawable.favorite_off);
                    App.fullMusics.get(position).isFavorite = false;
                    App.musicDatabase.execSQL
                            ("UPDATE MUSICS SET music_favorite = 0 " +
                                    "WHERE music_id = " +
                                    music.musicID);
                } else {
                    imgFavorite.setImageResource(R.drawable.favorite_on);
                    App.fullMusics.get(position).isFavorite = true;
                    App.musicDatabase.execSQL
                            ("UPDATE MUSICS SET music_favorite = 1 " +
                                    "WHERE music_id = " +
                                    music.musicID);
                }
            }
        });


        progressBar.setUserCanChange(true);


        txtName.setTypeface(App.persianFont);
        txtName.setText(App.fullMusics.get(position).musicName);
        imgAction.setImageResource(R.drawable.play);

        audioManager = (AudioManager) getSystemService(App.getContext().AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);


        listener = new MusicPlayer.OnStateChangedListener() {
            @Override
            public void state(boolean isPlaying) {
                if (isPlaying) {
                    imgAction.setImageResource(R.drawable.pause);
                    isMusicPlaying = true;
                } else {
                    imgAction.setImageResource(R.drawable.play);
                    isMusicPlaying = false;
                }
            }

            @Override
            public void changeMusic(boolean nextMusic) {
                if (MusicPlayer.shuffleMode) {
                    playRandomMusic();
                } else {
                    if (nextMusic) {
                        playNextMusic();
                    } else {
                        playPreviousMusic();
                    }
                }
            }

            @Override
            public void changeMode(boolean isShuffleMode, boolean isRepeatMode) {
                if (isShuffleMode) {
                    playRandomMusic();
                } else if (isRepeatMode) {
                    canRepeateSame = true;
                    reset();
                } else {
                    playNextMusic();
                }
            }
        };

        childPlayer.setOnStateListener(listener);
        reset();

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean userChanged) {
                if (userChanged) {
                    currentVolume = volumeSeekBar.getProgress();
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicPlayer.shuffleMode) {
                    playRandomMusic();
                } else {
                    playNextMusic();
                }
            }
        });

        imgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MusicPlayer.shuffleMode) {
                    playRandomMusic();
                } else {
                    playPreviousMusic();
                }
            }
        });

        imgAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayer.toggleState();
            }
        });


        progressBar.setOnProgressChangeListener(new WaterProgressBar.OnProgressChangeListener() {

            @Override
            public void onProgressChange(float progress, boolean userInput, boolean touchDown) {
                if (userInput) {
                    if (touchDown) {
                        if (MusicPlayer.getCurrentPosition() <= MusicPlayer.getDuration()) {
                            txtNow.setText(convertToTimeFormat(MusicPlayer.getCurrentPosition()));
                        }
                        progressCanUpdate = false;
                    } else {
                        float percent = progress / 100;
                        MusicPlayer.seekPlayerTo((int) (MusicPlayer.getDuration() * percent));
                        progressCanUpdate = true;
                    }

                }

            }
        });

    }

    public void reset() {
        MusicPlayer.position = position;
        progressBar.setProgress(0);
        txtNow.setText("00:00");
        txtName.setTypeface(App.persianFont);
        txtDuration.setTypeface(App.persianFont);
        txtNow.setTypeface(App.persianFont);
        txtName.setText(music.musicName);
        showCoverImage();

        if (music.isFavorite) {
            imgFavorite.setImageResource(R.drawable.favorite_on);
        }

        if (isMyServiceRunning()) {
            if ((!MusicPlayer.getMusicPath().equals(musicPath) || canRepeateSame) && App.musicServiceIntent != null) { // new music
                MusicPlayer.pause();
                canRepeateSame = false;
                MusicPlayer.music = music;
                App.musicServiceIntent = new Intent(App.getCurrentActivity(), MusicPlayer.class);
                App.musicServiceIntent.putExtra("PATH", musicPath);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    App.getCurrentActivity().startForegroundService(App.musicServiceIntent);
                } else {
                    App.getCurrentActivity().startService(App.musicServiceIntent);
                }
                isMusicPlaying = true;
            }
            boolean playerIsPlaying = MusicPlayer.isMusicPlaying();
            if (playerIsPlaying) {
                imgAction.setImageResource(R.drawable.pause);
                isMusicPlaying = true;
            } else {
                imgAction.setImageResource(R.drawable.play);
                isMusicPlaying = false;
            }

        } else {
            imgAction.setImageResource(R.drawable.pause);
            MusicPlayer.music = music;
            App.musicServiceIntent = new Intent(App.getCurrentActivity(), MusicPlayer.class);
            App.musicServiceIntent.putExtra("PATH", musicPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                App.getCurrentActivity().startForegroundService(App.musicServiceIntent);
            } else {
                App.getCurrentActivity().startService(App.musicServiceIntent);
            }
            isMusicPlaying = true;

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgressAlpha(128);
                progressBar.setProgressColor(getAverageColor());
                progressBar.setProgressAlpha(128);
            }
        }).start();
        killThreads = false;
        startThread();
        txtDuration.setText(convertToTimeFormat(MusicPlayer.getDuration()) + "");
    }

    private void startThread() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int myId = (int) (Math.random() * 10000);
                    threadIds.add(myId);
                    duration = MusicPlayer.getDuration();
                    float now = 0;
                    while (!killThreads) {
                        if (threadIds.size() > 1 && myId == threadIds.get(threadIds.size() - 2)) {
                            threadIds.remove(threadIds.size() - 2);
                            break;
                        }
                        if (progressCanUpdate) {
                            now = MusicPlayer.getCurrentPosition();
                            duration = MusicPlayer.getDuration();
                            if (now > duration) {
                                Thread.sleep(50);
                                continue;
                            }
                            float progress = (float) (100.0f * (float) now / (float) duration);
                            progressBar.setProgress(progress);
                            final int min = Math.min(duration, (int) Math.floor(now));
                            App.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    txtDuration.setText(convertToTimeFormat(duration));
                                    txtNow.setText(convertToTimeFormat(min));
                                }
                            });
                        }
                        if (killThreads) {
                            break;
                        }
                        Thread.sleep(50);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    private String convertToTimeFormat(int time) {
        time = time / 1000;
        String stringTime = "";
        int minutes = time / 60;
        int seconds = time % 60;
        if (minutes < 10) {
            stringTime += "0";
        }
        stringTime += minutes;
        stringTime += ":";

        if (seconds < 10) {
            stringTime += "0";
        }
        stringTime += seconds;
        String temp = stringTime;
        return temp;
    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(App.getContext().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MusicPlayer.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volumeSeekBar.setProgress(currentVolume);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volumeSeekBar.setProgress(currentVolume);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void showCoverImage() {
        try {
            Uri uri = Uri.fromFile(musicFile);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            byte[] rawArt;
            Bitmap art;
            BitmapFactory.Options bfo = new BitmapFactory.Options();

            mmr.setDataSource(App.getContext(), uri);
            rawArt = mmr.getEmbeddedPicture();
            if (null != rawArt) {
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
                imgCover.setImageBitmap(art);
                Bitmap blurredBitmap = BlurBuilder.blur(App.getContext(), art);
                imgBackground.setImageDrawable(new BitmapDrawable(getResources(), art));
            } else {
                imgCover.setImageResource(R.drawable.default_music_cover);
                imgBackground.setImageResource(R.drawable.default_music_cover);
            }
        } catch (Exception e) {
            Log.i("LOG", "read cover failed exception");
        }
    }

    @Override
    public void onBackPressed() {
        killThreads = true;
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private int getAverageColor() {

        Uri uri = Uri.fromFile(musicFile);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inSampleSize = 20;

        mmr.setDataSource(App.getContext(), uri);
        rawArt = mmr.getEmbeddedPicture();
        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
        } else {
            art = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_cover);
        }

        long redBucket = 0;
        long greenBucket = 0;
        long blueBucket = 0;
        long pixelCount = 0;


        for (int y = 0; y < art.getHeight(); y++) {
            for (int x = 0; x < art.getWidth(); x++) {
                int c = art.getPixel(x, y);

                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
                pixelCount = redBucket + blueBucket + greenBucket;
                // does alpha matter?
            }

        }
        int red = (int) (255 * redBucket / pixelCount);
        int green = (int) (255 * greenBucket / pixelCount);
        int blue = (int) (255 * blueBucket / pixelCount);

        int averageColor = Color.rgb(
                red,
                green,
                blue);

        return averageColor;
    }

    public void playNextMusic() {
        killThreads = true;
        do {
            position++;
            if (position >= App.fullClassMusics.size()) {
                position = 0;
            }
            music = App.fullClassMusics.get(position);
            fileName = (music.fileName);
            musicPath = App.DIR_MUSIC + "/" + fileName;
            musicFile = new File(musicPath);
        } while (!musicFile.exists());
        reset();
    }

    public void playPreviousMusic() {
        killThreads = true;
        do {
            position--;
            if (position < 0) {
                position = App.fullClassMusics.size() - 1;
            }
            music = App.fullClassMusics.get(position);
            fileName = (music.fileName);
            musicPath = App.DIR_MUSIC + "/" + fileName;
            musicFile = new File(musicPath);
        } while (!musicFile.exists());

        reset();
    }

    private void playRandomMusic() {
        killThreads = true;
        canRepeateSame = true;
        do {
            position = (int) (Math.floor(Math.random() * App.fullClassMusics.size()));
            music = App.fullMusics.get(position);
            fileName = App.fullMusics.get(position).fileName;
            musicPath = App.DIR_MUSIC + "/" + fileName;
            musicFile = new File(musicPath);
        } while (!musicFile.exists());
        reset();
    }

    @Override
    protected void onDestroy() {
        killThreads = true;
        super.onDestroy();
    }
}
