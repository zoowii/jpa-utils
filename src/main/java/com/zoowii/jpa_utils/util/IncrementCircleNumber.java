package com.zoowii.jpa_utils.util;

/**
 * 从start到end不断递增，到了end就循环回start
 * Created by zoowii on 15/1/27.
 */
public class IncrementCircleNumber {
    private long current;
    private long start;
    private long end;

    public IncrementCircleNumber(long start, long end) {
        if (end <= start) {
            throw new IllegalArgumentException("end should be large than start");
        }
        this.start = start;
        this.end = end;
        this.current = this.start;
    }

    public synchronized long getAndIncrement() {
        long next = this.current;
        this.current += 1;
        if (this.current >= end) {
            this.current = this.start;
        }
        return next;
    }
}