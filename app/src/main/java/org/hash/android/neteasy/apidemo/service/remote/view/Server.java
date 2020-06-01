package org.hash.android.neteasy.apidemo.service.remote.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hash.android.aidl.remote.IRemoteService;
import org.hash.android.aidl.remote.IRemoteServiceCallback;
import org.hash.android.aidl.remote.ISecondary;
import org.hash.android.neteasy.apidemo.R;
import org.hash.android.neteasy.apidemo.service.remote.RemoteService;

/**
 * Created by Hash on 2020/6/1.
 * <p>
 * Example of binding and unbinding to the remote service
 * This demonstrates the implementation of a service which the client
 * will bind to, interacting with it through an aidl interface
 * <p>
 * BindService
 */
public class Server extends Activity {
    /**
     * Standard initialization of this activity,
     * Set up the UI , then wait for the user to poke it before doing anything
     */
    protected static final String TAG = Server.class.getCanonicalName();
    protected Button mKillBtn;
    protected TextView mCallbackText;
    protected IRemoteService mService = null;
    protected ISecondary mSecondService = null;
    protected boolean mIsBound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_service_binding);
        findViewById(R.id.bind).setOnClickListener(mBindListener);
        findViewById(R.id.unbind).setOnClickListener(mUnBindListener);
        mKillBtn = findViewById(R.id.kill);
        mKillBtn.setOnClickListener(mKillListener);
        mKillBtn.setEnabled(false);
        mCallbackText = findViewById(R.id.callback);
        mCallbackText.setText("Not attached");
    }


    private View.OnClickListener mBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Establish a couple connections with the service, binding
            // by interface names, This allows other applications to be
            // installed the replace the remote service by implementing
            // the same interface
            Intent intent = new Intent(Server.this, RemoteService.class);
            // bind the same interface name
            intent.setAction(IRemoteService.class.getName());
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            intent.setAction(ISecondary.class.getName());
            bindService(intent, mSecondaryConnection, Context.BIND_AUTO_CREATE);

            mIsBound = true;
            mCallbackText.setText("Binding the Remote Service");

        }
    };

    private View.OnClickListener mUnBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsBound) {
                // If we have received the service , and hence registered with
                // it , the now is the time to unregister
                if (mService != null) {
                    try {
                        mService.unregisterCallback(mCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                // Detach our existing connection
                unbindService(mConnection);
                unbindService(mSecondaryConnection);
                mKillBtn.setEnabled(false);
                mIsBound = !mIsBound;
                mCallbackText.setText("Unbinding the RemoteService");
            }

        }
    };

    private View.OnClickListener mKillListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // To kill the process hosting our service ,
            // We need to know its PID, Conveniently our service
            // has a call that will return to us that information
            if (mSecondService != null) {
                try {
                    int pid = mSecondService.getPid();

                    // Note that, though this api allows us to request
                    // to kill any process based on its pid, the kernel
                    // will still impose standard restrictions on which
                    // PID you actually able to kill. typically this means
                    // only the process running your application and any additional
                    // processes created by that app as shown here; packages sharing
                    // a common UID will also be able to kill each other's processes
                    Process.killProcess(pid);
                    mCallbackText.setText("Killed service process");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    //Recover gracefully from the process hosting the server dying
                    // Just for purposes of the sample, put up a notification
                    Log.i(TAG, "Failure calling RemoteService");
                }
            }

        }
    };

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 11:
                    mCallbackText.setText("Receive from RemoteService:" + msg.arg1);
                    break;
            }
            super.handleMessage(msg);

        }
    };


    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        @Override
        public void valueChanged(int value) throws RemoteException {

            Log.i(TAG, "current thread " + Thread.currentThread().getName());
            mHandler.sendMessage(mHandler.obtainMessage(11, value, 0));

        }
    };


    /**
     * Class for interacting with main interface of the service
     */
    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IRemoteService.Stub.asInterface(service);
            mKillBtn.setEnabled(true);
            mCallbackText.setText("Service Attached");


            //We want to monitor the service for as long as we are
            //connected to it

            try {
                mService.registerCallback(mCallback);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "Connected to remote Service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //This is called when the connection with the service
            //has been unexpectedly disconnected -- that is ,its process crashed.
            mService = null;
            mKillBtn.setEnabled(false);
            mCallbackText.setText("Disconnected");
            Log.i(TAG, "Disconnected form remote service");
        }
    };

    /**
     * Class for interacting with secondary interface of the service
     */
    private ServiceConnection mSecondaryConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Connecting to a secondary interface is the same as any other interface
            mSecondService = ISecondary.Stub.asInterface(service);
            mKillBtn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSecondService = null;
            mKillBtn.setEnabled(false);

        }
    };
}
