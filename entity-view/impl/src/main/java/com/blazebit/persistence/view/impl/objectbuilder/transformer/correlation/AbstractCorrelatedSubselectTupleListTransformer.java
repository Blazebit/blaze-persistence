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
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
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

    protected final String viewRootAlias;
    protected final String correlationKeyExpression;

    protected FullQueryBuilder<?, ?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;

    public AbstractCorrelatedSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int tupleIndex, Class<?> correlationBasisType,
                                                           Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.viewRootAlias = viewRootAlias;
        this.correlationKeyExpression = correlationKeyExpression;
    }

    private String applyAndGetCorrelationRoot() {
        Class<?> viewRootEntityClass = viewRootType.getEntityClass();
        String idAttributePath = getEntityIdName(viewRootEntityClass);

        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType = correlationBasisEntity;
        String viewRootExpression = viewRootAlias;

        this.criteriaBuilder = queryBuilder.copy(Object[].class);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, viewRootEntityClass, idAttributePath, viewRootExpression);
        this.criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);

        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(criteriaBuilder, correlationAlias, correlationResult, correlationBasisType, correlationBasisEntityType, null, 1, true, attributePath);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        provider.applyCorrelation(correlationBuilder, correlationKeyExpression);
        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                criteriaBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }

        return correlationBuilder.getCorrelationRoot();
    }

    @Override
    public List<Object[]> transform(List<Object[]> tuples) {
        final String correlationRoot = applyAndGetCorrelationRoot();
        final boolean usesViewRoot = viewRootJpqlMacro.usesViewRoot();

        int totalSize = tuples.size();
        Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);

        if (usesViewRoot) {
            criteriaBuilder.select(viewRootAlias + "." + getEntityIdName(viewRootType.getEntityClass()));
            // Group tuples by view roots and correlation values and create tuple promises
            for (Object[] tuple : tuples) {
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
            }
        } else {
            Map<Object, TuplePromise> viewRootCorrelationValues = new HashMap<Object, TuplePromise>(tuples.size());
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

        // We have the view root id on the first and the correlation key on the second position
        correlator.finish(criteriaBuilder, entityViewConfiguration, usesViewRoot ? 2 : 1, correlationRoot);
        populateParameters(criteriaBuilder);

        List<Object[]> resultList = (List<Object[]>) criteriaBuilder.getResultList();
        populateResult(usesViewRoot, viewRoots, resultList);
        fillDefaultValues(viewRoots);

        return tuples;
    }

    protected abstract void populateResult(boolean usesViewRoot, Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list);

}
