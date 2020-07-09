package com.guidoroos.sunscreenapp;

/**
 * This abstract class extends the Activity class and overrides
 * lifecycle callbacks for logging various lifecycle events.
 * class is borrowed from coursera android development course
 */


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public abstract class LifecycleLoggingActivity
        extends Activity {
    // tag for logging purpose
    protected final String TAG =
            getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // The activity is being re-created. Use the
            // savedInstanceState bundle for initializations either
            // during onCreate or onRestoreInstanceState().
            Log.d(TAG, "onCreate(): activity re-created");

        } else {
            // Activity is being created anew. No prior saved
            // instance state information available in Bundle object.
            Log.d(TAG, "onCreate(): activity created anew");
        }

    }


    @Override
    protected void onStart() {
        // Always call super class for necessary
        // initialization/implementation.
        super.onStart();
        Log.d(TAG, "onStart() - the activity is about to become visible");
    }


    @Override
    protected void onResume() {
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        super.onResume();
        Log.d(TAG,
                "onResume() - the activity has become visible (it is now \"resumed\")");
    }


    @Override
    protected void onPause() {
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        super.onPause();
        Log.d(TAG,
                "onPause() - another activity is taking focus (this activity is about to be \"paused\")");
    }


    @Override
    protected void onStop() {
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        super.onStop();
        Log.d(TAG,
                "onStop() - the activity is no longer visible (it is now \"stopped\")");
    }


    @Override
    protected void onRestart() {
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        super.onRestart();
        Log.d(TAG, "onRestart() - the activity is about to be restarted()");
    }


    @Override
    protected void onDestroy() {
        // Always call super class for necessary
        // initialization/implementation and then log which lifecycle
        // hook method is being called.
        super.onDestroy();
        Log.d(TAG, "onDestroy() - the activity is about to be destroyed");
    }

}

