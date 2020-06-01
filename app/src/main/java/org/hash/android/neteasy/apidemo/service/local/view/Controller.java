package org.hash.android.neteasy.apidemo.service.local.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import org.hash.android.neteasy.apidemo.R;
import org.hash.android.neteasy.apidemo.service.local.LocalService;


/**
 * Created by Hash on 2020/6/1.
 * Example of explicitly starting and stopping the local service
 * This demonstrates the implementation of a service that runs in the same
 * process as the rest of the application. which is explicitly started and stopped
 * as desired.
 */
public class Controller extends Activity {

    protected static String TAG = Controller.class.getCanonicalName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_service_controller);

        findViewById(R.id.start).setOnClickListener(mStartListener);
        findViewById(R.id.stop).setOnClickListener(mStopListener);
        findViewById(R.id.bind).setOnClickListener(mBindListener);
        findViewById(R.id.unbind).setOnClickListener(mUnBindListener);

    }

    private View.OnClickListener mStartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            startService(new Intent(Controller.this, LocalService.class));
        }
    };

    private View.OnClickListener mStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            stopService(new Intent(Controller.this, LocalService.class));
        }
    };


    /*****************************bind************************************************/

    private boolean mShouldUnbind;

    protected LocalService mBoundService;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((LocalService.LocalBinder) service).getService();
            Log.i(TAG, "Connected to local service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
            Log.i(TAG, "Disconnected from local service");

        }
    };

    private View.OnClickListener mBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (bindService(new Intent(Controller.this, LocalService.class), mConnection, Context.BIND_AUTO_CREATE)) {
                mShouldUnbind = true;

            } else {
                Log.i(TAG, "Error: The request service doesn't exist, or" +
                        "this client isn't allowed access to it ");

            }

        }
    };

    private View.OnClickListener mUnBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mShouldUnbind) {
                unbindService(mConnection);
                mShouldUnbind = !mShouldUnbind;
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mShouldUnbind = false;
    }
}
