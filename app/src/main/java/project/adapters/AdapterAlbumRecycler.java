package project.adapters;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import project.activities.ActivityMusicsList;
import project.helper.FileHelper;
import project.structures.StructAlbum;

public class AdapterAlbumRecycler extends RecyclerView.Adapter<AdapterAlbumRecycler.ViewHolder> {

    private ArrayList<StructAlbum> list;

    BitmapFactory.Options options = new BitmapFactory.Options();


    public AdapterAlbumRecycler(ArrayList<StructAlbum> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(App.getContext()).inflate(R.layout.struct_album, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final StructAlbum item = list.get(position);

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if(position == 0){
            ((LinearLayout.LayoutParams) params).setMargins(
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//left
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//top
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//right
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp));//bottom
        }else if(position == 1){
            ((LinearLayout.LayoutParams) params).setMargins(
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//left
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//top
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//right
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp));//bottom
        }

        else if(position %2 == 0) {
            ((LinearLayout.LayoutParams) params).setMargins(
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//left
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//top
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//right
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp));//bottom

        }else{
            ((LinearLayout.LayoutParams) params).setMargins(
                    (int) App.getContext().getResources().getDimension(R.dimen._10sdp),//left
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//top
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp),//right
                    (int) App.getContext().getResources().getDimension(R.dimen._5sdp));//bottom
        }


        holder.layoutRoot.setLayoutParams(params);

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    holder.layoutRoot.animate().scaleXBy(0.04F).setDuration(300).start();
                    holder.layoutRoot.animate().scaleYBy(0.04F).setDuration(300).start();
                    return true;
                }else if (action == MotionEvent.ACTION_UP) {
                    holder.layoutRoot.animate().cancel();
                    holder.layoutRoot.animate().scaleX(1f).setDuration(300).start();
                    holder.layoutRoot.animate().scaleY(1f).setDuration(300).start();
                    Intent intent = new Intent(App.getCurrentActivity(), ActivityMusicsList.class);
                    intent.putExtra("ALBUM_ID",item.id);
                    App.getCurrentActivity().startActivity(intent);
                    return true;
                }else if (action == MotionEvent.ACTION_CANCEL) {
                    holder.layoutRoot.animate().cancel();
                    holder.layoutRoot.animate().scaleX(1f).setDuration(300).start();
                    holder.layoutRoot.animate().scaleY(1f).setDuration(300).start();
                    return true;
                }
                return false;
            }

        };

        holder.layoutRoot.setOnTouchListener(touchListener);
        holder.txtTitle.setOnTouchListener(touchListener);
        holder.imgCover.setOnTouchListener(touchListener);
        holder.txtDate.setOnTouchListener(touchListener);

        holder.txtTitle.setText(item.name);
        holder.txtDate.setText(item.date + "");

        if (item.coverBitmap == null) {
            if (item.coverUrl != null) {
                final File musicCover = new File(App.DIR_COVER + "/" + FileHelper.getFileName(item.coverUrl).replace("_"," "));

                if (musicCover.exists()) {
                    Picasso.with(App.getContext()).load(musicCover).into(holder.imgCover);

                } else {
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FileOutputStream ostream = new FileOutputStream(musicCover);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                                        ostream.flush();
                                        ostream.close();
                                        App.getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Picasso.with(App.getContext()).load(musicCover).into(holder.imgCover);
                                            }
                                        });

                                    } catch (IOException e) {
                                        Log.e("IOException", e.getLocalizedMessage());
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
                    holder.imgCover.setTag(target);
                    Picasso.with(App.getContext()).load(item.coverUrl).into(target);
                    //Picasso.get().load(item.coverUrl).into(holder.imgCover);
                }
            }
        } else {
            holder.imgCover.setImageBitmap(item.coverBitmap);
        }


    }

    //target to saveg

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewGroup layoutRoot;
        public ImageView imgCover;
        public TextView txtTitle;
        public TextView txtDate;

        public ViewHolder(View view) {
            super(view);

            options.inSampleSize = 2;
            layoutRoot = (ViewGroup) view.findViewById(R.id.layoutRoot);
            imgCover = (ImageView) view.findViewById(R.id.imgCover);
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtDate = (TextView) view.findViewById(R.id.txtDate);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // layoutRoot.setBackgroundResource(R.drawable.musiclist_root_ripple);
            } else {
                //  layoutRoot.setBackgroundResource(R.drawable.musiclist_root_selection);
            }
        }
    }

}
