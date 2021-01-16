package project.activities;

import project.connections.Commands;
import project.custom.CustomLoader;
import project.helper.PermissionManager;
import project.structures.StructAlbum;

import android.Manifest;
import android.app.AlertDialog;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ActivityStart extends CAppCompatActivity {

    private CustomLoader loader;
    private PermissionManager permissinManager;
    private int lastAlbumId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeActionBar();
        setContentView(R.layout.activity_start);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestPermission();
        loader = (CustomLoader) findViewById(R.id.loader);
        TextView txtName = (TextView) findViewById(R.id.txtEmpty);

        loader.color(getResources().getColor(R.color.golden)).width(3);
        txtName.setText("MOHSEN");
        txtName.setTypeface(App.englishFont);

    }

    private void requestPermission() {
        permissinManager = new PermissionManager(ActivityStart.this);
        permissinManager.setOnPermissionGranted(new PermissionManager.OnPermissionGranted() {
            @Override
            public void onGranted() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                            new File(App.DIR_MUSIC).mkdirs();
                            new File(App.DIR_VIDEO).mkdirs();
                            new File(App.DIR_COVER).mkdirs();
                            new File(App.DIR_DATABASE).mkdirs();
                            App.createDatabases();
                            readFromCache();
                            readFromNet();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();


            }
        }).setOnPermissionDenied(new PermissionManager.OnPermissionDenied() {
            @Override
            public void onDenied() {
                new AlertDialog.Builder(ActivityStart.this)
                        .setTitle(getString(R.string.permission_required))
                        .setMessage(getString(R.string.permission_required_text))
                        .setPositiveButton(getString(R.string.ask_again), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermission();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        }).create().show();
            }
        }).request(Manifest.permission.WRITE_EXTERNAL_STORAGE);


    }

    private void readFromNet() {
        new Commands().setCompleteListener(new Commands.onCommandCompleteListener() {
            @Override
            public void onComplete(String data) {
                loader.stopRotation();
                goToMain();
            }

            @Override
            public void onComplete() {
                loader.stopRotation();
                goToMain();

            }

            @Override
            public void onFail(String error) {
                loader.stopRotation();
                readFromCache();
                if (App.albums.size() > 0) {
                    goToMain();
                } else {
                    showDialog();
                }

            }
        }).readAlbums(lastAlbumId);
    }


    private void readFromCache() {
        App.albums = new ArrayList<StructAlbum>();
        App.albums.clear();
        Cursor cursor = App.albumDatabase.rawQuery("SELECT * FROM ALBUMS WHERE album_id = 0", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("album_id"));
            lastAlbumId = Math.max(lastAlbumId,id);
            String name = cursor.getString(cursor.getColumnIndex("album_name"));
            String date = cursor.getString(cursor.getColumnIndex("album_date"));
            String coverUrl = cursor.getString(cursor.getColumnIndex("album_cover_url"));
            StructAlbum album = new StructAlbum();
            album.name = name;
            album.coverUrl = coverUrl;
            album.date = date;
            album.coverUrl = coverUrl;
            album.id = id;
            App.albums.add(album);
        }

        cursor = App.albumDatabase.rawQuery("SELECT * FROM ALBUMS WHERE album_id <> 0 ORDER BY album_id DESC", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("album_id"));
            lastAlbumId = Math.max(lastAlbumId,id);
            String name = cursor.getString(cursor.getColumnIndex("album_name"));
            String date = cursor.getString(cursor.getColumnIndex("album_date"));
            String coverUrl = cursor.getString(cursor.getColumnIndex("album_cover_url"));
            StructAlbum album = new StructAlbum();
            album.name = name;
            album.coverUrl = coverUrl;
            album.date = date;
            album.coverUrl = coverUrl;
            album.id = id;
            App.albums.add(album);
        }
        cursor.close();

    }

    private void goToMain() {
        Intent intent = new Intent(App.getCurrentActivity(), ActivityAlbumsList.class);
        App.getCurrentActivity().startActivity(intent);
        finish();
    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(App.getCurrentActivity())
                .setTitle(getString(R.string.noInternetConnection))
                .setMessage(getString(R.string.youNeedNetwork))
                .setNeutralButton(getString(R.string.reTry), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loader.startRotation();
                        readFromNet();
                    }
                })
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissinManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
