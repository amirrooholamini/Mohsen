package project.adapters;

import android.app.ActivityManager;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import project.custom.LinearProgressBar;
import project.helper.FileHelper;
import project.services.DownloaderService;
import project.structures.StructMusic;
import project.structures.StructVideo;

public class AdapterVideoRecycler extends RecyclerView.Adapter<AdapterVideoRecycler.ViewHolder> {

    private ArrayList<StructVideo> list;
    private  boolean animationTime = false;

    public AdapterVideoRecycler(ArrayList<StructVideo> list) {
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int structId = R.layout.struct_video_not_exist;

        View view = LayoutInflater.from(App.getContext()).inflate(structId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final StructVideo item = list.get(position);


        holder.txtTitle.setText(item.videoTitle);
        holder.txtCity.setText(item.videoCity);
        holder.txtDate.setText(item.videoDate);
        holder.txtRate.setText(item.rate + "");
        holder.ratingBar.setRating(item.rate);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(animationTime){
                    return;
                }
                animationTime = true;
                Animation fadeIn = AnimationUtils.loadAnimation(App.getContext(),android.R.anim.fade_in);
                Animation fadeOut = AnimationUtils.loadAnimation(App.getContext(),android.R.anim.fade_out);


                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        animationTime = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
        });

        holder.imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        try {
                            while (i<100) {
                                Thread.sleep(100);
                                holder.downloadProgress.setProgress(i);
                                i++;
                                Log.i("LOG","i : "+i);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });


        if (!item.getState().equals("")) {
            holder.imgDownload.setVisibility(View.GONE);
        }


    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearProgressBar downloadProgress;
        public ViewGroup root;
        public TextView txtTitle;
        public TextView txtDate;
        public TextView txtCity;
        public TextView txtRate;
        public ImageView imgCover;
        public ImageView imgDownload;
        public RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);

            downloadProgress = (LinearProgressBar)view.findViewById(R.id.downloadProgress);
            root = (ViewGroup) view.findViewById(R.id.root);
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            txtCity = (TextView) view.findViewById(R.id.txtCity);
            txtRate = (TextView) view.findViewById(R.id.txtRate);
            imgCover = (ImageView) view.findViewById(R.id.imgCover);
            imgDownload = (ImageView) view.findViewById(R.id.imgDownload);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

            txtDate.setTypeface(App.persianFont);
            txtCity.setTypeface(App.persianFont);
            txtRate.setTypeface(App.persianFont);
            txtTitle.setTypeface(App.persianFont);

        }
    }

    private void downloadMusic(StructMusic music, int position) {
        final String fileName = FileHelper.getFileName(music.fileUrl);

//        DownloaderService.addToDownloadList(
//                music.fileUrl,
//                App.DIR_MUSIC + "/" + fileName,
//                music,
//                position);
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


}
