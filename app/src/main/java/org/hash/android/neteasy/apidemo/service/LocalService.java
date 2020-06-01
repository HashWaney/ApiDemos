package org.hash.android.neteasy.apidemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;


import androidx.annotation.Nullable;

/**
 * Created by Hash on 2020/6/1.
 */


public class LocalService extends Service {


//    final RemoteCallbackList<IRemoteServiceCallback> mCallbacks = new RemoteCallbackList<>();


    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return null;
    }
}
