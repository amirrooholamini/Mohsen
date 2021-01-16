package project.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import custom.CustomRatingBar;
import project.adapters.AdapterSlider;
import project.connections.Commands;
import project.custom.CustomToast;
import project.custom.LinearLoader;
import project.fragments.PagerFragmentMusic;
import project.helper.FileHelper;
import project.helper.PermissionManager;
import project.structures.StructMusic;

public class ActivityMusicsList extends CAppCompatActivity {


    public static int pagerPosition = 1;
    public static int albumId = -1;
    public static OnSearchMusic musicSearchListener;

    private static Dialog settingDialog;
    private static Dialog ratingDialog;
    private static TextView settingDialogTitle;
    private static TextView ratingDialogTitle;
    private static ImageView settingDialogImgCover;
    private static ImageView ratingDialogImgCover;
    private static Button btnFavorite;

    private ConstraintLayout root;
    private CustomRatingBar ratingBar;
    private Button btnSetRate;
    private boolean clickEnable = true;
    private LinearLoader loader;

    private PermissionManager permissinManager;

    public interface OnSearchMusic {
        void onSearched(String s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setActionBarTitle("Archive");

        setContentView((R.layout.activity_musics_list));
        clickEnable = true;

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (intent.hasExtra("ALBUM_ID")) {
            albumId = extras.getInt("ALBUM_ID");
        }

        root = (ConstraintLayout) findViewById(R.id.root);
        ViewPager slidingPager = (ViewPager) findViewById(R.id.slidingPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        final AdapterSlider adapter = new AdapterSlider(getSupportFragmentManager());
        // adapter.addFragment(new PagerFragmentMusicVideo(),getString(R.string.video));
        adapter.addFragment(new PagerFragmentMusic(), getString(R.string.music));
        slidingPager.setAdapter(adapter);
        slidingPager.setCurrentItem(adapter.getNumberOfItems() - 1);
        tabLayout.setupWithViewPager(slidingPager, true);


        slidingPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
            }
        });

