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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedSubselectTupleListTransformer extends AbstractCorrelatedTupleListTransformer {

    protected static final int VALUE_INDEX = 0;
    protected static final int VIEW_INDEX = 1;
    protected static final int KEY_INDEX = 2;

    protected final String viewRootAlias;
    protected final String embeddingViewPath;
    protected final String correlationKeyExpression;

    protected FullQueryBuilder<?, ?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected MutableEmbeddingViewJpqlMacro embeddingViewJpqlMacro;

    public AbstractCorrelatedSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, String viewRootAlias, ManagedViewType<?> embeddingViewType, String embeddingViewPath, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                           int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.viewRootAlias = viewRootAlias;
        this.embeddingViewPath = embeddingViewPath;
        this.correlationKeyExpression = correlationKeyExpression;
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        String idAttributePath = getEntityIdName(viewRootEntityClass);

        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType = correlationBasisEntity;
        String viewRootExpression = viewRootAlias;

        EmbeddingViewJpqlMacro embeddingViewJpqlMacro = entityViewConfiguration.getEmbeddingViewJpqlMacro();
        this.criteriaBuilder = queryBuilder.copy(Object[].class);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, false, viewRootEntityClass, idAttributePath, viewRootExpression);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);
        this.criteriaBuilder.registerMacro("embedding_view", embeddingViewJpqlMacro);

        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationAlias, correlationResult, correlationBasisType, correlationBasisEntityType, null, 1, true, attributePath);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        provider.applyCorrelation(correlationBuilder, correlationKeyExpression);
        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                criteriaBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }

        // Before we can determine whether we use view roots or embedding views, we need to add all selects, otherwise macros might report false although they are used
        final String correlationRoot = correlationBuilder.getCorrelationRoot();
        correlator.finish(criteriaBuilder, entityViewConfiguration, 2, correlationRoot, embeddingViewJpqlMacro);
        final boolean usesViewRoot = viewRootJpqlMacro.usesViewMacro();
        final boolean usesEmbeddingView = embeddingViewJpqlMacro.usesEmbeddingView();

        int totalSize = tuples.size();
        Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);

        if (usesEmbeddingView) {
            if (viewRootAlias.equals(embeddingViewPath)) {
                criteriaBuilder.select(viewRootAlias + "." + getEntityIdName(embeddingViewType.getEntityClass()));
            } else {
                criteriaBuilder.select(viewRootAlias + "." + embeddingViewPath + "." + getEntityIdName(embeddingViewType.getEntityClass()));
            }
            // Group tuples by view roots and correlation values and create tuple promises
            for (Object[] tuple : tuples) {
                Object embeddingViewKey = tuple[embeddingViewIndex];
                Object correlationValueKey = tuple[startIndex];

                Map<Object, TuplePromise> viewRootCorrelationValues = viewRoots.get(embeddingViewKey);
                if (viewRootCorrelationValues == null) {
                    viewRootCorrelationValues = new HashMap<>();
                    viewRoots.put(embeddingViewKey, viewRootCorrelationValues);
                }
                TuplePromise viewRootPromise = viewRootCorrelationValues.get(correlationValueKey);
                if (viewRootPromise == null) {
                    viewRootPromise = new TuplePromise(startIndex);
                    viewRootCorrelationValues.put(correlationValueKey, viewRootPromise);
                }
                viewRootPromise.add(tuple);
            }
        } else if (usesViewRoot) {
            criteriaBuilder.select(viewRootAlias + "." + getEntityIdName(viewRootType.getEntityClass()));
            // Group tuples by view roots and correlation values and create tuple promises
            for (Object[] tuple : tuples) {
                Object viewRootKey = tuple[viewRootIndex];
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
            }
        } else {
            criteriaBuilder.select("NULL");
            Map<Object, TuplePromise> viewRootCorrelationValues = new HashMap<>(tuples.size());
            viewRoots.put(null, viewRootCorrelationValues);
            // Group tuples by correlation values and create tuple promises
            for (Object[] tuple : tuples) {
                Object correlationValueKey = tuple[startIndex];

                TuplePromise viewRootPromise = viewRootCorrelationValues.get(correlationValueKey);
                if (viewRootPromise == null) {
                    viewRootPromise = new TuplePromise(startIndex);
                    viewRootCorrelationValues.put(correlationValueKey, viewRootPromise);
                }
                viewRootPromise.add(tuple);
            }
        }

        criteriaBuilder.select(correlationKeyExpression);

        populateParameters(criteriaBuilder);
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);

        List<Object[]> resultList = (List<Object[]>) criteriaBuilder.getResultList();
        populateResult(viewRoots, resultList);
        fillDefaultValues(viewRoots);

        return tuples;
    }

    protected abstract void populateResult(Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list);

}
