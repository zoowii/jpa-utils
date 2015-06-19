package com.zoowii.jpa_utils.util;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.util.functions.Function2;

import java.util.*;

public class ListUtil {
    public static Map<String, Object> hashmap(Object... params) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 >= params.length) {
                break;
            }
            Object param1 = params[i];
            Object param2 = params[i + 1];
            if (param1 == null) {
                continue;
            }
            String key = param1.toString();
            result.put(key, param2);
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

    public static List<Integer> intToList(int[] arr) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    public static <T1, T2> List<T2> map(List<T1> source, Function<T1, T2> fn) {
        if (source == null || fn == null) {
            return null;
        }
        List<T2> result = new ArrayList<T2>();
        for (T1 item : source) {
            result.add(fn.apply(item));
        }
        return result;
    }

    public static <T> T reduce(List<T> source, T start, Function2<T, T, T> fn) {
        if (fn == null || source == null) {
            return start;
        }
        T result = start;
        for (T item : source) {
            result = fn.apply(result, item);
        }
        return result;
    }

    public static <T> T reduce(List<T> source, Function2<T, T, T> fn) {
        if (fn == null || size(source) < 1) {
            return null;
        }
        return reduce(rest(source), first(source), fn);
    }

    public static <T> List<T> rest(List<T> source) {
        if (source == null) {
            return null;
        }
        List<T> result = clone(source);
        if (size(result) > 0) {
            result.remove(0);
        }
        return result;
    }

    public static <T> List<T> clone(List<T> source) {
        if (source == null) {
            return null;
        }
        return map(source, new Function<T, T>() {
            @Override
            public T apply(T t) {
                return t;
            }
        });
    }

    public static <T> List<T> filter(List<T> source, Function<T, Boolean> fn) {
        if (source == null || fn == null) {
            return source;
        }
        List<T> result = new ArrayList<T>();
        for (T item : source) {
            Boolean predResult = fn.apply(item);
            if (predResult != null && predResult) {
                result.add(item);
            }
        }
        return result;
    }

    public static <T> T first(List<T> source, Function<T, Boolean> fn) {
        if (source == null || fn == null) {
            return null;
        }
        for (T item : source) {
            Boolean predResult = fn.apply(item);
            if (predResult != null && predResult) {
                return item;
            }
        }
        return null;
    }

    public static <T> Function<T, Boolean> not(final Function<T, Boolean> fn) {
        if (fn == null) {
            return null;
        }
        return new Function<T, Boolean>() {
            @Override
            public Boolean apply(T t) {
                Boolean result = fn.apply(t);
                if (result == null) {
                    return true;
                }
                return !result;
            }
        };
    }

    public static <T> boolean any(List<T> source, Function<T, Boolean> fn) {
        return first(source, fn) != null;
    }

    public static <T> boolean all(List<T> source, Function<T, Boolean> fn) {
        return first(source, not(fn)) == null;
    }

    public static <T> T first(List<T> source) {
        if (source == null || source.size() < 1) {
            return null;
        }
        return source.get(0);
    }

    public static <T> int size(List<T> source) {
        if (source == null) {
            return 0;
        }
        return source.size();
    }

    public static <T> T max(List<T> source, Comparator<T> comparator) {
        if (source == null || comparator == null) {
            return null;
        }
        List<T> sorted = clone(source);
        return Collections.max(sorted, comparator);
    }

    public static <T> T min(List<T> source, Comparator<T> comparator) {
        if (source == null || comparator == null) {
            return null;
        }
        List<T> sorted = clone(source);
        return Collections.min(sorted, comparator);
    }
}
