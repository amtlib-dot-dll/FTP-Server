package org.server.ftp;

import android.app.Application;
import android.media.MediaScannerConnection;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ServerApplication extends Application {
    private final FtpServerFactory serverFactory = new FtpServerFactory();
    private final ListenerFactory listenerFactory = new ListenerFactory();
    private final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    private UserManager userManager;

    public void setPort(int port) {
        listenerFactory.setPort(port);
        serverFactory.addListener("default", listenerFactory.createListener());
    }

    public void setCharset(Charset charset) {
        serverFactory.setCharset(charset);
    }

    public FtpServer createServer() {
        return serverFactory.createServer();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serverFactory.getFtplets().put("media_scan", new DefaultFtplet() {
            @Override
            public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
                MediaScannerConnection.scanFile(ServerApplication.this, new String[]{((NativeFtpFile) session.getFileSystemView().getFile(request.getArgument())).getPhysicalFile().getPath()}, null, null);
                return null;
            }

            @Override
            public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
                MediaScannerConnection.scanFile(ServerApplication.this, new String[]{((NativeFtpFile) session.getFileSystemView().getFile(request.getArgument())).getPhysicalFile().getPath()}, null, null);
                return null;
            }
        });
        File file = new File(getFilesDir(), "users.properties");
        if (!file.exists()) {
            try {
                new FileOutputStream(file, true).close();
            } catch (IOException ignored) {
            }
        }
        userManagerFactory.setFile(file);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        userManager = userManagerFactory.createUserManager();
        serverFactory.setUserManager(userManager);
    }
}
