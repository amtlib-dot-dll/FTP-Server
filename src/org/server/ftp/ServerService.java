package org.server.ftp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.server.ftp.Constants.*;

public class ServerService extends Service {
    private static final String WAKE_LOCK_TAG = ServerService.class.getSimpleName();
    private final ServerBinder binder = new ServerBinder();
    private FtpServer server;
    private PowerManager.WakeLock lock;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (server == null || server.isStopped()) {
            try {
                ListenerFactory listenerFactory = new ListenerFactory();
                listenerFactory.setPort(preferences.getInt(PREFERENCE_PORT, DEFAULT_PORT));
                FtpServerFactory serverFactory = new FtpServerFactory();
                serverFactory.setCharset(Charset.forName(preferences.getString(PREFERENCE_CHARSET, DEFAULT_CHARSET)));
                serverFactory.addListener("default", listenerFactory.createListener());
                serverFactory.getFtplets().put("media_scan", new DefaultFtplet() {
                    @Override
                    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
                        MediaScannerConnection.scanFile(ServerService.this, new String[]{((NativeFtpFile) session.getFileSystemView().getFile(request.getArgument())).getPhysicalFile().getPath()}, null, null);
                        return null;
                    }

                    @Override
                    public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
                        MediaScannerConnection.scanFile(ServerService.this, new String[]{((NativeFtpFile) session.getFileSystemView().getFile(request.getArgument())).getPhysicalFile().getPath()}, null, null);
                        return null;
                    }
                });
                serverFactory.setUserManager(((ServerApplication) getApplication()).getUserManager());
                server = serverFactory.createServer();
                server.start();
                lock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                lock.acquire();
            } catch (FtpException e) {
                e.printStackTrace();
            }
            startForeground(hashCode(), new Notification.Builder(this)
                    .setTicker(getString(R.string.notification_ticker))
                    .setSmallIcon(R.drawable.ic_cloud_circle_white)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_info, preferences.getInt(PREFERENCE_PORT, DEFAULT_PORT)))
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
