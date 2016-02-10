package com.blazebit.persistence.tx;


public interface TxWork<V> {

    public V work() throws Exception;
}
