package project.fragments;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import project.adapters.AdapterVideoRecycler;
import project.connections.Commands;
import project.structures.StructVideo;

public class PagerFragmentMusicVideo extends Fragment {

    private static RecyclerView lstVideo;

    public static AdapterVideoRecycler adapter;

    private Commands readVideos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_videos,container,false);

        lstVideo = (RecyclerView) rootView.findViewById(R.id.lstVideo);

        App.videos = new ArrayList<StructVideo>();
        for(int i=0;i<10;i++){
            StructVideo video = new StructVideo();
            video.videoTitle = "میکس جدید";
            video.videoCity = "مشهد";
            video.videoDate = "99/02/16";
            video.rate = 4.0f;
            App.videos.add(video);
        }

        adapter = new AdapterVideoRecycler(App.videos);
        lstVideo.setLayoutManager(new LinearLayoutManager(App.getContext()));

        lstVideo.setAdapter(adapter);

        return rootView;
    }

}

