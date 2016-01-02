package com.stikhonenko.remoteplayer.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.stikhonenko.remoteplayer.R;

/**
 * Created by stikhonenko on 1/3/16.
 */
public class PermissionUtils {
    public static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void requestReadStoragePermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toasts.message(activity, R.string.read_storage_permission_description);
        }

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_STORAGE_PERMISSION_REQUEST_CODE);
    }

    public static boolean shouldRequestReadStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        return ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED;
    }

}
