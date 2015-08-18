package com.blazebit.persistence.impl.function;

public class CyclicUnsignedCounter {

    private int value = Integer.MIN_VALUE;

    public CyclicUnsignedCounter() {
    }

    public CyclicUnsignedCounter(int value) {
        this.value = value;
    }

    public int incrementAndGet() {
        return value = (value + 1) & Integer.MAX_VALUE;
    }

    public int getAndIncrement() {
        int temp = value;
        value = (value + 1) & Integer.MAX_VALUE;
        return temp;
    }
}
