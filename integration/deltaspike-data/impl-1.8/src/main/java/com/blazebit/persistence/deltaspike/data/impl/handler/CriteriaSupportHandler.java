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

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.deltaspike.data.base.criteria.QueryCriteria;
import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewContext;
import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewDelegateQueryHandler;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.apache.deltaspike.data.api.criteria.QuerySelection;
import org.apache.deltaspike.data.impl.criteria.selection.AttributeQuerySelection;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Abs;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Avg;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Count;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.CountDistinct;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Max;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Min;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Modulo;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Neg;
import org.apache.deltaspike.data.impl.criteria.selection.numeric.Sum;
import org.apache.deltaspike.data.impl.criteria.selection.strings.Lower;
import org.apache.deltaspike.data.impl.criteria.selection.strings.SubstringFrom;
import org.apache.deltaspike.data.impl.criteria.selection.strings.SubstringFromTo;
import org.apache.deltaspike.data.impl.criteria.selection.strings.Trim;
import org.apache.deltaspike.data.impl.criteria.selection.strings.Upper;
import org.apache.deltaspike.data.impl.criteria.selection.temporal.CurrentDate;
import org.apache.deltaspike.data.impl.criteria.selection.temporal.CurrentTime;
import org.apache.deltaspike.data.impl.criteria.selection.temporal.CurrentTimestamp;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Dependent
public class CriteriaSupportHandler<E> implements CriteriaSupport<E>, EntityViewDelegateQueryHandler {

    @Inject
    @EntityViewContext
    private EntityViewCdiQueryInvocationContext context;
    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    @SuppressWarnings("unchecked")
    private Class<E> getEntityClass() {
        return (Class<E>) context.getEntityClass();
    }

    private EntityManager getEntityManager() {
        return context.getEntityManager();
    }

    @Override
    public Criteria<E, E> criteria() {
        return new QueryCriteria<E, E>(getEntityClass(), getEntityClass(), getEntityManager(), criteriaBuilderFactory, context.getEntityViewManager());
    }

    @Override
    public <T> Criteria<T, T> where(Class<T> clazz) {
        return new QueryCriteria<T, T>(clazz, clazz, getEntityManager(), criteriaBuilderFactory, context.getEntityViewManager());
    }

    @Override
    public <T> Criteria<T, T> where(Class<T> clazz, JoinType joinType) {
        return new QueryCriteria<T, T>(clazz, clazz, getEntityManager(), joinType, criteriaBuilderFactory, context.getEntityViewManager());
    }

    // The rest of this class is copied from org.apache.deltaspike.data.impl.handler.CriteriaSupportHandler

    @Override
    public <X> QuerySelection<E, X> attribute(SingularAttribute<? super E, X> attribute) {
        return new AttributeQuerySelection<E, X>(attribute);
    }

    // ----------------------------------------------------------------------------
    // NUMERIC QUERY SELECTION
    // ----------------------------------------------------------------------------

    @Override
    public <N extends Number> QuerySelection<E, N> abs(SingularAttribute<? super E, N> attribute) {
        return new Abs<E, N>(attribute);
    }

    @Override
    public <N extends Number> QuerySelection<E, N> avg(SingularAttribute<? super E, N> attribute) {
        return new Avg<E, N>(attribute);
    }

    @Override
    public QuerySelection<E, Long> count(SingularAttribute<? super E, ?> attribute) {
        return new Count<E>(attribute);
    }

    @Override
    public QuerySelection<E, Long> countDistinct(SingularAttribute<? super E, ?> attribute) {
        return new CountDistinct<E>(attribute);
    }

    @Override
    public <N extends Number> QuerySelection<E, N> max(SingularAttribute<? super E, N> attribute) {
        return new Max<E, N>(attribute);
    }

    @Override
    public <N extends Number> QuerySelection<E, N> min(SingularAttribute<? super E, N> attribute) {
        return new Min<E, N>(attribute);
    }

    @Override
    public <N extends Number> QuerySelection<E, N> neg(SingularAttribute<? super E, N> attribute) {
        return new Neg<E, N>(attribute);
    }

    @Override
    public <N extends Number> QuerySelection<E, N> sum(SingularAttribute<? super E, N> attribute) {
        return new Sum<E, N>(attribute);
    }

    @Override
    public QuerySelection<E, Integer> modulo(SingularAttribute<? super E, Integer> attribute, Integer modulo) {
        return new Modulo<E>(attribute, modulo);
    }

    // ----------------------------------------------------------------------------
    // STRING QUERY SELECTION
    // ----------------------------------------------------------------------------

    @Override
    public QuerySelection<E, String> upper(SingularAttribute<? super E, String> attribute) {
        return new Upper<E>(attribute);
    }

    @Override
    public QuerySelection<E, String> lower(SingularAttribute<? super E, String> attribute) {
        return new Lower<E>(attribute);
    }

    @Override
    public QuerySelection<E, String> substring(SingularAttribute<? super E, String> attribute, int from) {
        return new SubstringFrom<E>(attribute, from);
    }

    @Override
    public QuerySelection<E, String> substring(SingularAttribute<? super E, String> attribute, int from, int length) {
        return new SubstringFromTo<E>(attribute, from, length);
    }

    @Override
    public QuerySelection<E, String> trim(SingularAttribute<? super E, String> attribute) {
        return new Trim<E>(attribute);
    }

    @Override
    public QuerySelection<E, String> trim(CriteriaBuilder.Trimspec trimspec,
                                          SingularAttribute<? super E, String> attribute) {
        return new Trim<E>(trimspec, attribute);
    }

    // ----------------------------------------------------------------------------
    // TEMPORAL QUERY SELECTION
    // ----------------------------------------------------------------------------

    @Override
    public QuerySelection<E, Date> currDate() {
        return new CurrentDate<E>();
    }

    @Override
    public QuerySelection<E, Time> currTime() {
        return new CurrentTime<E>();
    }

    @Override
    public QuerySelection<E, Timestamp> currTStamp() {
        return new CurrentTimestamp<E>();
    }
}
