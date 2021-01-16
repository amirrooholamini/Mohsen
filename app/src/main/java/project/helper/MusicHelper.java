package project.helper;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

import project.adapters.AdapterFavoriteMusicRecycler;
import project.adapters.AdapterMusicRecycler;
import project.structures.StructMusic;

public class MusicHelper {

    private static ArrayList<Integer> indexes = new ArrayList<>();

    private static boolean optimizationTime = false;

    synchronized public static void showCoverImage(final int position, final AdapterMusicRecycler.ViewHolder holder, final StructMusic music) {
        Bitmap bitmap = getBitmap(position,music.fileName);
        holder.imgCover.setImageBitmap(bitmap);

    }

    synchronized public static void showCoverImage(final int position, final AdapterFavoriteMusicRecycler.ViewHolder holder, final StructMusic music) {
        Bitmap bitmap = getBitmap(position,music.fileName);
        holder.imgCover.setImageBitmap(bitmap);

    }
    private static Bitmap getBitmap(int position, String fileName) {
        if (App.fullMusics.get(position).coverBitmap != null) {
            return App.fullMusics.get(position).coverBitmap;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        try {
            File musicFile = new File(App.DIR_MUSIC + "/" + fileName);
            Uri uri = Uri.fromFile(musicFile);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            byte[] rawArt;
            final Bitmap cover;


            mmr.setDataSource(App.getContext(), uri);
            rawArt = mmr.getEmbeddedPicture();
            if (null != rawArt) {
                cover = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, options);
                indexes.add(position);
                if (!optimizationTime && indexes.size() > 60) {
                    optimizeBitmaps();
                }
                App.fullMusics.get(position).coverBitmap = cover;
                return cover;
            }
            Bitmap bitmap = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover,options);
            App.fullMusics.get(position).coverBitmap = bitmap;
            return bitmap;
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.default_music_cover,options);
            App.fullMusics.get(position).coverBitmap = bitmap;
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
                    App.fullMusics.get(index).coverBitmap = null;
                    indexes.remove(0);
                }
                optimizationTime = false;
            }
        }).start();

    }
}

