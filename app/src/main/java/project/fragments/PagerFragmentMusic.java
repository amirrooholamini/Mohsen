package project.fragments;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import project.activities.ActivityAlbumsList;
import project.activities.ActivityMusicsList;
import project.adapters.AdapterMusicRecycler;
import project.connections.Commands;
import project.custom.CustomLoader;
import project.custom.LinearProgressBar;
import project.services.DownloaderService;
import project.structures.StructMusic;

public class PagerFragmentMusic extends Fragment {

    public static RecyclerView lstMusic;
    public static AdapterMusicRecycler adapter;
    private static float progress = 0;

    private Commands readMusics;
    private ArrayList<StructMusic> musics;
    private LinearProgressBar linearProgressBar;

    ViewGroup layoutLoader;
    CustomLoader loader;
    TextView txtName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_musics, container, false);
        lstMusic = (RecyclerView) rootView.findViewById(R.id.lstMusic);
        layoutLoader = (ViewGroup) rootView.findViewById(R.id.layoutLoader);
        loader = (CustomLoader) rootView.findViewById(R.id.loader);
        txtName = (TextView) rootView.findViewById(R.id.txtEmpty);
        linearProgressBar = (LinearProgressBar) rootView.findViewById(R.id.linearProgressBar);
        linearProgressBar.setVisibility(View.GONE);
        lstMusic.setLayoutManager(new LinearLayoutManager(App.getContext()));

        Animation loadingAnimation = AnimationUtils.loadAnimation(App.getContext(), R.anim.fade_loading);


        musics = new ArrayList<StructMusic>();
        musics.clear();

        int lastId = getLastMusicId();
        //readFromNet(lastId);
        readFromCache();
        for (StructMusic music : App.fullClassMusics) {
            musics.add(music);
        }

        setAdapter();
        linearProgressBar.setProgress(0);
        linearProgressBar.setVisibility(View.GONE);


        ActivityMusicsList.musicSearchListener = new ActivityMusicsList.OnSearchMusic() {
            @Override
            public void onSearched(String filter) {
                adapter.getFilter().filter(filter);
            }
        };

        return rootView;
    }

    private void readFromCache() {
        int index = 0;
        if (App.fullMusics == null || App.fullMusics.size() == 0) {
            App.fullMusics = new ArrayList<StructMusic>();
            App.fullClassMusics = new ArrayList<StructMusic>();
            App.fullMusics.clear();
            App.fullClassMusics.clear();
            Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS ORDER BY music_id DESC", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("music_id"));
                int albumId = cursor.getInt(cursor.getColumnIndex("album_id"));
                String name = cursor.getString(cursor.getColumnIndex("music_name"));
                String fileUrl = cursor.getString(cursor.getColumnIndex("music_fileUrl"));
                String coverUrl = cursor.getString(cursor.getColumnIndex("music_coverUrl"));
                float rate = cursor.getFloat(cursor.getColumnIndex("music_rate"));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndex("music_favorite")) == 1 ? true : false;
                StructMusic music = new StructMusic(id, albumId, name, 2.3f, coverUrl, fileUrl, isFavorite);
                App.fullMusics.add(music);
                if (albumId == ActivityMusicsList.albumId) {
                    music.fullMusicsIndex = index;
                    App.fullClassMusics.add(music);
                }
                index++;
            }
            int offset = cursor.getCount();
            cursor.close();
        } else {
            App.fullClassMusics = new ArrayList<StructMusic>();
            App.fullClassMusics.clear();
            Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS WHERE album_id = " + ActivityMusicsList.albumId + " ORDER BY music_id DESC", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("music_id"));
                int albumId = cursor.getInt(cursor.getColumnIndex("album_id"));
                String name = cursor.getString(cursor.getColumnIndex("music_name"));
                String fileUrl = cursor.getString(cursor.getColumnIndex("music_fileUrl"));
                String coverUrl = cursor.getString(cursor.getColumnIndex("music_coverUrl"));
                float rate = cursor.getFloat(cursor.getColumnIndex("music_rate"));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndex("music_favorite")) == 1 ? true : false;
                StructMusic music = new StructMusic(id, albumId, name, 2.3f, coverUrl, fileUrl, isFavorite);
                for (int i = 0; i < App.fullMusics.size(); i++) {
                    if (App.fullMusics.get(i).musicName.equals(music.musicName)) {
                        music.fullMusicsIndex = i;
                        break;
                    }
                }
                App.fullClassMusics.add(music);
            }
            int offset = cursor.getCount();
            cursor.close();
        }
    }

//    private void readFromNet(int lastId) {
//        readMusics = new Commands();
//        readMusics.setCompleteListener(new Commands.onCommandCompleteListener() {
//            @Override
//            public void onComplete() {
//                readFromCache();
//                for(StructMusic music : App.fullClassMusics){
//                    musics.add(music);
//                }
//                setAdapter();
//                linearProgressBar.setProgress(0);
//                linearProgressBar.setVisibility(View.GONE);
//
//            }
//            @Override
//            public void onComplete(String data){
//
//            }
//            @Override
//            public void onFail(String error) {
//                readFromCache();
//                for(StructMusic music : App.fullClassMusics){
//                    musics.add(music);
//                }
//                linearProgressBar.setProgress(0);
//                linearProgressBar.setVisibility(View.GONE);
//            }
//        }).readMusics(lastId);
//    }

    private void setAdapter() {
        DownloaderService.sincItems(musics);

        App.getHandler().post(new Runnable() {
            @Override
            public void run() {
                adapter = new AdapterMusicRecycler(musics);
                adapter.notifyDataSetChanged();
                lstMusic.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                AdapterMusicRecycler.downloadingNewItems = false;
                lstMusic.setVisibility(View.VISIBLE);
                layoutLoader.setVisibility(View.GONE);
                txtName.clearAnimation();
                loader.stopRotation();
            }
        });
        // App.fullMusics.get(0).fileUrl = "http://s7.picofile.com/d/8378668692/247241b0-1303-4775-85ec-1e55ac54dfc8/behet_ghol_midam.mp3";


    }

    private int getLastMusicId() {
        int lastId = -1;
        Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS", null);
        while (cursor.moveToNext()) {
            lastId = Math.max(lastId, cursor.getInt(cursor.getColumnIndex("music_id")));
        }
        cursor.close();
        return lastId;
    }

}
