package com.zoowii.hibernate_utils.util;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtil {
    public static <T1, T2> List<T2> map(List<T1> source, Function<T1, T2> fn) {
        if (fn == null || source == null) {
            return null;
        }
        List<T2> result = new ArrayList<T2>();
        for (T1 item : source) {
            result.add(fn.apply(item));
        }
        return result;
    }

    public static <T> List<T> seq(T... items) {
        List<T> result = new ArrayList<T>();
        for (T item : items) {
            result.add(item);
        }
        return result;
    }

    public static Map<String, Object> hashmap(Object... items) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < items.length; i += 2) {
            if (i + 1 >= items.length) {
                break;
            }
            Object key = items[i];
            Object val = items[i + 1];
            result.put((String) key, val);
        }
        return result;
    }
}
