package com.stikhonenko.remoteplayer.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Tikhonenko.S on 20.09.13.
 */
public class Toasts {
    public static void message(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void message(Context context, int stringResourceId) {
        String message = context.getResources().getString(stringResourceId);
        message(context, message);
    }
}
