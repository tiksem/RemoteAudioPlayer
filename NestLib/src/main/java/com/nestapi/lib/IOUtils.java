package com.nestapi.lib;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by stikhonenko on 1/5/16.
 */
public class IOUtils {
    private static final int BUFFER_SIZE = 1024;

    public static String toString(Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int n;

        while ((n = reader.read(buffer)) != -1) {
            content.append(buffer, 0, n);
        }

        return content.toString();
    }

    public static String toString(InputStream inputStream, String encoding)
            throws IOException {
        Reader reader = new InputStreamReader(inputStream, encoding);
        return toString(reader);
    }

    public static String readRawResourceAsString(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        try {
            return toString(inputStream, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
