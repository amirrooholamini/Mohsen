package project.activities;

import android.app.Dialog;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import project.adapters.AdapterAlbumRecycler;
import project.connections.Commands;
import project.custom.CustomLoader;
import project.custom.CustomToast;

public class ActivityAlbumsList extends CAppCompatActivity {
    private ImageView imgPoster;
    private File posterFile ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeActionBar();
        setContentView(R.layout.activity_albums_list);

        RecyclerView lstAlbum = (RecyclerView)findViewById(R.id.lstAlbum);
        imgPoster = (ImageView) findViewById(R.id.poster);
        AdapterAlbumRecycler adapter = new AdapterAlbumRecycler(App.albums);

        RtlGridLayoutManager gridLayoutManager = new RtlGridLayoutManager(App.getContext(),2);
        lstAlbum.setLayoutManager(gridLayoutManager);
        lstAlbum.setAdapter(adapter);

        final Dialog loadingDialog = new Dialog(ActivityAlbumsList.this);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        Window window = loadingDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        final CustomLoader loader = (CustomLoader)loadingDialog.findViewById(R.id.loader) ;
        loader.startRotation();
        new Commands().setCompleteListener(new Commands.onCommandCompleteListener() {
            @Override
            public void onComplete(String data) {
                try {
                    JSONObject posterInfo = new JSONObject(data);
                    String newDate = posterInfo.getString("dateTime");
                    String url = posterInfo.getString("url");
                    String lastPosterDate = App.preferences.getString("POSTER_DATE","");
                    if((newDate.length()>0 && !lastPosterDate.equals(newDate)) || !posterFile.exists()){ // new poster
                        if(posterFile.exists()){
                            posterFile.delete();
                        }
                        SharedPreferences.Editor editor = App.preferences.edit();
                        editor.putString("POSTER_DATE",newDate);
                        editor.commit();
                        showPoster(url);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onComplete() {}

            @Override
            public void onFail(String error) {
                Log.i("LOGGG","error in load poster info!");
            }
        }).getPosterInfo();

        final int lastId = getLastMusicId();
        if(lastId == -1) {
            loadingDialog.show();
        }
        new Commands().setCompleteListener(new Commands.onCommandCompleteListener() {
            @Override
            public void onComplete(String data) {
                loadingDialog.dismiss();
                loader.stopRotation();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
                loader.stopRotation();
            }

            @Override
            public void onFail(String error) {
                loadingDialog.dismiss();
                loader.stopRotation();
                if(lastId==-1){
                    CustomToast.showToast(App.getContext().getString(R.string.noInternetConnection));
                }
            }
        }).readMusics(getLastMusicId());

        posterFile = new File(App.DIR_COVER +"/poster.jpg");
        if(posterFile.exists()) {
            Picasso.with(App.getContext()).load(posterFile).into(imgPoster);
        }

    }

    private void showPoster(String url){

       Target target = new Target() {
           @Override
           public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           FileOutputStream outputStream = new FileOutputStream(posterFile);
                           bitmap.compress(Bitmap.CompressFormat.JPEG,80, outputStream);
                           outputStream.flush();
                           outputStream.close();
                           Thread.sleep(200);
                           App.getHandler().post(new Runnable() {
                               @Override
                               public void run() {
                                   Picasso.with(App.getContext()).load(posterFile).into(imgPoster);
                               }
                           });
                       } catch (IOException e) {
                           Log.e("IOException", e.getMessage());
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               }).start();
           }

           @Override
           public void onBitmapFailed(Drawable errorDrawable) {

           }

           @Override
           public void onPrepareLoad(Drawable placeHolderDrawable) {

           }
       };
        imgPoster.setTag(target);
        if(url.startsWith("/")){
            url = url.substring(1);
        }
        Picasso
                .with(App.getContext())
                .load(App.SERVER_ADDRESS+url)
                .memoryPolicy(MemoryPolicy.NO_CACHE )
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(target);
    }

    private class RtlGridLayoutManager extends GridLayoutManager {

        public RtlGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public RtlGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public RtlGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override
        protected boolean isLayoutRTL(){
            return true;
        }
    }

    private int getLastMusicId(){
        int lastId = -1;
        Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS", null);
        while (cursor.moveToNext()) {
            lastId = Math.max(lastId,cursor.getInt(cursor.getColumnIndex("music_id")));
        }
        cursor.close();
        return lastId;
    }
}
