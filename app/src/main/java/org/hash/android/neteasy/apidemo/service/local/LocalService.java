package org.hash.android.neteasy.apidemo.service.local;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;


import androidx.annotation.Nullable;

import org.hash.android.neteasy.apidemo.R;
import org.hash.android.neteasy.apidemo.service.local.view.Controller;

/**
 * Created by Hash on 2020/6/1.
 * This is an example of implementing an application service that runs
 * locally int the same process as the application.
 * The {@link Controller} and {@link Binding} classes show how to interact
 * with the service
 * <p>
 * <p>
 * Notice the use of the {@link android.app.NotificationManager}when interesting
 * things happen in the service. This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as call
 * startActivity()
 */
public class LocalService extends Service {

    private NotificationManager mNM;

    //Unique Identification Number for the Notification
    //We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    protected static String TAG = LocalService.class.getCanonicalName();

    /**
     * Class for clients to access. Because we know this service always
     * runs in the same process as its clients,we don't need to deal with
     * IPC
     */
    public class LocalBinder extends Binder {

        public LocalService getService() {
            return LocalService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        showNotification();
    }

    /**
     * Display a notification about us starting. We put an icon int the status bar.
     */
    private void showNotification() {
        // TODO: 2020/6/1
        CharSequence text = getText(R.string.local_service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, new Intent(this,
                        Controller.class), 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentText(text)
                .setContentTitle(getText(R.string.local_service_label))
                .setContentIntent(contentIntent)
                .build();
        mNM.notify(NOTIFICATION, notification);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Receive start id " + startId + " : " + intent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    //This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNM.cancel(NOTIFICATION);
        Log.i(TAG, getString(R.string.local_service_stopped));
    }
}
