package project.activities;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import project.adapters.AdapterFavoriteMusicRecycler;
import project.adapters.AdapterMusicRecycler;
import project.structures.StructMusic;

public class ActivityFavoritesMusics extends CAppCompatActivity {

    private RecyclerView lstMusic;
    private AdapterFavoriteMusicRecycler adapter;
    private ArrayList<StructMusic> favoriteMusics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites_musics);

        setActionBar();
        setActionBarTitle("Favorites");

        lstMusic = (RecyclerView) findViewById(R.id.lstMusic);
        readFromCache();
        adapter = new AdapterFavoriteMusicRecycler(App.favoriteMusics);
        lstMusic.setLayoutManager(new LinearLayoutManager(App.getContext()));
        lstMusic.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private int readFromCache() {
        App.favoriteMusics = new ArrayList<StructMusic>();
        App.favoriteMusics.clear();
        Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS WHERE music_favorite = 1", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("music_id"));
            int albumId = cursor.getInt(cursor.getColumnIndex("album_id"));
            String name = cursor.getString(cursor.getColumnIndex("music_name"));
            String composer = cursor.getString(cursor.getColumnIndex("music_composer"));
            String songWriter = cursor.getString(cursor.getColumnIndex("music_songWriter"));
            String arrangement = cursor.getString(cursor.getColumnIndex("music_arrangement"));
            String fileUrl = cursor.getString(cursor.getColumnIndex("music_fileUrl"));
            String coverUrl = cursor.getString(cursor.getColumnIndex("music_coverUrl"));
            boolean isFavorite = cursor.getInt(cursor.getColumnIndex("music_favorite")) == 1 ? true : false;
            StructMusic music = new StructMusic(id,albumId, name, 2.3f, coverUrl, fileUrl, isFavorite);
            App.favoriteMusics.add(music);
        }
        int offset = cursor.getCount();
        cursor.close();
        return offset;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
