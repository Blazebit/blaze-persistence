/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.ExpressionUtils;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.MutableViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.LateAdditionalObjectBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import javax.persistence.Parameter;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractCorrelatedSubselectTupleTransformer implements TupleTransformer {

    protected static final String[] EMPTY = new String[0];
    private static final Logger LOG = Logger.getLogger(AbstractCorrelatedSubselectTupleTransformer.class.getName());

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
    protected final int valueIndex;

    protected final String[] fetches;
    protected final String[] indexFetches;
    protected final Correlator correlator;
    protected final Correlator indexCorrelator;
    protected final int viewRootIndex;
    protected final int embeddingViewIndex;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;
    protected final Class<?> viewRootEntityClass;
    protected final ManagedViewTypeImplementor<?> embeddingViewType;
    protected final ManagedViewTypeImplementor<?> viewRootType;
    protected final String idAttributePath;
    protected final String correlationResultExpression;
    protected final String correlationAlias;
    protected final String correlationExternalAlias;
    protected final String indexExpression;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final EntityViewConfiguration entityViewConfiguration;

    protected final int startIndex;
    protected final String attributePath;
    protected final ContainerAccumulator<Object> containerAccumulator;
    protected final Limiter limiter;
    protected FullQueryBuilder<?, ?> criteriaBuilder;
    protected int viewIndex;
    protected Map<Object, Map<Object, Object>> collections;
    protected CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    protected MutableEmbeddingViewJpqlMacro embeddingViewJpqlMacro;

    public AbstractCorrelatedSubselectTupleTransformer(ExpressionFactory ef, Correlator correlator, ContainerAccumulator<?> containerAccumulator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRootType, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath,
                                                       Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                       String[] indexFetches, Expression index, Correlator indexCorrelator, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, EntityViewConfiguration entityViewConfiguration) {
        this.startIndex = tupleIndex;
        this.attributePath = attributePath;
        this.containerAccumulator = (ContainerAccumulator<Object>) containerAccumulator;
        this.evm = evm;
        this.correlationProviderFactory = correlationProviderFactory;
        this.entityViewConfiguration = entityViewConfiguration;
        this.correlationBasisType = correlationBasisType;
        this.viewRootAlias = viewRootAlias;
        this.viewRootIndex = viewRootIndex;
        String viewRootAliasPrefix = viewRootAlias + ".";
        this.viewRootType = viewRootType;
        this.viewRootIdExpression = viewRootAliasPrefix + getEntityIdName(entityViewConfiguration, viewRootType.getEntityClass());
        this.viewRootIdMapperCount = viewIdMapperCount(viewRootType);
        this.embeddingViewIndex = embeddingViewIndex;
        this.embeddingViewType = embeddingViewType;
        this.embeddingViewPath = embeddingViewPath;
        if (viewRootAlias.equals(embeddingViewPath)) {
            this.embeddingViewIdExpression = viewRootAliasPrefix + getEntityIdName(entityViewConfiguration, embeddingViewType.getEntityClass());
        } else {
            this.embeddingViewIdExpression = embeddingViewPath + "." + getEntityIdName(entityViewConfiguration, embeddingViewType.getEntityClass());
        }
        this.embeddingViewIdMapperCount = viewIdMapperCount(embeddingViewType);
        this.maximumViewMapperCount = Math.max(1, Math.max(viewRootIdMapperCount, embeddingViewIdMapperCount));
        this.correlationBasisExpression = correlationBasisExpression;
        this.correlationKeyExpression = correlationKeyExpression;
        this.valueIndex = correlator.getElementOffset();
        this.limiter = limiter;
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        if (limiter == null) {
            this.correlationExternalAlias = correlationAlias;
        } else {
            this.correlationExternalAlias = CorrelationProviderHelper.getDefaultExternalCorrelationAlias(attributePath);
        }
        if (ExpressionUtils.isEmptyOrThis(correlationResult)) {
            this.correlationResultExpression = correlationExternalAlias;
        } else {
            this.correlationResultExpression = PrefixingQueryGenerator.prefix(ef, correlationResult, correlationExternalAlias, viewRootType.getEntityViewRootTypes().keySet(), true);
        }
        this.indexExpression = index == null ? null : PrefixingQueryGenerator.prefix(ef, index, correlationResultExpression, viewRootType.getEntityViewRootTypes().keySet(), true);
        this.fetches = prefix(correlationAlias, fetches);
        this.indexFetches = prefix(indexExpression, indexFetches);
        this.correlator = correlator;
        this.indexCorrelator = indexCorrelator;
        this.correlationBasisEntity = correlationBasisEntity;
        this.viewRootEntityClass = viewRootType.getEntityClass();
        this.idAttributePath = getEntityIdName(entityViewConfiguration, viewRootEntityClass);
    }

    private void prepare() {
        JpaProvider jpaProvider = entityViewConfiguration.getCriteriaBuilder().getService(JpaProvider.class);
        FullQueryBuilder<?, ?> queryBuilder = entityViewConfiguration.getCriteriaBuilder();
        Map<String, Object> optionalParameters = entityViewConfiguration.getOptionalParameters();

        Class<?> correlationBasisEntityType = correlationBasisEntity;
        String viewRootExpression = viewRootAlias;

        EmbeddingViewJpqlMacro embeddingViewJpqlMacro = entityViewConfiguration.getEmbeddingViewJpqlMacro();
        ViewJpqlMacro viewJpqlMacro = entityViewConfiguration.getViewJpqlMacro();
        if (queryBuilder instanceof PaginatedCriteriaBuilder<?>) {
            criteriaBuilder = queryBuilder.copyCriteriaBuilder(Object[].class, false);
        } else {
            LimitBuilder<?> limitBuilder = (LimitBuilder<?>) queryBuilder;
            // To set the limit, we need the JPA provider to support this
            if (jpaProvider.supportsSubqueryInFunction() && (limitBuilder.getFirstResult() > 0 || limitBuilder.getMaxResults() < Integer.MAX_VALUE)) {
                // In case the outer query defines a limit/offset and this is not a paginated criteria builder
                // we must turn this query builder into a paginated criteria builder first
                try {
                    criteriaBuilder = queryBuilder.copyCriteriaBuilder(Object[].class, true)
                            .page(limitBuilder.getFirstResult(), limitBuilder.getMaxResults())
                            .copyCriteriaBuilder(Object[].class, false);
                } catch (IllegalStateException ex) {
                    LOG.log(Level.WARNING, "Could not create a paginated criteria builder for SUBSELECT fetching which might lead to bad performance", ex);
                    criteriaBuilder = queryBuilder.copyCriteriaBuilder(Object[].class, false);
                }
            } else {
                // Regular query without limit/offset
                criteriaBuilder = queryBuilder.copyCriteriaBuilder(Object[].class, false);
            }
        }
        int originalFirstResult = 0;
        int originalMaxResults = Integer.MAX_VALUE;
        // A copied query that is extended with further joins can't possibly use the limits provided by the outer query
        ((LimitBuilder<?>) criteriaBuilder).setFirstResult(originalFirstResult);
        ((LimitBuilder<?>) criteriaBuilder).setMaxResults(originalMaxResults);
        this.viewRootJpqlMacro = new CorrelatedSubqueryViewRootJpqlMacro(criteriaBuilder, optionalParameters, false, viewRootEntityClass, idAttributePath, viewRootExpression);
        criteriaBuilder.registerMacro("view", viewJpqlMacro);
        criteriaBuilder.registerMacro("view_root", viewRootJpqlMacro);
        criteriaBuilder.registerMacro("embedding_view", embeddingViewJpqlMacro);

        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(correlationResultExpression);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);

        String joinBase = embeddingViewPath;
        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(queryBuilder, optionalParameters, criteriaBuilder, correlationAlias, correlationExternalAlias, correlationResultExpression, correlationBasisType, correlationBasisEntityType, joinBase, attributePath, 1, limiter, true);
        CorrelationProvider provider = correlationProviderFactory.create(entityViewConfiguration.getCriteriaBuilder(), entityViewConfiguration.getOptionalParameters());

        provider.applyCorrelation(correlationBuilder, correlationBasisExpression);

        if (criteriaBuilder instanceof LimitBuilder<?>) {
            if (originalFirstResult != ((LimitBuilder<?>) criteriaBuilder).getFirstResult()
                    || originalMaxResults != ((LimitBuilder<?>) criteriaBuilder).getMaxResults()) {
                throw new IllegalArgumentException("Correlation provider '" + provider + "' wrongly uses setFirstResult() or setMaxResults() on the query builder which might lead to wrong results. Use SELECT fetching with batch size 1 or reformulate the correlation provider to use the limit/offset in a subquery!");
            }
        }
        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                criteriaBuilder.fetch(fetches[i]);
            }
        }
        if (indexFetches.length != 0) {
            for (int i = 0; i < indexFetches.length; i++) {
                criteriaBuilder.fetch(indexFetches[i]);
            }
        }

        // Before we can determine whether we use view roots or embedding views, we need to add all selects, otherwise macros might report false although they are used
        final String correlationRoot = correlationBuilder.getCorrelationRoot();
        final int tupleSuffix = maximumViewMapperCount + 1 + (indexCorrelator == null && indexExpression == null ? 0 : 1);
        ObjectBuilder<Object[]> objectBuilder = (ObjectBuilder<Object[]>) correlator.finish(criteriaBuilder, entityViewConfiguration, 0, tupleSuffix, correlationRoot, embeddingViewJpqlMacro, true);

        final boolean usesViewRoot = viewRootJpqlMacro.usesViewMacro();
        final boolean usesEmbeddingView = embeddingViewJpqlMacro.usesEmbeddingView();

        if (usesEmbeddingView && !(embeddingViewType instanceof ViewType<?>)) {
            throw new IllegalStateException("The use of EMBEDDING_VIEW in the correlation for '" + embeddingViewType.getJavaType().getName() + "." + attributePath.substring(attributePath.lastIndexOf('.') + 1) + "' is illegal because the embedding view type '" + embeddingViewType.getJavaType().getName() + "' does not declare a @IdMapping!");
        } else if (usesViewRoot && !(viewRootType instanceof ViewType<?>)) {
            throw new IllegalStateException("The use of VIEW_ROOT in the correlation for '" + embeddingViewType.getJavaType().getName() + "." + attributePath.substring(attributePath.lastIndexOf('.') + 1) + "' is illegal because the view root type '" + viewRootType.getJavaType().getName() + "' does not declare a @IdMapping!");
        }

        final int maximumSlotsFilled;
        final int elementKeyIndex;
        final int elementViewIndex;
        if (usesEmbeddingView) {
            maximumSlotsFilled = embeddingViewIdMapperCount == 0 ? 1 : embeddingViewIdMapperCount;
            elementKeyIndex = (maximumViewMapperCount - maximumSlotsFilled) + 2 + valueIndex;
            elementViewIndex = (maximumViewMapperCount - maximumSlotsFilled) + 1 + valueIndex;
            viewIndex = embeddingViewIndex;
        } else if (usesViewRoot) {
            maximumSlotsFilled = viewRootIdMapperCount == 0 ? 1 : viewRootIdMapperCount;
            elementKeyIndex = (maximumViewMapperCount - maximumSlotsFilled) + 2 + valueIndex;
            elementViewIndex = (maximumViewMapperCount - maximumSlotsFilled) + 1 + valueIndex;
            viewIndex = viewRootIndex;
        } else {
            maximumSlotsFilled = 0;
            elementKeyIndex = maximumViewMapperCount + 1 + valueIndex;
            elementViewIndex = 1 + valueIndex;
            viewIndex = -1;
        }

        for (int i = maximumSlotsFilled; i < maximumViewMapperCount; i++) {
            criteriaBuilder.select("NULL");
        }

        ExpressionFactory ef = criteriaBuilder.getService(ExpressionFactory.class);
        if (usesEmbeddingView) {
            EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableViewJpqlMacro(), new MutableEmbeddingViewJpqlMacro(), Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap(), entityViewConfiguration.getFetches(), attributePath);
            ObjectBuilder<Object[]> embeddingViewObjectBuilder = createViewAwareObjectBuilder(criteriaBuilder, embeddingViewType, configuration, embeddingViewIdExpression);
            if (embeddingViewObjectBuilder == null) {
                criteriaBuilder.select(embeddingViewIdExpression);
            } else {
                criteriaBuilder.selectNew(objectBuilder = new LateAdditionalObjectBuilder(objectBuilder, embeddingViewObjectBuilder, true));
            }
        } else if (usesViewRoot) {
            EntityViewConfiguration configuration = new EntityViewConfiguration(criteriaBuilder, ef, new MutableViewJpqlMacro(), new MutableEmbeddingViewJpqlMacro(), Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap(), entityViewConfiguration.getFetches(), attributePath);
            ObjectBuilder<Object[]> viewRootObjectBuilder = createViewAwareObjectBuilder(criteriaBuilder, viewRootType, configuration, viewRootIdExpression);
            if (viewRootObjectBuilder == null) {
                criteriaBuilder.select(viewRootIdExpression);
            } else {
                criteriaBuilder.selectNew(objectBuilder = new LateAdditionalObjectBuilder(objectBuilder, viewRootObjectBuilder, true));
            }
        }

        criteriaBuilder.select(correlationKeyExpression);
        if (indexCorrelator != null) {
            ObjectBuilder<?> indexBuilder = indexCorrelator.finish(criteriaBuilder, entityViewConfiguration, maximumViewMapperCount + 2, 0, indexExpression, embeddingViewJpqlMacro, true);
            if (indexBuilder != null) {
                criteriaBuilder.selectNew(new LateAdditionalObjectBuilder(objectBuilder, indexBuilder, false));
            }
        }

        populateParameters(entityViewConfiguration, criteriaBuilder);
        viewJpqlMacro.setViewPath(oldViewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);

        List<Object[]> resultList = (List<Object[]>) criteriaBuilder.getResultList();
        Map<Object, Map<Object, Object>> collections = new HashMap<>(resultList.size());
        for (int i = 0; i < resultList.size(); i++) {
            Object[] element = resultList.get(i);
            Map<Object, Object> viewRootResult = collections.get(element[elementViewIndex]);
            if (viewRootResult == null) {
                viewRootResult = new HashMap<>();
                collections.put(element[elementViewIndex], viewRootResult);
            }
            if (this.containerAccumulator == null) {
                viewRootResult.put(element[elementKeyIndex], element[valueIndex]);
            } else {
                Object result = viewRootResult.get(element[elementKeyIndex]);
                if (result == null) {
                    result = createDefaultResult();
                    viewRootResult.put(element[elementKeyIndex], result);
                }
                Object indexObject = null;
                if (indexCorrelator != null || indexExpression != null) {
                    indexObject = element[elementKeyIndex + 1];
                }
                this.containerAccumulator.add(result, indexObject, element[valueIndex], isRecording());
            }
        }
        this.collections = collections;
    }

    @Override
    public int getConsumeStartIndex() {
        return -1;
    }

    @Override
    public int getConsumeEndIndex() {
        return -1;
    }

    protected String getEntityIdName(EntityViewConfiguration entityViewConfiguration, Class<?> entityClass) {
        ManagedType<?> managedType = entityViewConfiguration.getCriteriaBuilder().getMetamodel().managedType(entityClass);
        if (JpaMetamodelUtils.isIdentifiable(managedType)) {
            return JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) managedType).getName();
        } else {
            return null;
        }
    }

    private static String[] prefix(String prefix, String[] fetches) {
        if (fetches == null || fetches.length == 0) {
            return fetches;
        }
        String[] newFetches = new String[fetches.length];
        for (int i = 0; i < fetches.length; i++) {
            newFetches[i] = (prefix  + "." + fetches[i]).intern();
        }

        return newFetches;
    }

    private static int viewIdMapperCount(ManagedViewType<?> viewRootType) {
        MethodAttribute<?, ?> idAttribute;
        if (viewRootType instanceof ViewType<?> && (idAttribute = ((ViewType<?>) viewRootType).getIdAttribute()).isSubview()) {
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

    protected void populateParameters(EntityViewConfiguration entityViewConfiguration, FullQueryBuilder<?, ?> queryBuilder) {
        FullQueryBuilder<?, ?> mainBuilder = entityViewConfiguration.getCriteriaBuilder();
        for (Parameter<?> paramEntry : mainBuilder.getParameters()) {
            if (queryBuilder.containsParameter(paramEntry.getName()) && !queryBuilder.isParameterSet(paramEntry.getName())) {
                queryBuilder.setParameter(paramEntry.getName(), mainBuilder.getParameterValue(paramEntry.getName()));
            }
        }
        for (Map.Entry<String, Object> paramEntry : entityViewConfiguration.getOptionalParameters().entrySet()) {
            if (queryBuilder.containsParameter(paramEntry.getKey()) && !queryBuilder.isParameterSet(paramEntry.getKey())) {
                queryBuilder.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }

    private ObjectBuilder<Object[]> createViewAwareObjectBuilder(FullQueryBuilder<?, ?> criteriaBuilder, ManagedViewType<?> viewType, EntityViewConfiguration configuration, String viewRoot) {
        MethodAttribute<?, ?> idAttribute;
        if (!(viewType instanceof ViewType<?>) || !(idAttribute = ((ViewType<?>) viewType).getIdAttribute()).isSubview()) {
            return null;
        }
        ManagedViewType<?> idViewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) idAttribute).getType();
        return (ObjectBuilder<Object[]>) evm.createObjectBuilder((ManagedViewTypeImplementor<?>) idViewType, null, viewRoot, "", criteriaBuilder, configuration, 1, 1, false);
    }

    protected boolean isRecording() {
        return false;
    }

    protected Object createDefaultResult() {
        return containerAccumulator == null ? null : containerAccumulator.createContainer(isRecording(), 0);
    }

    @Override
    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap) {
        if (collections == null) {
            prepare();
        }
        Object viewKey;
        if (viewIndex == -1) {
            viewKey = null;
        } else {
            viewKey = tuple[viewIndex];
        }
        Map<Object, Object> collectionsByKey = collections.get(viewKey);
        Object correlationValueKey = tuple[startIndex];
        if (correlationValueKey == null) {
            tuple[startIndex] = createDefaultResult();
        } else if (collectionsByKey == null) {
            collections.put(viewKey, collectionsByKey = new HashMap<>());
            collectionsByKey.put(correlationValueKey, tuple[startIndex] = createDefaultResult());
        } else {
            Object collection = collectionsByKey.get(correlationValueKey);
            if (collection == null) {
                collectionsByKey.put(correlationValueKey, tuple[startIndex] = createDefaultResult());
            } else {
                tuple[startIndex] = collection;
            }
        }

        return tuple;
    }

}
