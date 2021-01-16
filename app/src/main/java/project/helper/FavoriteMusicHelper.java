package project.helper;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import project.adapters.AdapterFavoriteMusicRecycler;
import project.adapters.AdapterMusicRecycler;
import project.structures.StructMusic;

public class FavoriteMusicHelper {

    private static ArrayList<AdapterFavoriteMusicRecycler.ViewHolder> holders = new ArrayList<>();
    private static ArrayList<StructMusic> musics = new ArrayList<>();
    private static ArrayList<Integer> positions = new ArrayList<>();
    private static ArrayList<Integer> indexes = new ArrayList<>();
    private static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private static ArrayList<ImageView> images = new ArrayList<>();


    private static boolean optimizationTime = false;
    private static boolean threadIsRunning = false;

    private static interface OnReadyThread {
        void onReady();

    }

    private static OnReadyThread readyThread;

    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    synchronized public static void showCoverImage(final int position, final AdapterFavoriteMusicRecycler.ViewHolder holder, final StructMusic music) {

        if (!positions.contains(position)) {
            positions.add(position);
            musics.add(music);
            holders.add(holder);
        }

        if (!threadIsRunning && positions.size() > 0) {
            threadIsRunning = true;
            start();
        }

    }


    private static void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (positions.size() > 0) {
                    final AdapterFavoriteMusicRecycler.ViewHolder h = holders.get(0);
                    int p = positions.get(0);
                    String fn = musics.get(0).fileName;
                    final Bitmap coverBitmap = getBitmap(p, fn, h);
                    positions.remove(0);
                    musics.remove(0);
                    holders.remove(0);
                    App.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            h.imgCover.setImageBitmap(coverBitmap);
                        }
                    });

                }
                threadIsRunning = false;
            }
        }).start();
    }

    private static Bitmap getBitmap(int position, String fileName, final AdapterFavoriteMusicRecycler.ViewHolder holder) {
        if (App.favoriteMusics.get(position).coverBitmap != null) {
            return App.favoriteMusics.get(position).coverBitmap;
        }
        try {
            File musicFile = new File(App.DIR_MUSIC + "/" + fileName);
            Uri uri = Uri.fromFile(musicFile);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            byte[] rawArt;
            final Bitmap cover;


            mmr.setDataSource(App.getContext(), uri);
            rawArt = mmr.getEmbeddedPicture();
            if (null != rawArt) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                cover = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, options);


                indexes.add(position);
                if (!optimizationTime && indexes.size() > 60) {
                    optimizeBitmaps();
                }
                App.favoriteMusics.get(position).coverBitmap = cover;
                musics.get(0).coverBitmap = cover;
                return cover;
            }
            Bitmap bitmap = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover);
            App.favoriteMusics.get(position).coverBitmap = bitmap;
            musics.get(0).coverBitmap = bitmap;
            return bitmap;
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover);
            App.favoriteMusics.get(position).coverBitmap = bitmap;
            musics.get(0).coverBitmap = bitmap;
            return bitmap;
        }
    }


    private static void optimizeBitmaps() {
        optimizationTime = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    int index = indexes.get(0);
                    App.favoriteMusics.get(index).coverBitmap = null;
                    indexes.remove(0);
                }
                optimizationTime = false;
            }
        }).start();

    }
}

