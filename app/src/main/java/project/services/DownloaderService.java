package project.services;

import android.app.Service;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import project.connections.Commands;
import project.structures.StructMusic;

public class DownloaderService extends Service {


    private static interface CheckStack {
        void downloadNewUrl(boolean canDownload);
    }

    private static boolean canUpdate = true;

    private static ArrayList<String> destinations;
    private static ArrayList<StructMusic> musics;
    private static ArrayList<Integer> positions;
    private static int runningThreads = 0;
    private static int maxThreads = 1;
    private int index = 0;

    private CheckStack checkStack;
    private static TextView textView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (musics == null) {
            return START_NOT_STICKY;
        }
        checkStack = new CheckStack() {
            @Override
            public void downloadNewUrl(boolean canDownload) {
                if (canDownload) {
                    while (runningThreads < maxThreads && index < musics.size()) {
                        runningThreads++;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String newUrl = musics.get(index).fileUrl;
                                String newDestinationPath = destinations.get(index);
                                int position = positions.get(index);
                                index++;

                                int copyIndex = index - 1;
                                downloadFile(newUrl, newDestinationPath, copyIndex);
                            }
                        }).start();
                    }

                }
                if (index >= musics.size()) {
                    destinations.clear();
                    positions.clear();
                    musics.clear();
                    index = 0;
                    canUpdate = true;
                    stopSelf();
                }
            }
        };

        // for first time :
        checkStack.downloadNewUrl(true);

        return START_NOT_STICKY;
    }


    public static void addToDownloadList(StructMusic music, String destinationPath, int position) {
        if (musics == null) {
            destinations = new ArrayList<String>();
            musics = new ArrayList<StructMusic>();
            positions = new ArrayList<Integer>();
        }

        if (musics.contains(music)) {
            return;
        }

        boolean founded = false;
        for(StructMusic m : musics){
            if(m.fileName.equals(music.fileName)){
                founded = true;
                break;
            }
        }
        if(founded){
            return;
        }

        musics.add(music);
        destinations.add(destinationPath);
        positions.add(position);
        musics.get(musics.size() - 1).changeState(App.getContext().getString(R.string.inStack), position);
    }

    public static void sincItems(ArrayList<StructMusic> list) {
        canUpdate = false;
        if (musics != null) {
            for (int i = 0; i < musics.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (musics.get(i).fileName.equals(list.get(j).fileName)) {
                        list.get(j).changeState(musics.get(i).getState(),j);
                        musics.set(i, list.get(j));
                    }

                }

            }
        }
        canUpdate = true;
    }


    private void downloadFile(String fileUrl, String destinationPath, final int idx) {

        if (canUpdate) {
            musics.get(idx).changeState(App.getContext().getString(R.string.downloading), positions.get(idx));
        }
        Commands downloader = new Commands();
        downloader.setProgressListener(new Commands.onCommandProgressListener() {
            @Override
            public void onProgress(float totalSize, float downloadedSize) {
                final String downloaded = String.format("%.1f", downloadedSize / (1048576));
                final String total = String.format("%.1f", totalSize / (1048576));
                String newState = downloaded + " / " + total;
                if (!App.fullMusics.get(positions.get(idx)).getState().equals(newState)) {
                    musics.get(idx).changeState(newState, positions.get(idx),downloadedSize,totalSize);
                }

            }

            @Override
            public void onComplete() {
                if (canUpdate) {
                    musics.get(idx).changeState(App.getContext().getString(R.string.completed), positions.get(idx));
                }
//                listener.onStateChanged(id,App.getContext().getString(R.string.completed));
                runningThreads--;
                if (runningThreads < maxThreads) {
                    checkStack.downloadNewUrl(true);

                }
            }

            @Override
            public void onFail(String error) {
                runningThreads--;
                if (canUpdate) {
                    musics.get(idx).changeState(App.getContext().getString(R.string.fail), positions.get(idx));
                }
//                listener.onStateChanged(id,App.getContext().getString(R.string.fail));
                checkStack.downloadNewUrl(true);
            }
        }).downloadFile(fileUrl, destinationPath);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        musics.clear();
        destinations.clear();
    }


}
