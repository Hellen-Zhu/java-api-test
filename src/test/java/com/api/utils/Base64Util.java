package com.api.utils;

import org.apache.commons.lang3.StringUtils;

public final class Base64Util {

    private Base64Util() {
    }

    private static final byte[] CODES = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            CODES[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            CODES[i] = (byte) (i - 'A');
        }
        for (int i = 'a'; i <= 'z'; i++) {
            CODES[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            CODES[i] = (byte) (52 + i - '0');
        }
        CODES['+'] = 62;
        CODES['/'] = 63;
    }

    public static byte[] convertStringToByteArray(String str) {
        if (StringUtils.isEmpty(str)) {
            return new byte[0];
        }
        char[] charArray = str.toCharArray();
        return convertCharArrayToByteArray(charArray);
    }

    public static byte[] convertCharArrayToByteArray(char[] charArray) {
        int len = ((charArray.length + 3) / 4) * 3;
        if (charArray.length > 0 && charArray[charArray.length - 1] == '=') {
            --len;
        }
        if (charArray.length > 1 && charArray[charArray.length - 2] == '=') {
            --len;
        }
        byte[] out = new byte[len];
        int shift = 0;
        int accum = 0;
        int index = 0;
        for (char datum : charArray) {
            int value = CODES[datum & 0xFF];
            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value;
                if (shift >= 8) {
                    shift -= 8;
                    out[index++] = (byte) ((accum >> shift) & 0xFF);
                }
            }
        }
        if (index != out.length) {
            System.out.println("[Exception] miscalculated data length!");
        }
        return out;
    }
}