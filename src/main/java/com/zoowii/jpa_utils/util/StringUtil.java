package com.zoowii.jpa_utils.util;

import java.util.List;
import java.util.Random;

public class StringUtil {
    public static String randomString(int n) {
        if (n < 1) {
            n = 1;
        }
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int num = random.nextInt(str.length());
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    public static String join(List<String> strs, String sep) {
        StringBuilder builder = new StringBuilder();
        if (strs == null) {
            return null;
        }
        if (sep == null) {
            sep = "";
        }
        for (int i = 0; i < strs.size(); ++i) {
            if (i > 0) {
                builder.append(sep);
            }
            builder.append(strs.get(i));
        }
        return builder.toString();
    }
}
