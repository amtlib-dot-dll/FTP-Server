package org.server.ftp;

import android.os.Environment;

public class Constants {
    private static final String PREFIX = Constants.class.getPackage().getName();
    public static final String PREFERENCE_PORT = "port";
    public static final String PREFERENCE_CHARSET = "charset";
    public static final int DEFAULT_PORT = 60021;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String EXTRA_NAME = PREFIX + "name";
    public static final String ADDRESS = "ftp://%1$s:%2$d/" + System.lineSeparator();
}
