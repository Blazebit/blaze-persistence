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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultReturningResult<Z> implements ReturningResult<Z> {
    private final List<Z> resultList;
    private final int updateCount;
    private final DbmsDialect dbmsDialect;

    public DefaultReturningResult(List<Z> resultList, int updateCount, DbmsDialect dbmsDialect) {
        this.resultList = resultList;
        this.updateCount = updateCount;
        this.dbmsDialect = dbmsDialect;
    }

    public DefaultReturningResult(List<Object[]> originalResultList, int updateCount, DbmsDialect dbmsDialect, ReturningObjectBuilder<Z> objectBuilder) {
        this.updateCount = updateCount;
        this.dbmsDialect = dbmsDialect;
        if (objectBuilder != null) {
            final List<Z> resultList = new ArrayList<Z>(originalResultList.size());

            for (Object[] element : originalResultList) {
                resultList.add(objectBuilder.build(element));
            }

            this.resultList = objectBuilder.buildList(resultList);
        } else {
            this.resultList = (List<Z>) originalResultList;
        }
    }

    @Override
    public Z getLastResult() {
        return resultList.get(resultList.size() - 1);
    }

    @Override
    public List<Z> getResultList() {
        if (dbmsDialect.supportsReturningAllGeneratedKeys()) {
            return resultList;
        }

        throw new UnsupportedOperationException("The database does not support returning all generated keys!");
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
    }
}
