package org.server.ftp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {
    private ServerService.ServerBinder binder;
    private Switch runningSwitch;
    private ViewPager pager;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = ((ServerService.ServerBinder) service);
            boolean running = binder.isRunning();
            runningSwitch.setChecked(running);
            runningSwitch.setEnabled(true);
            if (running) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            pager.setCurrentItem(running ? 1 : 0);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            throw new AssertionError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new SettingsFragment();
                    case 1:
                        return new StatusFragment();
                    default:
                        throw new AssertionError();
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        if (binder != null) {
                            binder.exit();
                        }
                        runningSwitch.setChecked(false);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    case 1:
                        startService(new Intent(MainActivity.this, ServerService.class));
                        runningSwitch.setChecked(true);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        });
        runningSwitch = (Switch) View.inflate(this, R.layout.switch_running, null);
        runningSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pager.setCurrentItem(isChecked ? 1 : 0);
            }
        });
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            throw new NullPointerException();
        }
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(runningSwitch, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END | Gravity.CENTER_VERTICAL));
        bindService(new Intent(this, ServerService.class), connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