        settingDialog = new Dialog(ActivityMusicsList.this);
        settingDialog.setCanceledOnTouchOutside(true);
        settingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        settingDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingDialog.setContentView(R.layout.dialog_setting);
        Window window = settingDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        settingDialog.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingDialog.dismiss();
            }
        });
        settingDialogTitle = (TextView) settingDialog.findViewById(R.id.txtName);
        settingDialogImgCover = (ImageView) settingDialog.findViewById(R.id.imgCover);
        final Button btnRate = (Button) settingDialog.findViewById(R.id.btnRate);
        Button btnRemove = (Button) settingDialog.findViewById(R.id.btnRemove);
        Button btnSave = (Button) settingDialog.findViewById(R.id.btnSave);
        btnFavorite = (Button) settingDialog.findViewById(R.id.btnFavorite);

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingDialog.dismiss();
                ratingBar.setRating(Math.round(App.selectedMusic.rate));
                ratingDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingDialog.dismiss();
                StructMusic selectedMusic = App.fullMusics.get(App.selectedMusicIndex);
                String name = selectedMusic.fileName;
                String musicPath = App.DIR_MUSIC + "/" + name;
                String destinationPath = App.DIR_PUBLIC_MUSIC+"/"+name;
                FileHelper.copyFile(musicPath,destinationPath);
                File musicFile = new File(destinationPath);
                MediaScannerConnection.scanFile(App.getContext(), new String[] { musicFile.getPath() }, new String[] { "media/mp3" }, null);
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StructMusic selectedMusic = App.fullMusics.get(App.selectedMusicIndex);
                if (selectedMusic.isFavorite) {
                    App.fullMusics.get(App.selectedMusicIndex).isFavorite = false;
                    App.musicDatabase.execSQL
                            ("UPDATE MUSICS SET music_favorite = 0 " +
                                    "WHERE music_id = " +
                                    selectedMusic.musicID);
                    Toast.makeText(App.getContext(), "\"" + selectedMusic.musicName + "\" " + getString(R.string.removedFromFavorites), Toast.LENGTH_SHORT).show();
                } else {
                    App.fullMusics.get(App.selectedMusicIndex).isFavorite = true;
                    App.musicDatabase.execSQL
                            ("UPDATE MUSICS SET music_favorite = 1 " +
                                    "WHERE music_id = " +
                                    selectedMusic.musicID);
                    Toast.makeText(App.getContext(), "\"" + selectedMusic.musicName + "\" " + getString(R.string.addedToFavorites), Toast.LENGTH_SHORT).show();
                }
                PagerFragmentMusic.adapter.notifyDataSetChanged();
                settingDialog.dismiss();
            }
        });


        ratingDialog = new Dialog(ActivityMusicsList.this);
        ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ratingDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        ratingDialog.setContentView(R.layout.dialog_rating);
        window = ratingDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        ratingDialog.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDialog.dismiss();
            }
        });
        ratingBar = (CustomRatingBar) ratingDialog.findViewById(R.id.ratingBar);
        ratingBar.userCanChangeRate(true);
        final TextView txtRate = (TextView) ratingDialog.findViewById(R.id.txtRate);
        ratingDialogTitle = (TextView) ratingDialog.findViewById(R.id.txtName);
        ratingDialogImgCover = (ImageView) ratingDialog.findViewById(R.id.imgCover);
        loader = (LinearLoader) ratingDialog.findViewById(R.id.loader);
        ratingBar.setOnRateChangeListener(new CustomRatingBar.OnRateChangeListener() {
            @Override
            public void onRateChanged(float rate, boolean byUser) {
                if (rate <= 1) {
                    txtRate.setText(getString(R.string.rate1));
                } else if (rate > 1 && rate <= 2) {
                    txtRate.setText(getString(R.string.rate2));
                } else if (rate > 2 && rate <= 3) {
                    txtRate.setText(getString(R.string.rate3));
                } else if (rate > 3 && rate <= 4) {
                    txtRate.setText(getString(R.string.rate4));
                } else {
                    txtRate.setText(getString(R.string.rate5));
                }
            }
        });
        btnSetRate = (Button) ratingDialog.findViewById(R.id.btnSetRate);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnFavorite.setBackgroundResource(R.drawable.dialog_buttons_ripple);
            btnRate.setBackgroundResource(R.drawable.dialog_buttons_ripple);
            btnRemove.setBackgroundResource(R.drawable.dialog_buttons_ripple);
            btnSetRate.setBackgroundResource(R.drawable.dialog_buttons_ripple);
            btnSave.setBackgroundResource(R.drawable.dialog_buttons_ripple);
        } else {
            btnFavorite.setBackgroundResource(R.drawable.dialog_buttons_selection);
            btnRate.setBackgroundResource(R.drawable.dialog_buttons_selection);
            btnRemove.setBackgroundResource(R.drawable.dialog_buttons_selection);
            btnSetRate.setBackgroundResource(R.drawable.dialog_buttons_selection);
            btnSave.setBackgroundResource(R.drawable.dialog_buttons_selection);
        }


        btnSetRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!clickEnable) {
                    CustomToast.showToast(getString(R.string.pleaseWait));
                    return;
                }
                loader.start();
                clickEnable = false;
                phoneStateRequestPermission();
                setRate();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getString(R.string.search));
        searchView.setDrawingCacheBackgroundColor(Color.BLACK);
        searchView.setGravity(Gravity.RIGHT);
        searchView.setDrawingCacheBackgroundColor(Color.parseColor("#dd55ff"));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String filter) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicSearchListener != null) {
                            musicSearchListener.onSearched(filter);
                        }
                    }
                }).start();

                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);


    }


    public static void showSettingDialog() {
        settingDialog.show();
    }

    public static void setSettingDialogTitle(String newTitle) {
        settingDialogTitle.setText(newTitle);
        ratingDialogTitle.setText(newTitle);

    }

    public static void setRemoveFromFavorites() {
        btnFavorite.setText(App.getContext().getString(R.string.removeFromFavorites));
    }

    public static void setAddToFavorites() {
        btnFavorite.setText(App.getContext().getString(R.string.addToFavorites));
    }

    public static void setSettingDialogCover(Bitmap bitmap) {
        settingDialogImgCover.setImageBitmap(bitmap);
        ratingDialogImgCover.setImageBitmap(bitmap);
//        final File musicCover = new File(App.DIR_COVER + "/" + App.fullMusics.get(App.selectedMusicIndex).fileName);
//        if (musicCover.exists()) {
//            Picasso.with(App.getContext()).load(musicCover).into(settingDialogImgCover);
//            Picasso.with(App.getContext()).load(musicCover).into(ratingDialogImgCover);
//
//        } else {
//            Target target = new Target() {
//                @Override
//                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                FileOutputStream ostream = new FileOutputStream(musicCover);
//                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
//                                ostream.flush();
//                                ostream.close();
//                                App.getHandler().post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Picasso.with(App.getContext()).load(musicCover).into(settingDialogImgCover);
//                                        Picasso.with(App.getContext()).load(musicCover).into(ratingDialogImgCover);
//                                    }
//                                });
//
//                            } catch (IOException e) {
//                                Log.e("IOException", e.getLocalizedMessage());
//                            }
//                        }
//                    }).start();
//                }
//
//                @Override
//                public void onBitmapFailed(Drawable errorDrawable) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            };
//            ratingDialogImgCover.setTag(target);
//            settingDialogImgCover.setTag(target);
//            Picasso.with(App.getContext()).load(App.fullMusics.get(App.selectedMusicIndex).coverUrl).into(target);
//            //Picasso.get().load(item.coverUrl).into(holder.imgCover);
//        }
    }

    public void phoneStateRequestPermission() {
        permissinManager = new PermissionManager(App.getCurrentActivity());
        permissinManager.setOnPermissionGranted(new PermissionManager.OnPermissionGranted() {
            @Override
            public void onGranted() {
                App.IMEI = App.getIMEI();
            }
        }).setOnPermissionDenied(new PermissionManager.OnPermissionDenied() {
            @Override
            public void onDenied() {
                new AlertDialog.Builder(App.getCurrentActivity())
                        .setTitle(getString(R.string.permission_required))
                        .setMessage(getString(R.string.IMEI_permission_required_text))
                        .setPositiveButton(getString(R.string.ask_again), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                phoneStateRequestPermission();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                clickEnable = true;
                                loader.stop();
                            }
                        }).create().show();
            }
        }).request(Manifest.permission.READ_PHONE_STATE);
    }

    private void setRate() {
        clickEnable = false;
        float rate = ratingBar.getRate();
        new Commands().setCompleteListener(new Commands.onCommandCompleteListener() {
            @Override
            public void onComplete(String data) {
                clickEnable = true;
                loader.stop();
                String message = "";
                message += getString(R.string.yourRate);
                message += " ";
                message += App.selectedMusic.musicName;
                message += " ";
                if (data.equals("changed")) {
                    message += getString(R.string.successfullyChanged);
                    CustomToast.showToast(message);
                } else {
                    message += getString(R.string.successfullyRegistered);
                    CustomToast.showToast(message);
                }
            }

            @Override
            public void onComplete() {
                clickEnable = true;
                loader.stop();
            }


            @Override
            public void onFail(String error) {
                CustomToast.showToast(getString(R.string.noInternetConnection));
                clickEnable = true;
                loader.stop();
            }
        }).setRate(rate, App.selectedMusic.musicID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissinManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
