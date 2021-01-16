package project.adapters;

import android.app.ActivityManager;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import project.activities.ActivityMusicsList;
import project.activities.ActivityMusicDetails;
import project.custom.LinearProgressBar;
import project.helper.FileHelper;
import project.services.DownloaderService;
import project.structures.StructMusic;

public class AdapterMusicRecycler extends RecyclerView.Adapter<AdapterMusicRecycler.ViewHolder> implements Filterable {

    private OnDownloadNewMusicItems listener;
    private ArrayList<StructMusic> list;
    private ArrayList<String> names;

    public static boolean downloadingNewItems = false;
    int index = 1;


    private static final int TYPE_NOT_EXIST = 0;
    private static final int TYPE_EXIST = 1;

    BitmapFactory.Options options = new BitmapFactory.Options();


    public AdapterMusicRecycler(ArrayList<StructMusic> list) {
        this.list = list;
        names = new ArrayList<>();
        names.clear();
        for (StructMusic item : list) {
            names.add(item.fileName);
        }
    }

    public interface OnDownloadNewMusicItems {
        void downloading();
    }


    @Override
    public int getItemViewType(int position) {
        StructMusic music = list.get(position);
        if (music.exists) {
            return TYPE_EXIST;
        }
        return TYPE_NOT_EXIST;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int structId = R.layout.struct_music_exist;
        if (viewType == TYPE_NOT_EXIST) {
            structId = R.layout.struct_music_not_exist;
        }

        View view = LayoutInflater.from(App.getContext()).inflate(structId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        downloadingNewItems = false;

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final StructMusic item = list.get(position);
        holder.txtMusicName.setText(item.musicName);
        holder.txtRate.setText(item.rate + "");
        holder.ratingBar.setRating(item.rate);

        final File musicFile = new File(App.DIR_MUSIC + "/" + item.fileName);
        final File musicCover = new File(App.DIR_COVER + "/" + FileHelper.getFileName(item.coverUrl).replace("_", " "));
        if (item.coverUrl != null && item.coverUrl.contains("covers")) {
            if (musicCover.exists()) {
                item.coverBitmap = BitmapFactory.decodeFile(musicCover.getPath());
                holder.imgCover.setImageBitmap(item.coverBitmap);
//                Picasso.with(App.getContext()).load(musicCover).into(holder.imgCover);
            } else {
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        Log.i("LOGGG","onBitmapLoaded");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
//                                    Log.i("LOGGG","set bitmap");
                                    item.coverBitmap = bitmap;
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
                                    Log.e("LOGGG", e.getMessage());
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
//                Picasso.get().load(item.coverUrl).into(holder.imgCover);
            }
        }

        if (item.listener == null && holder.imgDownload != null) {
            item.listener = new StructMusic.OnStateChangeListener() {
                @Override
                public void onChange(final String newState, final int downloadPosition) {
                    int pos = -1;
                    pos = names.indexOf(App.fullMusics.get(downloadPosition).fileName);
                    if(pos==-1){
                        return;
                    }
                    final int finalPos = pos;
                    App.getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (newState.equals(App.getContext().getString(R.string.fail))) {
                                holder.txtState.setVisibility(View.GONE);
                                holder.imgDownload.setVisibility(View.VISIBLE);
                                notifyItemChanged(finalPos);
                            } else if (newState.equals(App.getContext().getString(R.string.completed))) {
                                list.get(finalPos).exists = true;
                                notifyItemChanged(finalPos);
                                App.fullMusics.get(position).exists = true;
                            } else {
                                notifyItemChanged(finalPos);
                            }
                        }
                    });
                }

                @Override
                public void onChange(final String newState, final int downloadPosition, final float downloaded, final float total) {
                    App.getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int pos = -1;
                            pos = names.indexOf(App.fullMusics.get(downloadPosition).fileName);
                            if(pos==-1){
                                return;
                            }
                            float progress = downloaded * 100 / total;
                            item.downloadPercentage = progress;
                            notifyItemChanged(pos, holder.txtState);
                        }
                    });
                }
            };
        }


        int type = holder.getItemViewType();
        switch (type) {
            case TYPE_EXIST:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.imgSetting.setBackgroundResource(R.drawable.music_setting_ripple);
                } else {
                    holder.imgSetting.setBackgroundResource(R.drawable.music_setting_selection);
                }

                holder.imgSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        App.selectedMusicIndex = item.fullMusicsIndex;
                        App.selectedMusic = item;
                        ActivityMusicsList.setSettingDialogTitle(item.musicName);
                        ActivityMusicsList.setSettingDialogCover(item.coverBitmap);
                        ActivityMusicsList.setAddToFavorites();
                        Cursor cursor = App.musicDatabase.rawQuery("SELECT * FROM MUSICS WHERE music_favorite = 1 AND music_name = '" + item.musicName + "'", null);
                        while (cursor.moveToNext()) {
                            ActivityMusicsList.setRemoveFromFavorites();
                        }

                        ActivityMusicsList.showSettingDialog();
                    }
                });

                break;

            case TYPE_NOT_EXIST:

                holder.progressBar.setBackgroundColor(Color.parseColor("#2a2a2a"));
                holder.progressBar.setProgressColor(Color.parseColor(App.getContext().getString(R.color.foregroundColor)));
                holder.progressBar.setProgress(item.downloadPercentage);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.imgDownload.setBackgroundResource(R.drawable.music_setting_ripple);
                } else {
                    holder.imgDownload.setBackgroundResource(R.drawable.music_setting_selection);
                }

                if (!item.getState().equals("") && !item.getState().equals(App.getContext().getString(R.string.fail))) {
                    holder.imgDownload.setVisibility(View.GONE);
                    holder.txtState.setVisibility(View.VISIBLE);
                } else {
                    holder.imgDownload.setVisibility(View.VISIBLE);
                    holder.txtState.setVisibility(View.GONE);
                }

                //holder.imgCover.setImageResource(R.drawable.default_music_cover);
                holder.txtState.setTypeface(App.persianFont);
                holder.txtState.setText(item.getState());

                holder.imgDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.imgDownload.setVisibility(View.INVISIBLE);
                        holder.txtState.setVisibility(View.VISIBLE);
                        downloadMusic(item);
                    }
                });

                break;
        }


        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicFile.exists()) {
                    Intent intent = new Intent(App.getCurrentActivity(), ActivityMusicDetails.class);
                    intent.putExtra("POSITION", position);
                    if (App.getCurrentActivity() instanceof ActivityMusicsList) {
                        intent.putExtra("ALL", true);
                    } else {
                        intent.putExtra("ALL", false);
                    }
                    App.getCurrentActivity().startActivity(intent);
                    App.getCurrentActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                } else {
                    holder.imgDownload.setVisibility(View.INVISIBLE);
                    holder.txtState.setVisibility(View.VISIBLE);
                    downloadMusic(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout layoutRoot;
        public TextView txtMusicName;
        public TextView txtState;
        public TextView txtRate;
        public ImageView imgCover;
        public ImageView imgSetting;
        public ImageView imgDownload;
        public RatingBar ratingBar;
        public LinearProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);

            options.inSampleSize = 2;
            layoutRoot = (ConstraintLayout) view.findViewById(R.id.layoutRoot);
            txtMusicName = (TextView) view.findViewById(R.id.txtMusicName);
            txtState = (TextView) view.findViewById(R.id.txtState);
            txtRate = (TextView) view.findViewById(R.id.txtRate);
            imgCover = (ImageView) view.findViewById(R.id.imgCover);
            imgDownload = (ImageView) view.findViewById(R.id.imgDownload);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
            imgSetting = (ImageView) view.findViewById(R.id.imgSetting);
            progressBar = (LinearProgressBar) view.findViewById(R.id.progressBar);

            txtRate.setTypeface(App.persianFont);

            txtMusicName.setTypeface(App.persianFont);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                layoutRoot.setBackgroundResource(R.drawable.musiclist_root_ripple);
            } else {
                layoutRoot.setBackgroundResource(R.drawable.musiclist_root_selection);
            }
        }
    }

    private void downloadMusic(StructMusic music) {
        final String fileName = FileHelper.getFileName(music.fileUrl);

        DownloaderService.addToDownloadList(
                music,
                App.DIR_MUSIC + "/" + fileName,
                music.fullMusicsIndex);
        if (!isDownloaderServiceRunning()) {
            App.downloadServiceIntent = new Intent(App.getCurrentActivity(), DownloaderService.class);
            App.getCurrentActivity().startService(App.downloadServiceIntent);
        }
    }


    private boolean isDownloaderServiceRunning() {
        ActivityManager manager = (ActivityManager) App.getContext().getSystemService(App.getContext().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DownloaderService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<StructMusic> filteredList = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(App.fullClassMusics);


            } else {
                String filterWord = charSequence.toString();
                for (StructMusic music : App.fullClassMusics) {
                    if (music.musicName.contains(filterWord)) {
                        filteredList.add(music);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }


        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            names.clear();
            list.addAll((ArrayList) filterResults.values);
            for (StructMusic item : list) {
                names.add(item.fileName);
            }
            DownloaderService.sincItems(list);
            notifyDataSetChanged();
        }
    };


}
