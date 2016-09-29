package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.plan.DefaultModificationQueryPlan;
import com.blazebit.persistence.impl.plan.DefaultSelectQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultQuerySpecification implements QuerySpecification {

    private final Query query;
    private final EntityManager em;
    private final Set<String> parameterListNames;
    private final ExtendedQuerySupport extendedQuerySupport;

    public DefaultQuerySpecification(Query query, EntityManager em, Set<String> parameterListNames, ExtendedQuerySupport extendedQuerySupport) {
        this.query = query;
        this.em = em;
        this.parameterListNames = parameterListNames;
        this.extendedQuerySupport = extendedQuerySupport;
    }

    @Override
    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults) {
        return new DefaultModificationQueryPlan(query, firstResult, maxResults);
    }

    @Override
    public SelectQueryPlan createSelectPlan(int firstResult, int maxResults) {
        return new DefaultSelectQueryPlan(query, firstResult, maxResults);
    }

    @Override
    public String getSql() {
        return extendedQuerySupport.getSql(em, query);
    }

    @Override
    public List<Query> getParticipatingQueries() {
        return Arrays.asList(query);
    }

    @Override
    public Map<String, String> getAddedCtes() {
        return null;
    }

    @Override
    public Query getBaseQuery() {
        return query;
    }

    @Override
    public void onParameterChange(String parameterName) {
    }
}
