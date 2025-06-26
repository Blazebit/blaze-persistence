/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.persistence.ReturningResult;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateReturningResult<T> implements ReturningResult<T> {

    private List<T> resultList;
    private int updateCount;

    @Override
    public T getLastResult() {
        return resultList.get(resultList.size() - 1);
    }

    @Override
    public List<T> getResultList() {
        return resultList;
    }
    
    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
    }
    
    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
    

}
