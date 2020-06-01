package org.hash.android.neteasy.apidemo.service.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hash.android.aidl.remote.IRemoteService;
import org.hash.android.aidl.remote.IRemoteServiceCallback;
import org.hash.android.aidl.remote.ISecondary;
import org.hash.android.neteasy.apidemo.R;
import org.hash.android.neteasy.apidemo.service.local.LocalService;
import org.hash.android.neteasy.apidemo.service.remote.view.Client;

/**
 * Created by Hash on 2020/6/1.
 * TODO  Just enjoy Coding , wish guys Children'Day have a fun.
 * <p>
 * This is an example of implementation an application service
 * that runs in a different process than the application.
 * Because it can be in another processor, we must use IPC to interact with it.
 * <p>
 * The {@link org.hash.android.neteasy.apidemo.service.remote.view.Client} and {@link org.hash.android.neteasy.apidemo.service.remote.view.Server} classes show how to interact with the service
 * <p>
 * Note that most applications do not need to deal with the complexity shown here, If your
 * application simply has a service running in its own process, the {@link LocalService} sample
 * show s a much simpler way to interact with it
 */


public class RemoteService extends Service {

    /**
     * This is a list of callbacks tha have been registered with the service.
     * Note that this is package scoped (instead of private) so that it can be accessed more efficiently form inner classes
     */
    public static final RemoteCallbackList<IRemoteServiceCallback> mCallbacks = new RemoteCallbackList<>();

    protected static final String TAG = RemoteService.class.getCanonicalName();

    protected int mValue = 0;

    protected NotificationManager mNM;

    protected static final int REPORT_MSG = 1;

    /**
     * Our Handler used to execute operations on the main thread，
     * This is used to schedule increments of our value
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                // It is time to bump the value
                case REPORT_MSG:
                    // Up it goes
                    int value = +mValue;

                    //Broadcast to all clients the new value
                    int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            // to
                            Bundle bundle = new Bundle();
//                            BitmapFactory.decodeStream(new FileInputStream(new File("")));
//                            // 可以传bitmap 但是bitmap  Bitmap 是实现Parcelable接口的，bundle有一个方法是putParcelable方法来传递Parcelable实例
//                            // 但是你要知道Bitmap一般比较大，而Bundle的传递的值如果过大是会报错的，传递的budle过大，一般在1M以内
//
//                            bundle.putParcelable();
//                            bundle.clear();
                            mCallbacks.getBroadcastItem(i).valueChanged(value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mCallbacks.finishBroadcast();

                    // Repeat every 1 second
                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1_000);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting
        showNotification();

        //While this service is running, it will continually increment a number.
        //Send the first message tha is used to perform the increment.
        mHandler.sendEmptyMessage(REPORT_MSG);

    }

    /**
     * Show a notification while this service is running
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_stopped);

        //The PendingIntent to launcher our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, Client.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.remote_service_label))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();
        //Send the notification
        //We use a string id because it is a unique number , We use it later to cancel
        mNM.notify(R.string.remote_service_started, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id :" + startId + " : " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the persistent notification
        mNM.cancel(R.string.remote_service_started);

        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_LONG)
                .show();
        //Unregister all callback
        mCallbacks.kill();

        //Remove the next pending message to increment the  counter, stopping the increment loop.
        mHandler.removeMessages(REPORT_MSG);


    }

    //Local-side IPC implementation stub class
    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {

        /**
         * Often you want to allow a service to call back to its clients.
         * This shows how to do so, by registering a callback interface with
         * the service.
         * @param cb
         * @throws RemoteException
         */
        @Override
        public void registerCallback(IRemoteServiceCallback cb) throws RemoteException {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }

        /**
         *  Remove a previously registered callback interface.
         * @param cb
         * @throws RemoteException
         */
        @Override
        public void unregisterCallback(IRemoteServiceCallback cb) throws RemoteException {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }

        }
    };

    /**
     * A secondary interface to the service
     */
    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        @Override
        public int getPid() throws RemoteException {
            return Process.myPid();
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        // Select the interface to return.
        // If your service only implements a single interface ,
        // you can just return it here without checking the intent
        if (IRemoteService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }

        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }

        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "Task removed :" + rootIntent);
    }
}
