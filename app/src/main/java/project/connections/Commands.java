package project.connections;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import project.custom.CustomToast;
import project.helper.FileHelper;
import project.helper.HelperCalendar;
import project.helper.HelperString;
import project.structures.StructAlbum;
import project.structures.StructMusic;

import project.connection.WebServiceModule;

public class Commands {

    public static ArrayList<String> coverUrls = new ArrayList<String>();

    private boolean invalidData = false;

    private onCommandCompleteListener completeListener;
    private onCommandProgressListener progressListener;
    private onStreamRecievedListener streamListener;
    private WebServiceModule musicsReader;

    private OnDisconnect state;

    private interface OnDisconnect {
        void disconnect();
    }


    public interface onCommandCompleteListener {
        void onComplete(String data);

        void onComplete();

        void onFail(String error);
    }

    public interface onStreamRecievedListener {
        void onRecieved(InputStream inputStream);
    }

    public interface onCommandProgressListener {
        void onProgress(float totalSize, float downloadedSize);

        void onComplete();

        void onFail(String error);
    }

    public Commands setProgressListener(onCommandProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public Commands setCompleteListener(onCommandCompleteListener completeListener) {
        this.completeListener = completeListener;
        return this;
    }

    public Commands setStreamListener(onStreamRecievedListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }


    public void setRate(float rate,int musicId){
        WebServiceModule rating = new WebServiceModule();
        rating.url(App.SERVER_ADDRESS+"/mohsen/mohsen_service.php")
                .params("username", "farazist_mohsenyeganeh",
                        "password", "amirmohsen7364",
                        "action", "setRate",
                        "rate",rate+"",
                        "imei",App.getIMEI(),
                        "music_id",musicId+"")
                .listener(new WebServiceModule.Listener() {
                    @Override
                    public void onSuccess(String data) {
                        if(completeListener!=null){
                            completeListener.onComplete(data);
                        }
                    }

                    @Override
                    public void onFail(String error) {
                        if(completeListener!=null){
                            completeListener.onFail(error);
                        }
                    }
                }).connect();
    }

    public void getPosterInfo() {
        WebServiceModule posterReader = new WebServiceModule();
        posterReader.url(App.SERVER_ADDRESS + "/mohsen/mohsen_service.php")
                .params("username", "farazist_mohsenyeganeh",
                        "password", "amirmohsen7364",
                        "action", "posterInfo")
                .listener(new WebServiceModule.Listener() {
                    @Override
                    public void onSuccess(String data) {
                        if (completeListener != null) {
                            completeListener.onComplete(data);
                        }
                    }

                    @Override
                    public void onFail(String error) {
                        if (completeListener != null) {
                            completeListener.onFail(error);
                        }
                    }
                }).connect();
    }

    public void readAlbums(final int lastId) {
        WebServiceModule albumsReader = new WebServiceModule();
        albumsReader
                .url(App.SERVER_ADDRESS + "mohsen/mohsen_service.php/")
                .params("username", "farazist_mohsenyeganeh",
                        "password", "amirmohsen7364",
                        "action", "albums",
                        "lastId", lastId+"")
                .listener(new WebServiceModule.Listener() {
                    @Override
                    public void onSuccess(String data) {
                        try {
                            JSONArray albums = new JSONArray(data);
                            for (int i = 0; i < albums.length(); i++) {
                                JSONObject album = albums.getJSONObject(i);
                                StructAlbum item = new StructAlbum();
                                item.id = album.getInt("id");
                                item.name = album.getString("name");
                                if (i == 0) {
                                    item.date = HelperString.getTransformedDate(album.getString("date").split(" ")[0]);
                                } else {
                                    item.date = album.getString("date");
                                }
                                item.coverUrl = App.SERVER_ADDRESS + "/" + album.getString("coverUrl");
                                Cursor cursor = App.albumDatabase.rawQuery(
                                        "SELECT * FROM ALBUMS WHERE album_id = " + item.id,
                                        null);
                                if (cursor.getCount() == 0) {
                                    App.albumDatabase.execSQL(
                                            "INSERT INTO ALBUMS VALUES(" +
                                                    item.id + " , '" +
                                                    item.name + "' , '" +
                                                    item.date + "' , '" +
                                                    item.coverUrl + "')");
                                    App.albums.add(item);
                                } else {
                                    App.albumDatabase.execSQL(
                                            "UPDATE ALBUMS SET " +
                                                    "album_name = '" + item.name + "' ," +
                                                    "album_date = '" + item.date + "' ," +
                                                    "album_cover_url = '" + item.coverUrl + "' " +
                                                    "WHERE album_id = " + item.id);
                                    for (int j = 0; j < App.albums.size(); j++) {
                                        if (App.albums.get(j).id == item.id) {
                                            App.albums.get(j).coverUrl = item.coverUrl;
                                            App.albums.get(j).name = item.name;
                                            App.albums.get(j).date = item.date;
                                        }
                                    }

                                }
                                cursor.close();
                            }
                            if (completeListener != null) {
                                completeListener.onComplete();
                            }
                        } catch (JSONException e) {
                            if (completeListener != null) {

                                completeListener.onFail(e.getMessage());
                            }
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onFail(String error) {
                        if (completeListener != null) {
                            completeListener.onFail(error);
                        }
                    }
                }).connect();
    }


    public void readMusics(final int lastId) {

        state = new OnDisconnect() {
            @Override
            public void disconnect() {
                App.fullMusics.clear();
            }
        };
        invalidData = false;
        musicsReader = new WebServiceModule();
        musicsReader.url(App.SERVER_ADDRESS + "/mohsen/mohsen_service.php/")
                .params("username", "farazist_mohsenyeganeh",
                        "password", "amirmohsen7364",
                        "action", "musics",
                        "lastId", lastId + "")
                .listener(new WebServiceModule.Listener() {
                    @Override
                    public void onSuccess(String data) {
                        try {
                            JSONArray array = new JSONArray(data);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                int id = item.getInt("music_id");
                                int albumId = item.getInt("album_id");
                                String name = item.getString("music_name");
                                String fileUrl = App.SERVER_ADDRESS + item.getString("music_url");
                                String coverUrl = App.SERVER_ADDRESS + item.getString("music_coverUrl");
//                                float rate = (float) item.getDouble("music_rate");
                                float rate = 3.5f;
                                Cursor cursor = App.musicDatabase.rawQuery(
                                        "SELECT * FROM MUSICS WHERE music_id = " + id, null);

                                if (cursor.getCount() == 0) {
                                    App.musicDatabase.execSQL("INSERT INTO MUSICS VALUES(" +
                                            id + " , " +
                                            albumId + " , '" +
                                            name + "' , " +
                                            rate + " , '" +
                                            coverUrl + "' , '" +
                                            fileUrl + "'" +
                                            " , 0 )");
                                } else {
                                    App.musicDatabase.execSQL(
                                            "UPDATE MUSICS SET " +
                                                    "music_name = '" + name + "' ," +
                                                    "album_id = " + albumId + " ," +
                                                    "music_rate = " + rate + " ," +
                                                    "music_coverUrl = '" + coverUrl + "' ," +
                                                    "music_fileUrl = '" + fileUrl + "' " +
                                                    "WHERE music_id = " + id);
                                }
                                cursor.close();
                                StructMusic music = new StructMusic(id, albumId, name, rate, coverUrl, fileUrl, false);
                               // App.fullMusics.add(music);
                            }
                            if (completeListener != null) {
                                completeListener.onComplete();

                            }

                        } catch (JSONException e) {
                            Log.i("LOGGG", e.getMessage());
                            if (completeListener != null) {
                                completeListener.onFail("JSONException");
                            }
                            e.printStackTrace();
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onFail(String error) {
                        if (completeListener != null) {
                            completeListener.onFail(error);
                        }
                    }
                }).connect();
    }

    //
    public void downloadFile(final String fileUrl, final String destinationPath) {
        new DownloadManager(fileUrl, destinationPath)
                .progressListener(new DownloadManager.OnDownloadProgressListener() {
                    @Override
                    public void onProgress(final float totalSize, final float downloadedSize) {
                        if (progressListener != null) {
                            progressListener.onProgress(totalSize, downloadedSize);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (completeListener != null) {
                            completeListener.onComplete();
                        }
                        if (progressListener != null) {
                            progressListener.onComplete();
                        }

                    }

                    @Override
                    public void onFail(final String error) {
                        if (progressListener != null) {
                            progressListener.onFail(error);
                            return;
                        }
                        if (completeListener != null) {
                            completeListener.onFail(error);
                            return;
                        }
                    }
                }).resumeAbility(true).download();
    }

    public void getCoverBitmap(String fileUrl) {
        new DownloadManager(fileUrl)
                .streamListener(new DownloadManager.OnStreamReceivedListener() {
                    @Override
                    public void onStreamReceived(InputStream stream) {
                        if (streamListener != null) {
                            streamListener.onRecieved(stream);
                        }
                    }
                }).getStream();

    }

    public void closeConnection() {
        if (musicsReader != null) {
            invalidData = true;
            state.disconnect();
        }
    }

}
