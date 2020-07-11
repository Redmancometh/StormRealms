package org.stormrealms.stormmenus.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by GyuriX, on 2017. 05. 31..
 */
public class StreamUtils
{
    public static Charset utf8 = Charset.forName("UTF-8");

    public static void streamToFile(InputStream is, File f) throws IOException
    {
        byte[] buf = new byte[2048];
        FileOutputStream fos = new FileOutputStream(f);
        for (int i = is.read(buf, 0, 2048); i > 0; i = is.read(buf, 0, 2048))
            fos.write(buf, 0, i);
        is.close();
        fos.close();
    }

    public static String streamToString(InputStream is) throws IOException
    {
        return new String(streamToBytes(is), utf8);
    }

    public static byte[] streamToBytes(InputStream is) throws IOException
    {
        byte[] buf = new byte[2048];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = is.read(buf, 0, 2048); i > 0; i = is.read(buf, 0, 2048))
            bos.write(buf, 0, i);
        is.close();
        return bos.toByteArray();
    }

    public static void stringToStream(String s, OutputStream os) throws IOException
    {
        os.write(s.getBytes(utf8));
        os.close();
    }
}
