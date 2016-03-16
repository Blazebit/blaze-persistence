package com.blazebit.persistence.view.testsuite.update;

import javax.persistence.EntityManager;

public interface TxVoidWork {

    public void doWork(EntityManager em) throws Exception;
    
}
