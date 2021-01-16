package project.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private static ArrayList<PermissionManager> requests = new ArrayList<>();
    private static boolean isFirstPermission = true;
    private static int lastRequestCode = 10;
    private static int requestCode;

    private boolean isFinished;
    private OnPermissionGranted permissionGranted;
    private OnPermissionDenied permissionDenied;
    private String permission;

    public static boolean checkPermission(Activity activity, String permission){
        int permissionCheck = ContextCompat.checkSelfPermission(activity,permission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


    public interface OnPermissionGranted {
        void onGranted();
    }

    public interface OnPermissionDenied {
        void onDenied();
    }

    private Activity activity;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }


    public PermissionManager setOnPermissionGranted(OnPermissionGranted permissionGranted) {
        this.permissionGranted = permissionGranted;
        return this;
    }

    public PermissionManager setOnPermissionDenied(OnPermissionDenied permissionDenied) {
        this.permissionDenied = permissionDenied;
        return this;
    }

    public void request(String permission) {
        this.permission = permission;
        lastRequestCode += 1;
        requests.add(this);
        requestCode = lastRequestCode;
        if(isFirstPermission){
            isFirstPermission = false;
            requestNextPermission();
        }
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for(PermissionManager request : requests){
            if (request.requestCode == requestCode) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (request.permissionGranted != null) {
                        request.permissionGranted.onGranted();
                    }
                } else {
                    if (request.permissionDenied != null) {
                        request.permissionDenied.onDenied();
                    }
                }
                request.isFinished = true;
                requestNextPermission();
            }
        }
    }

    private static void requestNextPermission(){
        for(PermissionManager request : requests) {
            if (!request.isFinished) {
                int state = ActivityCompat.checkSelfPermission(request.activity, request.permission);
                if (state == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(request.activity, new String[]{request.permission}, requestCode);
                }else if(state == PackageManager.PERMISSION_GRANTED){
                    if(request.permissionGranted !=null){
                        request.isFinished = true;
                        request.permissionGranted.onGranted();
                        requestNextPermission();
                    }
                }
                return;
            }
        }
        isFirstPermission = true;
        requests.clear();
    }
}
