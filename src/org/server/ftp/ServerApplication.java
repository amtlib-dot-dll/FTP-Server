package org.server.ftp;

import android.app.Application;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerApplication extends Application {
    private UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        File file = new File(getFilesDir(), "users.properties");
        if (!file.exists()) {
            try {
                new FileOutputStream(file, true).close();
            } catch (IOException ignored) {
            }
        }
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(file);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        userManager = userManagerFactory.createUserManager();
    }
}
