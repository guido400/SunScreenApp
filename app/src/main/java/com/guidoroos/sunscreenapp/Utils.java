package com.guidoroos.sunscreenapp;

import android.content.Context;
import android.widget.Toast;

public class Utils {


    // tag for logging purpose
    private static final String TAG =
            Utils.class.getCanonicalName();

    // make sure not instance created
    private Utils() {
        throw new AssertionError();
    }

    //show toast message
    public static void showToast(Context context,
                                 String message) {
        Toast.makeText(context,
                message,
                Toast.LENGTH_SHORT).show();
    }



}

