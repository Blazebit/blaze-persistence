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
 *
 * @author Christian
 */
public interface BaseQueryBuilder<X> extends Aggregateable<RestrictionBuilder<? extends X>>, Filterable<RestrictionBuilder<? extends X>> {
    
    public String getQueryString();
    /*
     * Join methods
     */
    public X join(String path, String alias, JoinType type);

    public X innerJoin(String path, String alias);

    public X leftJoin(String path, String alias);

    public X outerJoin(String path, String alias);

    public X rightJoin(String path, String alias);
    
    /*
     * Select methods
     */
    public X distinct();
    
    public CaseWhenBuilder<? extends X> selectCase();

    /* CASE caseOperand (WHEN scalarExpression THEN scalarExpression)+ ELSE scalarExpression END */
    public SimpleCaseWhenBuilder<? extends X> selectCase(String expression);

    public CriteriaBuilder<Tuple> select(String expression);

    public CriteriaBuilder<Tuple> select(String expression, String alias);
    
    /*
     * Order by methods
     */
    public X orderBy(String expression, boolean ascending, boolean nullFirst);

    public X orderByAsc(String expression);

    public X orderByAsc(String expression, boolean nullFirst);

    public X orderByDesc(String expression);

    public X orderByDesc(String expression, boolean nullFirst);

    /*
     * Where methods
     */
    @Override
    public RestrictionBuilder<? extends X> where(String expression);

    public WhereOrBuilder<? extends X> whereOr();

    /*
     * Group by methods
     */
    public X groupBy(String... expressions);

    public X groupBy(String expression);

    /*
     * Having methods
     */
    @Override
    public RestrictionBuilder<? extends X> having(String expression);

    public HavingOrBuilder<? extends X> havingOr();
}
