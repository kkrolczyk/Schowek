package org.kkrolczyk.schowek;

import java.nio.charset.Charset;

/**
 * Created by kkrolczyk on 23.11.14.
 */
public class MyUtils {

    public static String[] convertToStrings(byte[][] byteStrings) {
        String[] data = new String[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            data[i] = new String(byteStrings[i], Charset.defaultCharset());

        }
        return data;
    }

    public static byte[][] convertToBytes(String[] strings) {
        byte[][] data = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            data[i] = string.getBytes(Charset.defaultCharset()); // you can chose charset
        }
        return data;
    }

}
