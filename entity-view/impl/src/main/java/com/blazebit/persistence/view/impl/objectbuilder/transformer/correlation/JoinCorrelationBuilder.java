/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.BaseFromQueryBuilder;
import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.FromBuilder;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.impl.builder.predicate.AbstractQuantifiablePredicateBuilder;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

import javax.persistence.metamodel.EntityType;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JoinCorrelationBuilder implements CorrelationBuilder {

    private final ParameterHolder<?> parameterHolder;
    private final Map<String, Object> optionalParameters;
    private final FromBuilder<?> criteriaBuilder;
    private final String joinBase;
    private final String correlationAlias;
    private final String correlationExternalAlias;
    private final String attributePath;
    private final JoinType joinType;
    private final Limiter limiter;
    private boolean correlated;
    private Object correlationBuilder;

    public JoinCorrelationBuilder(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, FromBuilder<?> criteriaBuilder, String joinBase, String correlationAlias, String correlationExternalAlias, String attributePath, JoinType joinType, Limiter limiter) {
        this.parameterHolder = parameterHolder;
        this.optionalParameters = optionalParameters;
        this.criteriaBuilder = criteriaBuilder;
        this.joinBase = joinBase;
        this.correlationAlias = correlationAlias;
        this.correlationExternalAlias = correlationExternalAlias;
        this.attributePath = attributePath;
        this.joinType = joinType;
        this.limiter = limiter;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return ((ServiceProvider) criteriaBuilder).getService(serviceClass);
    }

    @Override
    public FromProvider getCorrelationFromProvider() {
        return criteriaBuilder;
    }

    @Override
    public String getCorrelationAlias() {
        return correlationAlias;
    }

    public void finish() {
        if (correlationBuilder instanceof SubqueryBuilder<?>) {
            ((SubqueryBuilder<?>) correlationBuilder).end();
        } else  if (correlationBuilder instanceof FullSelectCTECriteriaBuilder<?>) {
            ((FullSelectCTECriteriaBuilder<?>) correlationBuilder).end();
        }
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        if (correlated) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        correlated = true;
        if (limiter == null) {
            return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.joinOn(joinBase, entityClass, correlationAlias, joinType);
        } else {
            BaseFromQueryBuilder<?, ?> lateralBuilder;
            if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.joinOn(joinBase, entityClass, correlationExternalAlias, joinType).on(correlationExternalAlias).eq())
                    .from(entityClass, correlationAlias, true).select(correlationAlias);
            } else {
                checkLimitSupport();
                lateralBuilder = criteriaBuilder.joinLateralEntitySubquery(joinBase, entityClass, correlationExternalAlias, correlationAlias, joinType);
            }
            limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
            this.correlationBuilder = lateralBuilder;
            return lateralBuilder.getService(JoinOnBuilder.class);
        }
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
        if (correlated) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        correlated = true;
        if (limiter == null) {
            return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.joinOn(joinBase, entityType, correlationAlias, joinType);
        } else {
            BaseFromQueryBuilder<?, ?> lateralBuilder;
            if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.joinOn(joinBase, entityType, correlationExternalAlias, joinType).on(correlationExternalAlias).eq("a", "a"))
                    .from(entityType, correlationAlias, true).select(correlationAlias);
            } else {
                checkLimitSupport();
                lateralBuilder = criteriaBuilder.joinLateralEntitySubquery(joinBase, entityType, correlationExternalAlias, correlationAlias, joinType);
            }
            limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
            this.correlationBuilder = lateralBuilder;
            return lateralBuilder.getService(JoinOnBuilder.class);
        }
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(String correlationPath) {
        if (correlated) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        correlated = true;
        if (limiter == null) {
            return (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.joinOn(correlationPath, correlationAlias, joinType);
        } else {
            BaseFromQueryBuilder<?, ?> lateralBuilder;
            if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.joinOn(correlationPath, correlationExternalAlias, joinType).on(correlationExternalAlias).eq("a", "a"))
                    .from(correlationPath, correlationAlias, true).select(correlationAlias);
            } else {
                checkLimitSupport();
                lateralBuilder = criteriaBuilder.joinLateralEntitySubquery(correlationPath, correlationExternalAlias, correlationAlias, joinType);
            }
            limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
            this.correlationBuilder = lateralBuilder;
            return lateralBuilder.getService(JoinOnBuilder.class);
        }
    }

    private void checkLimitSupport() {
        DbmsDialect dbmsDialect = getService(DbmsDialect.class);
        if (dbmsDialect.getLateralStyle() == LateralStyle.NONE && !dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
            throw new IllegalStateException("Can't limit the amount of elements for the attribute path " + attributePath + " because the DBMS doesn't support lateral or the use of LIMIT in quantified predicates! Use the SELECT strategy with batch size 1 if you really need this.");
        }
    }
}
