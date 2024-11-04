/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.plan.DefaultModificationQueryPlan;
import com.blazebit.persistence.impl.plan.DefaultSelectQueryPlan;
import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedQuerySupport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultQuerySpecification implements QuerySpecification {

    private final DbmsStatementType statementType;
    private final Query query;
    private final EntityManager em;
    private final Set<String> parameterListNames;
    private final ExtendedQuerySupport extendedQuerySupport;

    public DefaultQuerySpecification(DbmsStatementType statementType, Query query, EntityManager em, Set<String> parameterListNames, ExtendedQuerySupport extendedQuerySupport) {
        this.statementType = statementType;
        this.query = query;
        this.em = em;
        this.parameterListNames = parameterListNames;
        this.extendedQuerySupport = extendedQuerySupport;
    }

    @Override
    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults) {
        return new DefaultModificationQueryPlan(statementType, query, firstResult, maxResults);
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
    public Collection<? extends Parameter<?>> getParameters() {
        return query.getParameters();
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
    public void onCollectionParameterChange(String parameterName, Collection value) {
        if (parameterListNames.contains(parameterName)) {
            query.setParameter(parameterName, value);
        }
    }
}
