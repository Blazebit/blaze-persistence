/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import javax.persistence.Tuple;

/**
 * A builder for criteria queries. This is the entry point for building queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaBuilder<T> extends QueryBuilder<T, CriteriaBuilder<T>>, GroupByBuilder<T, CriteriaBuilder<T>>, DistinctBuilder<T, CriteriaBuilder<T>> {

    /*
     * Covariant overrides.
     */
    
    @Override
    public SimpleCaseWhenBuilder<CriteriaBuilder<Tuple>> selectSimpleCase(String expression);
    
    @Override
    public SimpleCaseWhenBuilder<CriteriaBuilder<Tuple>> selectSimpleCase(String expression, String alias);

    @Override
    public CaseWhenBuilder<CriteriaBuilder<Tuple>> selectCase();

    @Override
    public CaseWhenBuilder<CriteriaBuilder<Tuple>> selectCase(String alias);
    
    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);

    @Override
    public CriteriaBuilder<Tuple> select(String expression);

    @Override
    public CriteriaBuilder<Tuple> select(String expression, String alias);

    @Override
    public SubqueryInitiator<CriteriaBuilder<Tuple>> selectSubquery();

    @Override
    public SubqueryInitiator<CriteriaBuilder<Tuple>> selectSubquery(String alias);
    
    // TODO: add SubqueryInitiator<CriteriaBuilder<Tuple>> selectSubquery(String expression, String subqueryAlias, String selectAlias)
}
