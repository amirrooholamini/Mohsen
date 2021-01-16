package project.activities;

import android.app.Activity;
import android.com.i3center.rooholamini.mohsen.App;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class CAppCompatActivity extends AppCompatActivity {


    @Override
    protected void onResume() {
        super.onResume();
        App.setCurrentActivity(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    public void setActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        actionBar.setElevation(0);
    }

    public void removeActionBar(){
//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
    }

    public void setActionBarTitle(String title){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    public static class Founder {
        private final Activity activity;
        private int[] features;
        private boolean noTitlebar;
        private boolean noActionbar;
        private boolean fullscreen;
        private int layoutId;
        private Object ui;

        public Founder(Activity activity) {
            this.activity = activity;
        }

        public Founder requestFeatures(int... features) {
            this.features = features;
            return this;
        }

        public Founder noTitlebar() {
            this.noTitlebar = true;
            return this;
        }

        public Founder noActionbar() {
            this.noActionbar = true;
            return this;
        }

        public Founder fullscreen() {
            this.fullscreen = true;
            return this;
        }

        public Founder contentView(@LayoutRes int layoutResID) {
            this.layoutId = layoutResID;
            return this;
        }

        public Founder extractUi(Object ui) {
            this.ui = ui;
            return this;
        }

        public Founder build() {
            if (features != null) {
                for (int feature : this.features) {
                    activity.getWindow().requestFeature(feature);
                }
            }

            if (noTitlebar) {
                activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }

            if (fullscreen) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

//            if (noActionbar) {
//                activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//
//                {
//                    ActionBar actionBar = activity.getActionBar();
//                    if (actionBar != null) {
//                        actionBar.hide();
//                    }
//                }
//
//                if (activity instanceof AppCompatActivity) {
//                    AppCompatActivity castedActivity = (AppCompatActivity) activity;
//                    android.support.v7.app.ActionBar actionBar = castedActivity.getSupportActionBar();
//                    if (actionBar != null) {
//                        actionBar.hide();
//                    }
//                }
//            }

            activity.setContentView(layoutId);

            // reflect ui elements
            {
                if (ui != null) {
                    Class clazz = ui.getClass();
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        String name = field.getName();
                        Class type = field.getType();
                        if (name.contains("$")) {
                            continue;
                        }
                        //Log.i("LOG", "name : " + name + "package : " + App.get().getPackageName());
                        int id = App.get().getResources().getIdentifier(name, "id", App.get().getPackageName());
                        try {
                            field.set(ui, activity.findViewById(id));
                        } catch (IllegalAccessException e) {
                            Log.i("LOG", "Error");
                            e.printStackTrace();
                        }
                    }
                }
            }

            return this;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
