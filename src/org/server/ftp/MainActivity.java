package org.server.ftp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {
    private ServerService.ServerBinder binder;
    private Switch runningSwitch;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = ((ServerService.ServerBinder) service);
            boolean running = binder.isRunning();
            runningSwitch.setChecked(running);
            runningSwitch.setEnabled(true);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, running ? new StatusFragment() : new SettingsFragment());
            transaction.commit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            throw new AssertionError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            throw new NullPointerException();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindService(new Intent(this, ServerService.class), connection, BIND_AUTO_CREATE);
        runningSwitch = (Switch) View.inflate(this, R.layout.switch_running, null);
        runningSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                if (isChecked) {
                    startService(new Intent(MainActivity.this, ServerService.class));
                    transaction.replace(android.R.id.content, new StatusFragment());
                } else {
                    if (binder != null) {
                        binder.exit();
                    }
                    transaction.replace(android.R.id.content, new SettingsFragment());
                }
                transaction.commit();
            }
        });
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(runningSwitch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
