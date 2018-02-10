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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedBatchTupleListTransformer extends AbstractCorrelatedTupleListTransformer {

    private static final String CORRELATION_KEY_ALIAS = "correlationKey";
    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    protected final int batchSize;
    protected final boolean expectBatchCorrelationValues;

    protected String correlationParamName;
    protected String correlationSelectExpression;
    protected CriteriaBuilder<?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected Query query;

    public AbstractCorrelatedBatchTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                       int tupleIndex, int defaultBatchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.batchSize = entityViewConfiguration.getBatchSize(attributePath, defaultBatchSize);
        this.expectBatchCorrelationValues = entityViewConfiguration.getExpectBatchCorrelationValues(attributePath);
    }

    private String generateCorrelationParamName() {
        final FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        final Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();
        int paramNumber = 0;
        String paramName;
        while (true) {
            paramName = CORRELATION_PARAM_PREFIX + paramNumber;
            if (queryBuilder.getParameter(paramName) != null) {
                paramNumber++;
            } else if (optionalParameters.containsKey(paramName)) {
                paramNumber++;
            } else {
                return paramName;
            }
        }
    }

    private String applyAndGetCorrelationRoot(boolean batchCorrelationValues) {
        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        String idAttributePath = getEntityIdName(viewRootEntityClass);

        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType;
        String viewRootExpression;
        if (batchCorrelationValues) {
            correlationBasisEntityType = correlationBasisEntity;
            viewRootExpression = null;
        } else {
            correlationBasisEntityType = viewRootEntityClass;
            viewRootExpression = CORRELATION_KEY_ALIAS;
        }

        this.criteriaBuilder = queryBuilder.getCriteriaBuilderFactory().create(queryBuilder.getEntityManager(), Object[].class);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, viewRootEntityClass, idAttributePath, viewRootExpression);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationAlias, correlationResult, correlationBasisType, correlationBasisEntityType, CORRELATION_KEY_ALIAS, batchSize, false, attributePath);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        String correlationKeyExpression;
        if (batchSize > 1) {
            if (batchCorrelationValues) {
                this.correlationParamName = CORRELATION_KEY_ALIAS;
            } else {
                this.correlationParamName = generateCorrelationParamName();
            }
            if (correlationBasisEntityType != null) {
                correlationKeyExpression = CORRELATION_KEY_ALIAS;
                if (batchCorrelationValues) {
                    correlationSelectExpression = CORRELATION_KEY_ALIAS + '.' + getEntityIdName(correlationBasisEntityType);
                } else {
                    correlationSelectExpression = CORRELATION_KEY_ALIAS + '.' + idAttributePath;
                }
            } else {
                // The correlation key is basic type
                correlationSelectExpression = correlationKeyExpression = CORRELATION_KEY_ALIAS + ".value";
            }
        } else {
            this.correlationParamName = generateCorrelationParamName();
            this.correlationSelectExpression = correlationKeyExpression = null;
        }

        if (batchSize > 1 && batchCorrelationValues) {
            provider.applyCorrelation(correlationBuilder, correlationKeyExpression);
        } else {
            provider.applyCorrelation(correlationBuilder, ':' + correlationParamName);
        }

        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                criteriaBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }

        return correlationBuilder.getCorrelationRoot();
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        FixedArrayList correlationParams = new FixedArrayList(batchSize);
        // We have the correlation key on the first position if we do batching
        int tupleOffset = batchSize > 1 ? 1 : 0;
        // Implementation detail: the tuple list is a LinkedList
        Iterator<Object[]> tupleListIter = tuples.iterator();

        final String correlationRoot = applyAndGetCorrelationRoot(expectBatchCorrelationValues);
        EntityManager em = criteriaBuilder.getEntityManager();

        // If view root is used, we have to decide whether we do batches for each view root id or correlation param
        if (viewRootJpqlMacro.usesViewRoot()) {
            int totalSize = tuples.size();
            Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);
            Map<Object, Map<Object, TuplePromise>> correlationValues = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);

            // Group tuples by view roots and correlation values and create tuple promises
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object viewRootKey = tuple[0];
                Object correlationValueKey = tuple[startIndex];

                Map<Object, TuplePromise> viewRootCorrelationValues = viewRoots.get(viewRootKey);
                if (viewRootCorrelationValues == null) {
                    viewRootCorrelationValues = new HashMap<Object, TuplePromise>();
                    viewRoots.put(viewRootKey, viewRootCorrelationValues);
                }
                TuplePromise viewRootPromise = viewRootCorrelationValues.get(correlationValueKey);
                if (viewRootPromise == null) {
                    viewRootPromise = new TuplePromise(startIndex);
                    viewRootCorrelationValues.put(correlationValueKey, viewRootPromise);
                }
                viewRootPromise.add(tuple);

                Map<Object, TuplePromise> correlationValueViewRoots = correlationValues.get(correlationValueKey);
                if (correlationValueViewRoots == null) {
                    correlationValueViewRoots = new HashMap<Object, TuplePromise>();
                    correlationValues.put(correlationValueKey, correlationValueViewRoots);
                }
                TuplePromise correlationValuePromise = correlationValueViewRoots.get(viewRootKey);
                if (correlationValuePromise == null) {
                    correlationValuePromise = new TuplePromise(startIndex);
                    correlationValueViewRoots.put(viewRootKey, correlationValuePromise);
                }
                correlationValuePromise.add(tuple);
            }

            boolean batchCorrelationValues = viewRoots.size() <= correlationValues.size();
            FixedArrayList viewRootIds = new FixedArrayList(batchSize);

            if (batchCorrelationValues) {
                if (batchSize > 1) {
                    // If the expectation was wrong, we have to create a new criteria builder
                    if (!expectBatchCorrelationValues) {
                        applyAndGetCorrelationRoot(true);
                    }

                    criteriaBuilder.select(correlationSelectExpression);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot);
                populateParameters(criteriaBuilder);
                query = criteriaBuilder.getQuery();

                for (Map.Entry<Object, Map<Object, TuplePromise>> batchEntry : viewRoots.entrySet()) {
                    Map<Object, TuplePromise> batchValues = batchEntry.getValue();
                    for (Map.Entry<Object, TuplePromise> batchValueEntry : batchValues.entrySet()) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(em.getReference(correlationBasisEntity, batchValueEntry.getKey()));
                        } else {
                            correlationParams.add(batchValueEntry.getKey());
                        }

                        if (batchSize == correlationParams.realSize()) {
                            viewRootIds.add(batchEntry.getKey());
                            Object defaultKey;
                            if (correlationBasisEntity != null) {
                                defaultKey = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(correlationParams.get(0));
                            } else {
                                defaultKey = correlationParams.get(0);
                            }
                            batchLoad(batchValues, correlationParams, viewRootIds, defaultKey, true);
                        }
                    }

                    if (correlationParams.realSize() > 0) {
                        viewRootIds.add(batchEntry.getKey());
                        batchLoad(batchValues, correlationParams, viewRootIds, null, true);
                    }
                }

                fillDefaultValues(viewRoots);
            } else {
                if (batchSize > 1) {
                    // If the expectation was wrong, we have to create a new criteria builder
                    if (expectBatchCorrelationValues) {
                        applyAndGetCorrelationRoot(false);
                    }

                    criteriaBuilder.select(correlationSelectExpression);
                }
                correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot);
                populateParameters(criteriaBuilder);
                query = criteriaBuilder.getQuery();

                for (Map.Entry<Object, Map<Object, TuplePromise>> batchEntry : correlationValues.entrySet()) {
                    Map<Object, TuplePromise> batchValues = batchEntry.getValue();
                    for (Map.Entry<Object, TuplePromise> batchValueEntry : batchValues.entrySet()) {
                        if (viewRootJpqlMacro.usesViewRootEntityParameter()) {
                            viewRootIds.add(em.getReference(viewRootType.getEntityClass(), batchValueEntry.getKey()));
                        } else {
                            viewRootIds.add(batchValueEntry.getKey());
                        }

                        if (batchSize == viewRootIds.realSize()) {
                            if (correlationBasisEntity != null) {
                                correlationParams.add(em.getReference(correlationBasisEntity, batchEntry.getKey()));
                            } else {
                                correlationParams.add(batchEntry.getKey());
                            }
                            Object defaultKey;
                            if (viewRootJpqlMacro.usesViewRootEntityParameter()) {
                                defaultKey = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(viewRootIds.get(0));
                            } else {
                                defaultKey = viewRootIds.get(0);
                            }
                            batchLoad(batchValues, correlationParams, viewRootIds, defaultKey, false);
                        }
                    }

                    if (viewRootIds.realSize() > 0) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(em.getReference(correlationBasisEntity, batchEntry.getKey()));
                        } else {
                            correlationParams.add(batchEntry.getKey());
                        }
                        batchLoad(batchValues, correlationParams, viewRootIds, null, false);
                    }
                }

                fillDefaultValues(correlationValues);
            }
        } else {
            if (batchSize > 1) {
                // If the expectation was wrong, we have to create a new criteria builder
                if (!expectBatchCorrelationValues) {
                    applyAndGetCorrelationRoot(true);
                }

                criteriaBuilder.select(correlationSelectExpression);
            }
            correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot);
            populateParameters(criteriaBuilder);
            query = criteriaBuilder.getQuery();

            Map<Object, TuplePromise> correlationValues = new HashMap<Object, TuplePromise>(tuples.size());
            while (tupleListIter.hasNext()) {
                Object[] tuple = tupleListIter.next();
                Object correlationValue = tuple[startIndex];
                TuplePromise tupleIndexValue = correlationValues.get(correlationValue);

                if (tupleIndexValue == null) {
                    tupleIndexValue = new TuplePromise(startIndex);
                    tupleIndexValue.add(tuple);
                    correlationValues.put(correlationValue, tupleIndexValue);

                    if (correlationBasisEntity != null) {
                        correlationParams.add(em.getReference(correlationBasisEntity, tuple[startIndex]));
                    } else {
                        correlationParams.add(tuple[startIndex]);
                    }

                    if (batchSize == correlationParams.realSize()) {
                        Object defaultKey;
                        if (correlationBasisEntity != null) {
                            defaultKey = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(correlationParams.get(0));
                        } else {
                            defaultKey = correlationParams.get(0);
                        }
                        batchLoad(correlationValues, correlationParams, null, defaultKey, batchSize > 1);
                    }
                } else {
                    tupleIndexValue.add(tuple);
                }
            }

            if (correlationParams.realSize() > 0) {
                batchLoad(correlationValues, correlationParams, null, null, batchSize > 1);
            }

            fillDefaultValues(Collections.singletonMap(null, correlationValues));
        }

        return tuples;
    }

    private void batchLoad(Map<Object, TuplePromise> correlationValues, FixedArrayList batchParameters, FixedArrayList viewRootIds, Object defaultKey, boolean batchCorrelationValues) {
        batchParameters.clearRest();
        if (batchCorrelationValues) {
            query.setParameter(correlationParamName, batchParameters);
        } else {
            query.setParameter(correlationParamName, batchParameters.get(0));
        }

        if (viewRootIds != null) {
            viewRootIds.clearRest();
            if (viewRootIds.size() == 1) {
                viewRootJpqlMacro.setParameters(query, viewRootIds.get(0));
            } else {
                viewRootJpqlMacro.setParameters(query, viewRootIds);
            }
        }

        populateResult(correlationValues, defaultKey, (List<Object>) query.getResultList());

        batchParameters.reset();
        if (viewRootIds != null) {
            viewRootIds.reset();
        }
    }

    protected abstract void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object> list);

}
