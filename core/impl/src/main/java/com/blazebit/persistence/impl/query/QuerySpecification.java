/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface QuerySpecification<T> {

    public ModificationQueryPlan createModificationPlan(int firstResult, int maxResults);

    public SelectQueryPlan<T> createSelectPlan(int firstResult, int maxResults);

    public String getSql();

    public List<Query> getParticipatingQueries();

    public Collection<? extends Parameter<?>> getParameters();

    public Map<String, String> getAddedCtes();

    public Query getBaseQuery();

    public void onCollectionParameterChange(String parameterName, Collection<?> value);

}
