package com.bzh.dytt.key;

import android.net.Uri;

import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class KeyUtilsTest {

    @Test
    public void test2() {

    }

    @Test
    public void test() throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append("wq0LQbLUTH66");
        sb.append("x-header-request-timestamp");
        sb.append("=");
        long currentTimeMillis = System.currentTimeMillis();
        long x = currentTimeMillis / 1000;
        System.out.println(x);
        sb.append(x);
        sb.append("x-header-request-imei");
        sb.append("=");
        sb.append("");
        String content = sb.toString();

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] bytes = digest.digest(content.getBytes());

        System.out.println(new String(HEX(bytes)));
    }

    static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static char[] HEX(byte[] bArr) {
        int i = 0;
        int length = bArr.length;
        char[] cArr = new char[(length << 1)];
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = i + 1;
            cArr[i] = HEX[(bArr[i2] & 240) >>> 4];
            i = i3 + 1;
            cArr[i3] = HEX[bArr[i2] & 15];
        }
        return cArr;
    }
}