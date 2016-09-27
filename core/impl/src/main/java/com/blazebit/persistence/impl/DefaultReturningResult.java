package com.blazebit.persistence.impl;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;

import java.util.ArrayList;
import java.util.List;

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
