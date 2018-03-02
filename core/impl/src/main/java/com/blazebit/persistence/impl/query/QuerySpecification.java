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

package com.blazebit.persistence.impl.query;

import com.blazebit.persistence.impl.plan.ModificationQueryPlan;
import com.blazebit.persistence.impl.plan.SelectQueryPlan;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Set<Parameter<?>> getParameters();

    public Map<String, String> getAddedCtes();

    public Query getBaseQuery();

    public void onCollectionParameterChange(String parameterName, Collection<?> value);

}
