package org.hash.android.neteasy.apidemo.service.remote.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import org.hash.android.neteasy.apidemo.R;
import org.hash.android.neteasy.apidemo.service.remote.RemoteService;

/**
 * Created by Hash on 2020/6/1.
 * <p>
 * Example of explicitly starting and stopping the remove service.
 * This demonstrates the implementation of a service that runs in a different
 * process than the rest of the application, which is explicitly started and stopped
 * as desired
 */
public class Client extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_service_controller);
        // Watch for button clicks
        findViewById(R.id.start).setOnClickListener(mStartListener);
        findViewById(R.id.stop).setOnClickListener(mStopListener);
    }

    private View.OnClickListener mStartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Make sure the service is started . It will continue running
            // until someone calls stopService()
            // We use an action code here, instead of explicitly supplying
            // the component name, so that other packages can replace the service
            startService(new Intent(Client.this, RemoteService.class));

        }
    };

    private View.OnClickListener mStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Cancel a previous call to stopService() .
            // Note that the service will not actually stop at this point
            // if there are still bound clients
            stopService(new Intent(Client.this, RemoteService.class));

        }
    };
}
