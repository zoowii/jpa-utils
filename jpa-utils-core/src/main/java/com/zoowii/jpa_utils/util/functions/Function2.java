package com.zoowii.jpa_utils.util.functions;

/**
 * Created by zoowii on 2014/10/13.
 */
public interface Function2<T1, T2, R> {
    public R apply(T1 t1, T2 t2);
}
