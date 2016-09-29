package com.blazebit.persistence.impl.plan;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.spi.ExtendedQuerySupport;
import com.blazebit.persistence.spi.ServiceProvider;

import javax.persistence.Query;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CustomModificationQueryPlan implements ModificationQueryPlan {

    private final ExtendedQuerySupport extendedQuerySupport;
    private final ServiceProvider serviceProvider;
    private final Query delegate;
    private final List<Query> participatingQueries;
    private final String sql;

    public CustomModificationQueryPlan(ExtendedQuerySupport extendedQuerySupport, ServiceProvider serviceProvider, Query delegate, List<Query> participatingQueries, String sql) {
        this.extendedQuerySupport = extendedQuerySupport;
        this.serviceProvider = serviceProvider;
        this.delegate = delegate;
        this.participatingQueries = participatingQueries;
        this.sql = sql;
    }

    @Override
    public int executeUpdate() {
        return extendedQuerySupport.executeUpdate(serviceProvider, participatingQueries, delegate, sql);
    }

}
