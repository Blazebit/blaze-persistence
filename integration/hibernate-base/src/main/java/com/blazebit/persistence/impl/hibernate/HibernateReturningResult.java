/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
