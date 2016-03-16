package com.blazebit.persistence.view.testsuite.update;

import javax.persistence.EntityManager;

public interface TxWork<T> {

    public T doWork(EntityManager em) throws Exception;
    
}
