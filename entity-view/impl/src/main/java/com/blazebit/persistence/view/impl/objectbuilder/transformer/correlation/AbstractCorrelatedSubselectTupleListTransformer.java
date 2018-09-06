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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.LateAdditionalObjectBuilder;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.Collections;
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

    protected final EntityViewManagerImpl evm;
    protected final String viewRootAlias;
    protected final String viewRootIdExpression;
    protected final int viewRootIdMapperCount;
    protected final String embeddingViewPath;
    protected final String embeddingViewIdExpression;
    protected final int embeddingViewIdMapperCount;
    protected final int maximumViewMapperCount;
    protected final String correlationBasisExpression;
    protected final String correlationKeyExpression;

    protected int keyIndex;
    protected FullQueryBuilder<?, ?> criteriaBuilder;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected MutableEmbeddingViewJpqlMacro embeddingViewJpqlMacro;

    public AbstractCorrelatedSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, EntityViewManagerImpl evm, ManagedViewType<?> viewRootType, String viewRootAlias, ManagedViewType<?> embeddingViewType, String embeddingViewPath, String correlationResult, String correlationBasisExpression, String correlationKeyExpression,
                                                           CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.evm = evm;
        this.viewRootAlias = viewRootAlias;
        this.viewRootIdExpression = viewRootAlias + "." + getEntityIdName(viewRootType.getEntityClass());
        this.viewRootIdMapperCount = viewIdMapperCount(viewRootType);
        this.embeddingViewPath = embeddingViewPath;
        this.embeddingViewIdExpression = viewRootAlias.equals(embeddingViewPath) ? viewRootAlias + "." + getEntityIdName(embeddingViewType.getEntityClass()) : viewRootAlias + "." + embeddingViewPath + "." + getEntityIdName(embeddingViewType.getEntityClass());
        this.embeddingViewIdMapperCount = viewIdMapperCount(embeddingViewType);
        this.maximumViewMapperCount = Math.max(1, Math.max(viewRootIdMapperCount, embeddingViewIdMapperCount));
        this.correlationBasisExpression = correlationBasisExpression;
        this.correlationKeyExpression = correlationKeyExpression;
    }

    private static int viewIdMapperCount(ManagedViewType<?> viewRootType) {
        MethodAttribute<?, ?> idAttribute = ((ViewType<?>) viewRootType).getIdAttribute();
        if (idAttribute.isSubview()) {
            return viewIdMapperCount(idAttribute);
        } else {
            return 0;
        }
    }

    private static int viewIdMapperCount(MethodAttribute<?, ?> attribute) {
        if (attribute.isSubview()) {
            ManagedViewType<?> viewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) attribute).getType();
            int count = 0;
            for (MethodAttribute<?, ?> methodAttribute : viewType.getAttributes()) {
                count += viewIdMapperCount(methodAttribute);
            }
            return count;
        } else {
            return 1;
        }
    }

    private ObjectBuilder<Object[]> createViewAwareObjectBuilder(ManagedViewType<?> viewType, EntityViewConfiguration configuration, String viewRoot) {
        MethodAttribute<?, ?> idAttribute = ((ViewType<?>) viewType).getIdAttribute();
        if (!idAttribute.isSubview()) {
            return null;
        }
        ManagedViewType<?> idViewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) idAttribute).getType();
        String viewName = idViewType.getJavaType().getSimpleName();
        return (ObjectBuilder<Object[]>) evm.createObjectBuilder((ManagedViewTypeImplementor<?>) idViewType, null, viewName, viewRoot, "", criteriaBuilder, configuration, 1, 1);
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

        provider.applyCorrelation(correlationBuilder, correlationBasisExpression);
        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                criteriaBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }

        // Before we can determine whether we use view roots or embedding views, we need to add all selects, otherwise macros might report false although they are used
        final String correlationRoot = correlationBuilder.getCorrelationRoot();
        ObjectBuilder<Object[]> objectBuilder = (ObjectBuilder<Object[]>) correlator.finish(criteriaBuilder, entityViewConfiguration, maximumViewMapperCount + 1, correlationRoot, embeddingViewJpqlMacro);

        final boolean usesViewRoot = viewRootJpqlMacro.usesViewMacro();
        final boolean usesEmbeddingView = embeddingViewJpqlMacro.usesEmbeddingView();

        int totalSize = tuples.size();
        Map<Object, Map<Object, TuplePromise>> viewRoots = new HashMap<Object, Map<Object, TuplePromise>>(totalSize);
        final int maximumSlotsFilled;

        if (usesEmbeddingView) {
            maximumSlotsFilled = embeddingViewIdMapperCount == 0 ? 1 : embeddingViewIdMapperCount;
            this.keyIndex = (maximumViewMapperCount - maximumSlotsFilled) + 2;
        } else if (usesViewRoot) {
            maximumSlotsFilled = viewRootIdMapperCount == 0 ? 1 : viewRootIdMapperCount;
            this.keyIndex = (maximumViewMapperCount - maximumSlotsFilled) + 2;
        } else {
            maximumSlotsFilled = 0;
            this.keyIndex = maximumViewMapperCount + 1;
        }

        for (int i = maximumSlotsFilled; i < maximumViewMapperCount; i++) {
            criteriaBuilder.select("NULL");
        }

        if (usesEmbeddingView) {
            ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
            EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableEmbeddingViewJpqlMacro(), Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap());
            ObjectBuilder<Object[]> embeddingViewObjectBuilder = createViewAwareObjectBuilder(embeddingViewType, configuration, embeddingViewIdExpression);
            if (embeddingViewObjectBuilder == null) {
                criteriaBuilder.select(embeddingViewIdExpression);
            } else {
                criteriaBuilder.selectNew(new LateAdditionalObjectBuilder(objectBuilder, embeddingViewObjectBuilder));
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
            ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
            EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableEmbeddingViewJpqlMacro(), Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap());
            ObjectBuilder<Object[]> viewRootObjectBuilder = createViewAwareObjectBuilder(viewRootType, configuration, viewRootIdExpression);
            if (viewRootObjectBuilder == null) {
                criteriaBuilder.select(viewRootIdExpression);
            } else {
                criteriaBuilder.selectNew(new LateAdditionalObjectBuilder(objectBuilder, viewRootObjectBuilder));
            }
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
