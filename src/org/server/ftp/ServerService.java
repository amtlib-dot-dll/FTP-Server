package org.server.ftp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;

import java.nio.charset.Charset;

import static org.server.ftp.Constants.*;

public class ServerService extends Service {
    private static final String WAKE_LOCK_TAG = ServerService.class.getPackage().getName();
    private final ServerBinder binder = new ServerBinder();
    private FtpServer server;
    private PowerManager.WakeLock lock;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        ((ServerApplication) getApplication()).setPort(preferences.getInt(PREFERENCE_PORT, DEFAULT_PORT));
        ((ServerApplication) getApplication()).setCharset(Charset.forName(preferences.getString(PREFERENCE_CHARSET, DEFAULT_CHARSET)));
        if (server == null || server.isStopped()) {
            try {
                server = ((ServerApplication) getApplication()).createServer();
                server.start();
                lock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                lock.acquire();
            } catch (FtpException e) {
                e.printStackTrace();
            }
            startForeground(hashCode(), new Notification.Builder(this)
//                    .setSubText("sub text")
                    .setTicker(getString(R.string.notification_ticker))
                    .setSmallIcon(R.drawable.ic_action_web_site)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_info, preferences.getInt(PREFERENCE_PORT, DEFAULT_PORT)))
//                    .setContentText("text")
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                    .setOngoing(true)
                    .build());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ServerBinder extends Binder {
        public boolean isRunning() {
            return server != null && !server.isStopped();
        }

        public void exit() {
            stopForeground(true);
            server.stop();
            lock.release();
            stopSelf();
        }
    }
}
