package com.blazebit.persistence.impl.hibernate;

import java.util.List;

import com.blazebit.persistence.ReturningResult;


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
