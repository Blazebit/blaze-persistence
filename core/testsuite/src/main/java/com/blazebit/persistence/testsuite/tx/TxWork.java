package com.blazebit.persistence.testsuite.tx;


public interface TxWork<V> {

    public V work() throws Exception;
}
