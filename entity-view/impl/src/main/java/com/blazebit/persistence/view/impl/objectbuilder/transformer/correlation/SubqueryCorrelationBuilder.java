/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.impl.builder.predicate.AbstractQuantifiablePredicateBuilder;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

import javax.persistence.metamodel.EntityType;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryCorrelationBuilder implements CorrelationBuilder {

    private final ParameterHolder<?> parameterHolder;
    private final Map<String, Object> optionalParameters;
    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final String correlationAlias;
    private final String correlationExternalAlias;
    private final String correlationResult;
    private final Class<?> correlationBasisType;
    private final Class<?> correlationBasisEntity;
    private final String correlationJoinBase;
    private final String attributePath;
    private final int batchSize;
    private final Limiter limiter;
    private final boolean correlateJoinBase;
    private String correlationRoot;
    private Object correlationBuilder;

    public SubqueryCorrelationBuilder(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, FullQueryBuilder<?, ?> criteriaBuilder, String correlationAlias, String correlationExternalAlias, String correlationResult, Class<?> correlationBasisType, Class<?> correlationBasisEntity, String correlationJoinBase, String attributePath, int batchSize,
                                      Limiter limiter, boolean correlateJoinBase) {
        this.parameterHolder = parameterHolder;
        this.optionalParameters = optionalParameters;
        this.criteriaBuilder = criteriaBuilder;
        this.correlationAlias = correlationAlias;
        this.correlationExternalAlias = correlationExternalAlias;
        this.correlationResult = correlationResult;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.correlationJoinBase = correlationJoinBase;
        this.attributePath = attributePath;
        this.batchSize = batchSize;
        this.correlateJoinBase = correlateJoinBase;
        this.limiter = limiter;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return criteriaBuilder.getService(serviceClass);
    }

    @Override
    public FromProvider getCorrelationFromProvider() {
        return criteriaBuilder;
    }

    @Override
    public String getCorrelationAlias() {
        return correlationAlias;
    }

    public String getCorrelationRoot() {
        if (correlationBuilder instanceof SubqueryBuilder<?>) {
            ((SubqueryBuilder<?>) correlationBuilder).end();
        } else if (correlationBuilder instanceof FullSelectCTECriteriaBuilder<?>) {
            ((FullSelectCTECriteriaBuilder<?>) correlationBuilder).end();
        }
        return correlationRoot;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        if (correlationRoot != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        JoinOnBuilder<CorrelationQueryBuilder> correlationBuilder;
        if (batchSize > 1) {
            if (correlationBasisEntity != null) {
                criteriaBuilder.fromIdentifiableValues(correlationBasisEntity, correlationJoinBase, batchSize);
            } else {
                criteriaBuilder.fromValues(correlationBasisType, correlationJoinBase, batchSize);
            }

            if (limiter == null) {
                correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityClass, correlationAlias);
            } else {
                BaseFromQueryBuilder<?, ?> lateralBuilder;
                if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                    lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityClass, correlationExternalAlias).on(correlationExternalAlias).eq())
                        .from(entityClass, correlationAlias, true).select(correlationAlias);
                } else {
                    checkLimitSupport();
                    lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationJoinBase, entityClass, correlationExternalAlias, correlationAlias);
                }
                limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                this.correlationBuilder = lateralBuilder;
                correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
            }
        } else {
            if (limiter == null) {
                if (correlateJoinBase) {
                    correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityClass, correlationAlias);
                } else {
                    criteriaBuilder.from(entityClass, correlationAlias);
                    correlationBuilder = criteriaBuilder.getService(JoinOnBuilder.class);
                }
            } else {
                if (correlateJoinBase) {
                    BaseFromQueryBuilder<?, ?> lateralBuilder;
                    if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                        lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityClass, correlationExternalAlias).on(correlationExternalAlias).eq())
                            .from(entityClass, correlationAlias, true).select(correlationAlias);
                    } else {
                        lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationJoinBase, entityClass, correlationExternalAlias, correlationAlias);
                    }
                    limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                    this.correlationBuilder = lateralBuilder;
                    correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
                } else {
                    criteriaBuilder.from(entityClass, correlationExternalAlias);
                    SubqueryBuilder<?> subqueryBuilder;
                    if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1) {
                        subqueryBuilder = criteriaBuilder.where(correlationExternalAlias).eq().from(entityClass, correlationAlias);
                    } else {
                        subqueryBuilder = criteriaBuilder.where(correlationExternalAlias).in().from(entityClass, correlationAlias);
                    }
                    limiter.apply(parameterHolder, optionalParameters, subqueryBuilder);
                    this.correlationBuilder = subqueryBuilder;
                    correlationBuilder = subqueryBuilder.getService(JoinOnBuilder.class);
                }
            }
        }

        this.correlationRoot = correlationResult;
        return correlationBuilder;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
        if (correlationRoot != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        JoinOnBuilder<CorrelationQueryBuilder> correlationBuilder;
        if (batchSize > 1) {
            if (correlationBasisEntity != null) {
                criteriaBuilder.fromIdentifiableValues(correlationBasisEntity, correlationJoinBase, batchSize);
            } else {
                criteriaBuilder.fromValues(correlationBasisType, correlationJoinBase, batchSize);
            }

            if (limiter == null) {
                correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(entityType, correlationAlias);
            } else {
                BaseFromQueryBuilder<?, ?> lateralBuilder;
                if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                    lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityType, correlationExternalAlias).on(correlationExternalAlias).eq())
                        .from(entityType, correlationAlias, true).select(correlationAlias);
                } else {
                    checkLimitSupport();
                    lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationJoinBase, entityType, correlationExternalAlias, correlationAlias);
                }
                limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                this.correlationBuilder = lateralBuilder;
                correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
            }
        } else {
            if (limiter == null) {
                if (correlateJoinBase) {
                    correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityType, correlationAlias);
                } else {
                    criteriaBuilder.from(entityType, correlationAlias);
                    correlationBuilder = criteriaBuilder.getService(JoinOnBuilder.class);
                }
            } else {
                checkLimitSupport();
                BaseFromQueryBuilder<?, ?> lateralBuilder;
                if (correlateJoinBase) {
                    if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                        lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationJoinBase, entityType, correlationExternalAlias).on(correlationExternalAlias).eq())
                            .from(entityType, correlationAlias, true).select(correlationAlias);
                    } else {
                        checkLimitSupport();
                        lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationJoinBase, entityType, correlationExternalAlias, correlationAlias);
                    }
                } else {
                    if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                        lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(entityType, correlationExternalAlias).on(correlationExternalAlias).eq())
                            .from(entityType, correlationAlias, true).select(correlationAlias);
                    } else {
                        checkLimitSupport();
                        lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(entityType, correlationExternalAlias, correlationAlias);
                    }
                }
                limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                this.correlationBuilder = lateralBuilder;
                correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
            }
        }

        this.correlationRoot = correlationResult;
        return correlationBuilder;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(String correlationPath) {
        if (correlationRoot != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        JoinOnBuilder<CorrelationQueryBuilder> correlationBuilder;
        if (batchSize > 1) {
            if (correlationBasisEntity != null) {
                criteriaBuilder.fromIdentifiableValues(correlationBasisEntity, correlationJoinBase, batchSize);
            } else {
                criteriaBuilder.fromValues(correlationBasisType, correlationJoinBase, batchSize);
            }

            if (limiter == null) {
                correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(correlationPath, correlationAlias);
            } else {
                BaseFromQueryBuilder<?, ?> lateralBuilder;
                if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                    lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationPath, correlationExternalAlias).on(correlationExternalAlias).eq())
                        .from(correlationPath, correlationAlias, true).select(correlationAlias);
                } else {
                    checkLimitSupport();
                    lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationPath, correlationExternalAlias, correlationAlias);
                }
                limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                this.correlationBuilder = lateralBuilder;
                correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
            }
        } else {
            if (limiter == null) {
                correlationBuilder = (JoinOnBuilder<CorrelationQueryBuilder>) (JoinOnBuilder<?>) criteriaBuilder.innerJoinOn(correlationPath, correlationAlias);
            } else {
                BaseFromQueryBuilder<?, ?> lateralBuilder;
                if (limiter.getLimitValue() != null && limiter.getLimitValue() == 1 && getService(DbmsDialect.class).getLateralStyle() == LateralStyle.NONE) {
                    lateralBuilder = ((AbstractQuantifiablePredicateBuilder<?>) criteriaBuilder.innerJoinOn(correlationPath, correlationExternalAlias).on(correlationExternalAlias).eq())
                        .from(correlationPath, correlationAlias, true).select(correlationAlias);
                } else {
                    checkLimitSupport();
                    lateralBuilder = criteriaBuilder.innerJoinLateralEntitySubquery(correlationPath, correlationExternalAlias, correlationAlias);
                }
                limiter.apply(parameterHolder, optionalParameters, lateralBuilder);
                this.correlationBuilder = lateralBuilder;
                correlationBuilder = lateralBuilder.getService(JoinOnBuilder.class);
            }
        }

        this.correlationRoot = correlationResult;
        return correlationBuilder;
    }

    private void checkLimitSupport() {
        DbmsDialect dbmsDialect = getService(DbmsDialect.class);
        if (dbmsDialect.getLateralStyle() == LateralStyle.NONE && !dbmsDialect.supportsLimitInQuantifiedPredicateSubquery()) {
            throw new IllegalStateException("Can't limit the amount of elements for the attribute path " + attributePath + " because the DBMS doesn't support lateral or the use of LIMIT in quantified predicates! Use the SELECT strategy with batch size 1 if you really need this.");
        }
    }
}
