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

import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.BatchCorrelationMode;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryEmbeddingViewJpqlMacro;
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

    public static final String CORRELATION_KEY_ALIAS = "correlationKey";
    protected static final int VALUE_INDEX = 0;
    protected static final int KEY_INDEX = 1;
    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    protected final int batchSize;
    protected final boolean correlatesThis;
    protected final BatchCorrelationMode expectBatchCorrelationMode;

    protected String correlationParamName;
    protected String correlationSelectExpression;
    protected CriteriaBuilder<?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected CorrelatedSubqueryEmbeddingViewJpqlMacro embeddingViewJpqlMacro;
    protected Query query;

    public AbstractCorrelatedBatchTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, ManagedViewType<?> embeddingViewType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                       boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int defaultBatchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.batchSize = entityViewConfiguration.getBatchSize(attributePath, defaultBatchSize);
        this.correlatesThis = correlatesThis;
        this.expectBatchCorrelationMode = entityViewConfiguration.getExpectBatchCorrelationValues(attributePath);
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

    private String applyAndGetCorrelationRoot(BatchCorrelationMode batchCorrelationMode) {
        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        Class<?> embeddingViewEntityClass = embeddingViewType.getEntityClass();
        String viewRootIdAttributePath = getEntityIdName(viewRootEntityClass);
        String embeddingViewIdAttributePath = getEntityIdName(embeddingViewEntityClass);

        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType;
        String viewRootExpression;
        String embeddingViewExpression;
        boolean batchedIdValues = false;
        if (batchCorrelationMode == BatchCorrelationMode.VALUES) {
            correlationBasisEntityType = correlationBasisEntity;
            viewRootExpression = null;
            batchedIdValues = correlatesThis && correlationBasisEntity == null;
            embeddingViewExpression = correlatesThis ? CORRELATION_KEY_ALIAS : null;
        } else if (batchCorrelationMode == BatchCorrelationMode.VIEW_ROOTS) {
            correlationBasisEntityType = viewRootEntityClass;
            viewRootExpression = CORRELATION_KEY_ALIAS;
            embeddingViewExpression = null;
        } else {
            correlationBasisEntityType = embeddingViewEntityClass;
            viewRootExpression = null;
            embeddingViewExpression = CORRELATION_KEY_ALIAS;
        }

        this.criteriaBuilder = queryBuilder.getCriteriaBuilderFactory().create(queryBuilder.getEntityManager(), Object[].class);
        if (queryBuilder instanceof CTEBuilder<?>) {
            this.criteriaBuilder.withCtesFrom((CTEBuilder<?>) queryBuilder);
        }
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, viewRootExpression != null, viewRootEntityClass, viewRootIdAttributePath, viewRootExpression);
        this.embeddingViewJpqlMacro = new CorrelatedSubqueryEmbeddingViewJpqlMacro(criteriaBuilder, optionalParameters, embeddingViewExpression != null, embeddingViewEntityClass, embeddingViewIdAttributePath, embeddingViewExpression, batchedIdValues, viewRootJpqlMacro);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);
        this.criteriaBuilder.registerMacro("embedding_view", embeddingViewJpqlMacro);

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationAlias, correlationResult, correlationBasisType, correlationBasisEntityType, CORRELATION_KEY_ALIAS, batchSize, false, attributePath);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        String correlationKeyExpression;
        if (batchSize > 1) {
            if (batchCorrelationMode == BatchCorrelationMode.VALUES) {
                this.correlationParamName = CORRELATION_KEY_ALIAS;
                // TODO: when using EMBEDDING_VIEW, we could make use of correlationBasis instead of binding parameters separately
            } else {
                this.correlationParamName = generateCorrelationParamName();
            }
            if (correlationBasisEntityType != null) {
                correlationKeyExpression = CORRELATION_KEY_ALIAS;
                if (batchCorrelationMode == BatchCorrelationMode.VALUES) {
                    correlationSelectExpression = CORRELATION_KEY_ALIAS + '.' + getEntityIdName(correlationBasisEntityType);
                } else {
                    correlationSelectExpression = CORRELATION_KEY_ALIAS + '.' + viewRootIdAttributePath;
                }
            } else {
                // The correlation key is basic type
                correlationSelectExpression = correlationKeyExpression = CORRELATION_KEY_ALIAS + ".value";
            }
        } else {
            this.correlationParamName = generateCorrelationParamName();
            this.correlationSelectExpression = correlationKeyExpression = null;
        }

        if (batchSize > 1 && batchCorrelationMode == BatchCorrelationMode.VALUES) {
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

        final String correlationRoot = applyAndGetCorrelationRoot(expectBatchCorrelationMode);
        // Add select items so that macros are properly used and we can query usage
        correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot, embeddingViewJpqlMacro);
        if (batchSize > 1) {
            criteriaBuilder.select(correlationSelectExpression);
        }

        // If a view macro is used, we have to decide whether we do batches for each view id or correlation param
        if (embeddingViewJpqlMacro.usesViewMacroNonId() || !correlatesThis && embeddingViewJpqlMacro.usesViewMacro()) {
            transformViewMacroAware(tuples, correlationParams, tupleOffset, correlationRoot, embeddingViewJpqlMacro, BatchCorrelationMode.EMBEDDING_VIEWS, embeddingViewType, embeddingViewIndex);
        } else if (viewRootJpqlMacro.usesViewMacro()) {
            transformViewMacroAware(tuples, correlationParams, tupleOffset, correlationRoot, viewRootJpqlMacro, BatchCorrelationMode.VIEW_ROOTS, viewRootType, viewRootIndex);
        } else {
            EntityManager em = criteriaBuilder.getEntityManager();
            // Implementation detail: the tuple list is a LinkedList
            Iterator<Object[]> tupleListIter = tuples.iterator();
            if (batchSize > 1) {
                // If the expectation was wrong, we have to create a new criteria builder
                if (expectBatchCorrelationMode != BatchCorrelationMode.VALUES) {
                    applyAndGetCorrelationRoot(BatchCorrelationMode.VALUES);
                    correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot, embeddingViewJpqlMacro);
                    criteriaBuilder.select(correlationSelectExpression);
                }
            }
            populateParameters(criteriaBuilder);
            query = criteriaBuilder.getQuery();

            Map<Object, TuplePromise> correlationValues = new HashMap<>(tuples.size());
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
                            defaultKey = jpaProvider.getIdentifier(correlationParams.get(0));
                        } else {
                            defaultKey = correlationParams.get(0);
                        }
                        batchLoad(correlationValues, correlationParams, null, defaultKey, viewRootJpqlMacro, BatchCorrelationMode.VALUES);
                    }
                } else {
                    tupleIndexValue.add(tuple);
                }
            }

            if (correlationParams.realSize() > 0) {
                batchLoad(correlationValues, correlationParams, null, null, viewRootJpqlMacro, BatchCorrelationMode.VALUES);
            }

            fillDefaultValues(Collections.singletonMap(null, correlationValues));
        }

        return tuples;
    }

    private void transformViewMacroAware(List<Object[]> tuples, FixedArrayList correlationParams, int tupleOffset, String correlationRoot, CorrelatedSubqueryViewRootJpqlMacro macro, BatchCorrelationMode correlationMode, ManagedViewType<?> viewType, int viewIndex) {
        EntityManager em = criteriaBuilder.getEntityManager();
        // Implementation detail: the tuple list is a LinkedList
        Iterator<Object[]> tupleListIter = tuples.iterator();
        int totalSize = tuples.size();
        Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<>(totalSize);
        Map<Object, Map<Object, TuplePromise>> correlationValues = new HashMap<>(totalSize);

        // Group tuples by view roots and correlation values and create tuple promises
        while (tupleListIter.hasNext()) {
            Object[] tuple = tupleListIter.next();
            Object viewRootKey = tuple[viewIndex];
            Object correlationValueKey = tuple[startIndex];

            Map<Object, TuplePromise> viewRootCorrelationValues = viewRoots.get(viewRootKey);
            if (viewRootCorrelationValues == null) {
                viewRootCorrelationValues = new HashMap<>();
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
                correlationValueViewRoots = new HashMap<>();
                correlationValues.put(correlationValueKey, correlationValueViewRoots);
            }
            TuplePromise correlationValuePromise = correlationValueViewRoots.get(viewRootKey);
            if (correlationValuePromise == null) {
                correlationValuePromise = new TuplePromise(startIndex);
                correlationValueViewRoots.put(viewRootKey, correlationValuePromise);
            }
            correlationValuePromise.add(tuple);
        }

        boolean batchCorrelationValues = !macro.usesViewMacro() && viewRoots.size() <= correlationValues.size();
        FixedArrayList viewRootIds = new FixedArrayList(batchSize);

        if (batchCorrelationValues) {
            if (batchSize > 1) {
                // If the expectation was wrong, we have to create a new criteria builder
                if (expectBatchCorrelationMode != BatchCorrelationMode.VALUES) {
                    applyAndGetCorrelationRoot(BatchCorrelationMode.VALUES);
                    macro = BatchCorrelationMode.VIEW_ROOTS == correlationMode ? viewRootJpqlMacro : embeddingViewJpqlMacro;
                    correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot, embeddingViewJpqlMacro);
                    criteriaBuilder.select(correlationSelectExpression);
                }
                macro.addBatchPredicate(criteriaBuilder);
            } else {
                // We have to bind the view id value, otherwise we might get wrong results
                macro.addIdParamPredicate(criteriaBuilder);
            }
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
                            defaultKey = jpaProvider.getIdentifier(correlationParams.get(0));
                        } else {
                            defaultKey = correlationParams.get(0);
                        }
                        batchLoad(batchValues, correlationParams, viewRootIds, defaultKey, macro, correlationMode);
                    }
                }

                if (correlationParams.realSize() > 0) {
                    viewRootIds.add(batchEntry.getKey());
                    batchLoad(batchValues, correlationParams, viewRootIds, null, macro, correlationMode);
                }
            }

            fillDefaultValues(viewRoots);
        } else {
            if (batchSize > 1) {
                // If the expectation was wrong, we have to create a new criteria builder
                if (expectBatchCorrelationMode != correlationMode) {
                    applyAndGetCorrelationRoot(correlationMode);
                    macro = BatchCorrelationMode.VIEW_ROOTS == correlationMode ? viewRootJpqlMacro : embeddingViewJpqlMacro;
                    correlator.finish(criteriaBuilder, entityViewConfiguration, tupleOffset, correlationRoot, embeddingViewJpqlMacro);
                    criteriaBuilder.select(correlationSelectExpression);
                }
                macro.addBatchPredicate(criteriaBuilder);
            } else {
                // We have to bind the view id value, otherwise we might get wrong results
                macro.addIdParamPredicate(criteriaBuilder);
            }
            populateParameters(criteriaBuilder);
            query = criteriaBuilder.getQuery();

            for (Map.Entry<Object, Map<Object, TuplePromise>> batchEntry : correlationValues.entrySet()) {
                Map<Object, TuplePromise> batchValues = batchEntry.getValue();
                for (Map.Entry<Object, TuplePromise> batchValueEntry : batchValues.entrySet()) {
                    viewRootIds.add(batchValueEntry.getKey());

                    if (batchSize == viewRootIds.realSize()) {
                        if (correlationBasisEntity != null) {
                            correlationParams.add(em.getReference(correlationBasisEntity, batchEntry.getKey()));
                        } else {
                            correlationParams.add(batchEntry.getKey());
                        }
                        Object defaultKey = viewRootIds.get(0);
                        batchLoad(batchValues, correlationParams, viewRootIds, defaultKey, macro, correlationMode);
                    }
                }

                if (viewRootIds.realSize() > 0) {
                    if (correlationBasisEntity != null) {
                        correlationParams.add(em.getReference(correlationBasisEntity, batchEntry.getKey()));
                    } else {
                        correlationParams.add(batchEntry.getKey());
                    }
                    batchLoad(batchValues, correlationParams, viewRootIds, null, macro, correlationMode);
                }
            }

            fillDefaultValues(correlationValues);
        }
    }

    private void batchLoad(Map<Object, TuplePromise> correlationValues, FixedArrayList batchParameters, FixedArrayList viewRootIds, Object defaultKey, CorrelatedSubqueryViewRootJpqlMacro macro, BatchCorrelationMode batchCorrelationMode) {
        batchParameters.clearRest();
        if (batchSize > 1 && batchCorrelationMode == BatchCorrelationMode.VALUES) {
            criteriaBuilder.setParameter(correlationParamName, batchParameters);
            query.setParameter(correlationParamName, batchParameters);
        } else {
            query.setParameter(correlationParamName, batchParameters.get(0));
            criteriaBuilder.setParameter(correlationParamName, batchParameters.get(0));
        }

        if (viewRootIds != null) {
            viewRootIds.clearRest();
            if (viewRootIds.size() == 1) {
                macro.setParameters(query, viewRootIds.get(0));
            } else {
                macro.setParameters(query, viewRootIds);
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
