/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.SubqueryProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.ExpressionUtils;
import com.blazebit.persistence.view.impl.MacroConfigurationExpressionFactory;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractParameterAttribute;
import com.blazebit.persistence.view.impl.metamodel.ConstrainedAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ConstrainedTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.CorrelationMultisetTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.MultisetTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.SimpleSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapperBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleParameterMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TypeUtils;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.CollectionMultisetTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.CollectionTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.IndexedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.MapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SingularMultisetTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SubviewTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.BasicCorrelator;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSubviewJoinTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.SubviewCorrelator;
import com.blazebit.persistence.view.impl.proxy.AbstractReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.AssignmentConstructorReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.type.IntegerBasicUserType;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.OrderByItem;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewTypeObjectBuilderTemplate<T> {

    private static final String[] EMPTY = new String[0];
    private static final int FEATURE_PARAMETERS = 0;
    private static final int FEATURE_INDEXED_COLLECTIONS = 1;
    private static final int FEATURE_SUBVIEWS = 2;
    private static final int FEATURE_SUBQUERY_CORRELATION = 3;

    private static final String CASE_WHEN_PREFIX = "CASE WHEN ";
    private static final String CASE_WHEN_SUFFIX = " THEN true ELSE false END";

    private final ManagedViewTypeImplementor<?> viewType;
    private final ObjectInstantiator<T> objectInstantiator;
    private final ObjectInstantiator<T>[] subtypeInstantiators;
    private final TupleElementMapper[] mappers;
    private final TupleParameterMapper parameterMapper;
    private final int effectiveTupleSize;
    private final boolean hasId;
    private final boolean hasParameters;
    private final boolean hasIndexedCollections;
    private final boolean hasSubqueryCorrelation;
    private final boolean hasSubviews;
    private final boolean hasSubtypes;

    private final ManagedViewTypeImplementor<?> viewRoot;
    private final String viewRootAlias;
    private final Class<?> managedTypeClass;
    private final int[] idPositions;
    private final int tupleOffset;
    private final EntityViewManagerImpl evm;
    private final ProxyFactory proxyFactory;
    private final TupleTransformatorFactory tupleTransformatorFactory;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ViewTypeObjectBuilderTemplate(ManagedViewTypeImplementor<?> viewRoot, String viewRootAlias, String attributePath, String aliasPrefix, String mappingPrefix, String idPrefix, TupleIdDescriptor tupleIdDescriptor, TupleIdDescriptor viewIdDescriptor, int tupleOffset, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                                          Map<ManagedViewType<? extends T>, String> inheritanceSubtypeMappings, EntityViewManagerImpl evm, ExpressionFactory ef, ManagedViewTypeImplementor<T> managedViewType, MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory) {
        ViewType<T> viewType;
        if (managedViewType instanceof ViewType<?>) {
            viewType = (ViewType<T>) managedViewType;
            this.hasId = true;
        } else {
            viewType = null;
            this.hasId = false;
        }

        if (mappingConstructor == null) {
            mappingConstructor = managedViewType.getDefaultConstructor();
            if (managedViewType.getConstructors().size() > 1) {
                if (mappingConstructor == null) {
                    throw new IllegalArgumentException("The given view type '" + managedViewType.getJavaType().getName() + "' has multiple constructors and the given constructor is null, but the view type has no default 'init' constructor!");
                }
            }
        }

        this.viewType = managedViewType;
        this.viewRoot = viewRoot;
        this.viewRootAlias = viewRootAlias;
        this.managedTypeClass = managedViewType.getEntityClass();
        this.tupleOffset = tupleOffset;
        this.evm = evm;
        this.proxyFactory = proxyFactory;

        ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> inheritanceSubtypeConfiguration = managedViewType.getInheritanceSubtypeConfiguration(inheritanceSubtypeMappings);
        Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>>> attributeMap = new LinkedHashMap<>(inheritanceSubtypeConfiguration.getAttributesClosure());

        int attributeCount = attributeMap.size();

        // We have special handling for the id attribute since we need to know it's position in advance
        // Therefore we have to remove it so that it doesn't get processed as normal attribute
        if (viewType != null) {
            attributeMap.remove(new ManagedViewTypeImpl.AttributeKey(0, viewType.getIdAttribute().getName()));
        }

        MappingConstructorImpl.InheritanceSubtypeConstructorConfiguration<T> subtypeConstructorConfiguration;
        List<AbstractParameterAttribute<? super T, ?>> parameterAttributeList;

        if (mappingConstructor == null) {
            subtypeConstructorConfiguration = null;
            parameterAttributeList = Collections.emptyList();
        } else {
            subtypeConstructorConfiguration = mappingConstructor.getSubtypeConstructorConfiguration(inheritanceSubtypeMappings);
            parameterAttributeList = subtypeConstructorConfiguration.getParameterAttributesClosure();
        }

        attributeCount += parameterAttributeList.size();

        List<TupleElementMapper> mappingList = new ArrayList<>(attributeCount);
        List<String> parameterMappingList = new ArrayList<>(attributeCount);
        boolean[] featuresFound = new boolean[4];

        final TupleTransformatorFactory tupleTransformatorFactory = new TupleTransformatorFactory();
        final EntityMetamodel metamodel = evm.getMetamodel().getEntityMetamodel();
        TupleElementMapperBuilder mainMapperBuilder = new TupleElementMapperBuilder(0, null, null, aliasPrefix, mappingPrefix, idPrefix, null, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);

        int classMappingIndex = -1;

        // Add inheritance type extraction
        if (inheritanceSubtypeConfiguration.hasSubtypes()) {
            String mapping = inheritanceSubtypeConfiguration.getInheritanceDiscriminatorMapping();
            classMappingIndex = tupleOffset + mainMapperBuilder.mapperIndex();
            mainMapperBuilder.addMapper(createMapper(IntegerBasicUserType.INSTANCE, mainMapperBuilder.getMapping(mapping), mainMapperBuilder.getAlias("class"), attributePath, mappingPrefix, embeddingViewJpqlMacro.getEmbeddingViewPath(), EMPTY));
        }

        if (viewType != null) {
            MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) idAttribute;

            tupleIdDescriptor.addIdPosition(tupleOffset + mainMapperBuilder.mapperIndex());
            viewIdDescriptor.addIdPosition(tupleOffset + mainMapperBuilder.mapperIndex());

            // An id mapping can only be basic or a flat subview
            if (idAttribute.isSubview()) {
                ManagedViewTypeImpl<Object[]> subViewType = (ManagedViewTypeImpl<Object[]>) ((SingularAttribute<?, ?>) mappingAttribute).getType();
                featuresFound[FEATURE_SUBVIEWS] = true;
                applySubviewIdMapping(mappingAttribute, attributePath, tupleIdDescriptor, subViewType, mainMapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false);
            } else {
                applyBasicIdMapping(mappingAttribute, attributePath, mainMapperBuilder, embeddingViewJpqlMacro);
            }
        }

        // Add tuple element mappers for attributes
        for (Map.Entry<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>>> attributeEntry : attributeMap.entrySet()) {
            ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>> constrainedAttribute = attributeEntry.getValue();
            if (constrainedAttribute.requiresCaseWhen()) {
                // Collect all mappers for all constraints
                List<ConstrainedTupleElementMapper.ConstrainedTupleElementMapperBuilder> builders = new ArrayList<>(constrainedAttribute.getSelectionConstrainedAttributes().size());
                for (ConstrainedAttribute.Entry<AbstractMethodAttribute<? super T, ?>> entry : constrainedAttribute.getSelectionConstrainedAttributes()) {
                    AbstractMethodAttribute<? super T, ?> attribute = entry.getAttribute();
                    String constraint;
                    EntityType<?> treatType;
                    if (entry.getConstraint() == null) {
                        constraint = null;
                        treatType = null;
                    } else {
                        String mapping = mainMapperBuilder.getMapping(CASE_WHEN_PREFIX + entry.getConstraint() + CASE_WHEN_SUFFIX);
                        constraint = mapping.substring(CASE_WHEN_PREFIX.length(), mapping.length() - CASE_WHEN_SUFFIX.length());
                        treatType = getTreatType(metamodel, managedViewType, attribute.getDeclaringType());
                    }
                    TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(mappingList.size(), constraint, entry.getSubtypeIndex(), aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef);
                    applyMapping(attribute, attributePath, mapperBuilder, featuresFound, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
                    builders.add(new ConstrainedTupleElementMapper.ConstrainedTupleElementMapperBuilder(constraint, entry.getSubtypeIndexes(), mapperBuilder));
                }
                ConstrainedTupleElementMapper.addMappers(classMappingIndex, mappingList, parameterMappingList, tupleTransformatorFactory, builders);
            } else {
                AbstractMethodAttribute<? super T, ?> attribute = constrainedAttribute.getSubAttribute(managedViewType);

                EntityType<?> treatType = getTreatType(metamodel, managedViewType, attribute.getDeclaringType());
                TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, null, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);
                applyMapping(attribute, attributePath, mapperBuilder, featuresFound, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
            }
        }

        int subtypeIndex = -1;
        MappingConstructor<?> lastConstructor;
        if (inheritanceSubtypeConfiguration.hasSubtypes()) {
            lastConstructor = null;
        } else {
            lastConstructor = mappingConstructor;
        }


        if (!parameterAttributeList.isEmpty()) {
            // Add tuple element mappers for constructor parameters
            for (ParameterAttribute<? super T, ?> parameterAttribute : parameterAttributeList) {
                String paramAliasPrefix;
                if (lastConstructor == parameterAttribute.getDeclaringConstructor()) {
                    paramAliasPrefix = aliasPrefix;
                } else {
                    lastConstructor = parameterAttribute.getDeclaringConstructor();
                    paramAliasPrefix = aliasPrefix + "_" + (++subtypeIndex) + "_" + lastConstructor.getDeclaringType().getJavaType().getSimpleName();
                }
                EntityType<?> treatType = getTreatType(metamodel, managedViewType, parameterAttribute.getDeclaringType());
                TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, null, paramAliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);
                applyMapping((AbstractAttribute<?, ?>) parameterAttribute, attributePath, mapperBuilder, featuresFound, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
            }
        }

        ManagedViewTypeImplementor<T> viewTypeBase = null;
        if (this.hasSubtypes = inheritanceSubtypeConfiguration.hasSubtypes()) {
            viewTypeBase = managedViewType;
        }
        // This can only happen for subview mappings
        if (!inheritanceSubtypeConfiguration.getInheritanceSubtypes().contains(managedViewType.getRealType())) {
            this.objectInstantiator = null;
        } else {
            Class<?>[] constructorParameterTypes;
            if (mappingConstructor == null) {
                constructorParameterTypes = inheritanceSubtypeConfiguration.getParameterTypes().toArray(new Class[inheritanceSubtypeConfiguration.getParameterTypes().size()]);
            } else {
                List<Class<?>> parameterTypes = new ArrayList<>(inheritanceSubtypeConfiguration.getParameterTypes().size() +  mappingConstructor.getParameterAttributes().size());
                parameterTypes.addAll(inheritanceSubtypeConfiguration.getParameterTypes());
                for (ParameterAttribute<? super T, ?> parameterAttribute :  mappingConstructor.getParameterAttributes()) {
                    parameterTypes.add(parameterAttribute.getConvertedJavaType());
                }
                constructorParameterTypes = parameterTypes.toArray(new Class[parameterTypes.size()]);
            }
            this.objectInstantiator = createInstantiator(managedViewType, viewTypeBase, mappingConstructor, constructorParameterTypes, evm, inheritanceSubtypeConfiguration, subtypeConstructorConfiguration);
        }

        List<ObjectInstantiator<T>> subtypeInstantiators = new ArrayList<>(inheritanceSubtypeConfiguration.getInheritanceSubtypes().size());

        for (ManagedViewType<?> subtype : inheritanceSubtypeConfiguration.getInheritanceSubtypes()) {
            if (subtype == managedViewType) {
                subtypeInstantiators.add(0, objectInstantiator);
            } else {
                Class<?>[] subtypeConstructorParameterTypes;
                if (mappingConstructor == null) {
                    subtypeConstructorParameterTypes = inheritanceSubtypeConfiguration.getParameterTypes().toArray(new Class[inheritanceSubtypeConfiguration.getParameterTypes().size()]);
                } else {
                    List<Class<?>> subtypeParameterTypes = new ArrayList<>(inheritanceSubtypeConfiguration.getParameterTypes().size() + subtypeConstructorConfiguration.getParameterAttributesClosure().size());
                    subtypeParameterTypes.addAll(inheritanceSubtypeConfiguration.getParameterTypes());
                    for (AbstractParameterAttribute<? super T, ?> parameterAttribute : subtypeConstructorConfiguration.getParameterAttributesClosure()) {
                        subtypeParameterTypes.add(parameterAttribute.getConvertedJavaType());
                    }
                    subtypeConstructorParameterTypes = subtypeParameterTypes.toArray(new Class[subtypeParameterTypes.size()]);
                }
                ObjectInstantiator<T> instantiator = createInstantiator((ManagedViewType<? extends T>) subtype, managedViewType, mappingConstructor, subtypeConstructorParameterTypes, evm, inheritanceSubtypeConfiguration, subtypeConstructorConfiguration);
                subtypeInstantiators.add(instantiator);
            }
        }

        if (viewType == null) {
            int length = mainMapperBuilder.mapperIndex();
            for (int i = 0; i < length; i++) {
                tupleIdDescriptor.addIdPosition(tupleOffset + i);
                viewIdDescriptor.addIdPosition(tupleOffset + i);
            }
        }

        this.idPositions = viewIdDescriptor.createIdPositions();
        this.hasParameters = featuresFound[FEATURE_PARAMETERS];
        this.hasIndexedCollections = featuresFound[FEATURE_INDEXED_COLLECTIONS];
        this.hasSubviews = featuresFound[FEATURE_SUBVIEWS];
        this.hasSubqueryCorrelation = featuresFound[FEATURE_SUBQUERY_CORRELATION];
        this.subtypeInstantiators = subtypeInstantiators.toArray(new ObjectInstantiator[subtypeInstantiators.size()]);
        this.effectiveTupleSize = attributeCount;
        this.mappers = mappingList.toArray(new TupleElementMapper[mappingList.size()]);
        this.parameterMapper = new TupleParameterMapper(parameterMappingList, tupleOffset);
        this.tupleTransformatorFactory = tupleTransformatorFactory;
    }

    private EntityType<?> getTreatType(EntityMetamodel metamodel, ManagedViewTypeImplementor<T> managedViewType, ManagedViewType<? super T> subtype) {
        if (managedViewType == subtype) {
            return null;
        }
        return getTreatType(metamodel, managedViewType.getEntityClass(), subtype.getEntityClass());
    }

    private EntityType<?> getTreatType(EntityMetamodel metamodel, Class<?> baseType, Class<?> subtype) {
        if (baseType == subtype) {
            return null;
        }

        return metamodel.entity(subtype);
    }

    @SuppressWarnings("unchecked")
    private ObjectInstantiator<T> createInstantiator(ManagedViewType<? extends T> managedViewType, ManagedViewTypeImplementor<T> viewTypeBase, MappingConstructorImpl<? extends T> mappingConstructor, Class<?>[] constructorParameterTypes,
                                                     EntityViewManagerImpl entityViewManager, ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> configuration, MappingConstructorImpl.InheritanceSubtypeConstructorConfiguration<T> constructorConfiguration) {
        if (viewTypeBase == null) {
            return AbstractReflectionInstantiator.createInstantiator((MappingConstructorImpl<T>) mappingConstructor, proxyFactory, (ManagedViewTypeImplementor<T>) managedViewType, constructorParameterTypes, entityViewManager, configuration.getMutableBasicUserTypes(), configuration.getTypeConverterEntries());
        } else {
            return new AssignmentConstructorReflectionInstantiator<>((MappingConstructorImpl<T>) mappingConstructor, proxyFactory, (ManagedViewTypeImplementor<T>) managedViewType, constructorParameterTypes, entityViewManager, configuration, constructorConfiguration);
        }
    }

    private TupleElementMapper createMapper(Type<?> type, String expression, String alias, String attributePath, String viewPath, String embeddingViewPath, String[] originalFetches) {
        return createMapper(TypeUtils.forType(type), expression, alias, attributePath, viewPath, embeddingViewPath, originalFetches);
    }

    private TupleElementMapper createMapper(BasicUserTypeStringSupport<?> basicTypeStringSupport, String expression, String alias, String attributePath, String viewPath, String embeddingViewPath, String[] originalFetches) {
        String[] fetches;
        if (originalFetches.length != 0) {
            fetches = new String[originalFetches.length];
            for (int i = 0; i < originalFetches.length; i++) {
                fetches[i] = expression + "." + originalFetches[i];
            }
        } else {
            fetches = originalFetches;
        }
        if (alias != null) {
            return new AliasExpressionTupleElementMapper((BasicUserTypeStringSupport<Object>) basicTypeStringSupport, expression, alias, attributePath, viewPath, embeddingViewPath, fetches);
        } else {
            return new ExpressionTupleElementMapper((BasicUserTypeStringSupport<Object>) basicTypeStringSupport, expression, attributePath, viewPath, embeddingViewPath, fetches);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyMapping(AbstractAttribute<?, ?> attribute, String parentAttributePath, TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound, TupleIdDescriptor tupleIdDescriptor, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, ExpressionFactory ef) {
        String attributePath = getAttributePath(parentAttributePath, attribute, false);
        int batchSize = attribute.getBatchSize();

        if (batchSize == -1) {
            batchSize = attribute.getDeclaringType().getDefaultBatchSize();
        }

        if (attribute.isSubquery()) {
            applySubqueryMapping((SubqueryAttribute<? super T, ?>) attribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
        } else {
            if (attribute.isCollection()) {
                PluralAttribute<? super T, ?, ?> pluralAttribute = (PluralAttribute<? super T, ?, ?>) attribute;
                TypeConverter<Object, Object> keyConverter = null;
                TypeConverter<Object, Object> valueConverter = (TypeConverter<Object, Object>) pluralAttribute.getElementType().getConverter();
                boolean listKey = pluralAttribute.isIndexed() && pluralAttribute instanceof ListAttribute<?, ?>;
                boolean mapKey = pluralAttribute.isIndexed() && pluralAttribute instanceof MapAttribute<?, ?, ?>;
                int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                int mapValueStartIndex = startIndex + 1;

                if (listKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    if (pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        throw new IllegalArgumentException("When using a non-join fetch strategy, mappings can't be indexed!");
                    }
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping(IntegerBasicUserType.INSTANCE, "INDEX", "_KEY", mappingAttribute, attributePath, mapperBuilder, EMPTY, embeddingViewJpqlMacro);
                } else if (mapKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    if (pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        throw new IllegalArgumentException("When using a non-join fetch strategy, Map mappings are disallowed currently! Consider using a Set, List or Collection to map only the value!");

                    }
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) pluralAttribute;
                    keyConverter = (TypeConverter<Object, Object>) mapAttribute.getKeyType().getConverter();
                    if (mapAttribute.isKeySubview()) {
                        featuresFound[FEATURE_SUBVIEWS] = true;
                        ManagedViewTypeImpl<Object[]> managedViewType = (ManagedViewTypeImpl<Object[]>) mapAttribute.getKeyType();
                        applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, true, true);
                        mapValueStartIndex = tupleOffset + mapperBuilder.mapperIndex();
                    } else {
                        applyCollectionFunctionMapping(TypeUtils.forType(mapAttribute.getKeyType()), "KEY", "_KEY", mappingAttribute, attributePath, mapperBuilder, EMPTY, embeddingViewJpqlMacro);
                    }
                }

                boolean dirtyTracking = pluralAttribute instanceof MethodAttribute<?, ?> && attribute.needsDirtyTracker();
                if (pluralAttribute.isSubview()) {
                    featuresFound[FEATURE_SUBVIEWS] = true;

                    TupleIdDescriptor newTupleIdDescriptor;

                    if (listKey || mapKey) {
                        newTupleIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
                        newTupleIdDescriptor.addIdPosition(startIndex);
                    } else {
                        newTupleIdDescriptor = tupleIdDescriptor;
                    }

                    if (pluralAttribute.isCorrelated() || (pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN || attribute.getLimitExpression() != null) && pluralAttribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                        ManagedViewTypeImplementor<Object> managedViewType = (ManagedViewTypeImplementor<Object>) pluralAttribute.getElementType();
                        if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                            boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                            boolean nullIfEmpty = managedViewType instanceof ViewType<?>;
                            ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, featuresFound, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false);
                            mapperBuilder.setTupleListTransformerFactory(new CollectionMultisetTupleListTransformerFactory(startIndex, null, attributePath, getMultisetResultAlias(attributePath), valueConverter, attribute.getCollectionInstantiator(), dirtyTracking,
                                    subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(subviewTemplate, updatableObjectCache, nullIfEmpty)));
                        } else {
                            applyCorrelatedSubviewMapping(attribute, attributePath, newTupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, featuresFound, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, dirtyTracking);
                        }
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) pluralAttribute.getElementType();
                        boolean nullIfEmpty = managedViewType instanceof ViewType<?> || !listKey && !mapKey;
                        if (pluralAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                            boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                            String mapping = mapperBuilder.getMapping(mappingAttribute);
                            ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applySubviewMapping(mappingAttribute, attributePath, newTupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                            mapperBuilder.setTupleListTransformerFactory(new CollectionMultisetTupleListTransformerFactory(startIndex, mapping, attributePath, getMultisetResultAlias(attributePath), valueConverter, attribute.getCollectionInstantiator(), dirtyTracking,
                                    subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(subviewTemplate, updatableObjectCache, nullIfEmpty)));
                        } else {
                            // Obviously, we produce null if the object type is identifiable i.e. a ViewType and it is empty = null id
                            // Additionally, we also consider empty embeddables as null when we have a non-indexed collection so we can filter out these elements
                            applySubviewMapping(mappingAttribute, attributePath, newTupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                        }
                    }
                } else if (mapKey) {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyCollectionFunctionMapping(TypeUtils.forType(getType(mappingAttribute)), "VALUE", "", mappingAttribute, attributePath, mapperBuilder, mappingAttribute.getFetches(), embeddingViewJpqlMacro);
                } else {
                    if (pluralAttribute.isCorrelated() || pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN && pluralAttribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                        applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, featuresFound, ef, batchSize, dirtyTracking, embeddingViewJpqlMacro);
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        applyBasicMapping(mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                    }
                }

                if (listKey) {
                    if (pluralAttribute.isSorted()) {
                        throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                    } else {
                        mapperBuilder.setTupleListTransformer(new IndexedListTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, attribute.getCollectionInstantiator(), dirtyTracking, valueConverter));
                    }
                } else if (mapKey) {
                    mapperBuilder.setTupleListTransformer(new MapTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, mapValueStartIndex, attribute.getMapInstantiator(), dirtyTracking, keyConverter, valueConverter));
                } else if (pluralAttribute.getFetchStrategy() == FetchStrategy.JOIN) {
                    switch (pluralAttribute.getCollectionType()) {
                        case COLLECTION:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                            }
                            break;
                        case LIST:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                            }
                            break;
                        case SET:
                            break;
                        case MAP:
                            throw new IllegalArgumentException("Ignoring the index on the attribute '" + pluralAttribute + "' is not possible!");
                        default:
                            throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                    }
                    mapperBuilder.setTupleListTransformer(new CollectionTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, attribute.getCollectionInstantiator(), dirtyTracking, valueConverter));
                }
            } else if (attribute.isQueryParameter()) {
                MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                featuresFound[FEATURE_PARAMETERS] = true;
                applyQueryParameterMapping(mappingAttribute, mapperBuilder);
            } else if (attribute.isSubview()) {
                featuresFound[FEATURE_SUBVIEWS] = true;
                boolean nullIfEmpty = !((SingularAttribute<?, ?>) attribute).isCreateEmptyFlatView();
                if (attribute.isCorrelated() || attribute.getFetchStrategy() != FetchStrategy.JOIN && attribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                    ManagedViewTypeImplementor<Object> managedViewType = (ManagedViewTypeImplementor<Object>) ((SingularAttribute<?, ?>) attribute).getType();
                    if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                        int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                        boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                        ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, featuresFound, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false);
                        TypeConverter<Object, Object> elementConverter = (TypeConverter<Object, Object>) (TypeConverter<?, ?>) managedViewType.getConverter();
                        mapperBuilder.setTupleListTransformerFactory(new SingularMultisetTupleListTransformerFactory(startIndex, null, attributePath, getMultisetResultAlias(attributePath), elementConverter, subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(subviewTemplate, updatableObjectCache, nullIfEmpty)));
                    } else {
                        applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, featuresFound, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false);
                    }
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) ((SingularAttribute<?, ?>) attribute).getType();
                    if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                        int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                        boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                        String mapping = mapperBuilder.getMapping(mappingAttribute);
                        ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                        TypeConverter<Object, Object> elementConverter = (TypeConverter<Object, Object>) (TypeConverter<?, ?>) managedViewType.getConverter();
                        mapperBuilder.setTupleListTransformerFactory(new SingularMultisetTupleListTransformerFactory(startIndex, mapping, attributePath, getMultisetResultAlias(attributePath), elementConverter, subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(subviewTemplate, updatableObjectCache, nullIfEmpty)));
                    } else {
                        applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                    }
                }
            } else {
                if (attribute.isCorrelated() || attribute.getFetchStrategy() != FetchStrategy.JOIN && attribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                    applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, featuresFound, ef, batchSize, false, embeddingViewJpqlMacro);
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyBasicMapping(mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                }
            }
        }
    }

    private void applyCollectionFunctionMapping(BasicUserTypeStringSupport<?> basicUserTypeStringSupport, String function, String aliasSuffix, MappingAttribute<? super T, ?> mappingAttribute, String attributePath, TupleElementMapperBuilder mapperBuilder, String[] fetches, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String expression = function + "(" + mapperBuilder.getMapping(mappingAttribute) + ")";
        String alias = mapperBuilder.getAlias(mappingAttribute, false);
        TupleElementMapper mapper;
        if (alias == null) {
            mapper = createMapper(basicUserTypeStringSupport, expression, null, attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), fetches);
        } else {
            mapper = createMapper(basicUserTypeStringSupport, expression, alias + aliasSuffix, attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), fetches);
        }
        mapperBuilder.addMapper(mapper);
    }

    private void applySubviewIdMapping(MappingAttribute<? super T, ?> mappingAttribute, String parentAttributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, ExpressionFactory ef, boolean isKey) {
        String attributePath = getAttributePath(parentAttributePath, mappingAttribute, false);
        applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, isKey, true);
    }

    private String getMultisetResultAlias(String attributePath) {
        return "multiset_" + attributePath.replace('.', '_');
    }

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate<Object[]> applySubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String subviewAttributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                                                                        ExpressionFactory ef, boolean isKey, boolean nullIfEmpty) {
        String subviewAliasPrefix = mapperBuilder.getAlias(mappingAttribute, isKey);
        String subviewMappingPrefix;
        String subviewIdPrefix;
        int startIndex;
        if (mappingAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            startIndex = 0;
        } else {
            startIndex = tupleOffset + mapperBuilder.mapperIndex();
        }
        boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
        TupleIdDescriptor subviewTupleIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
        TupleIdDescriptor subviewIdDescriptor;

        String multisetCorrelationExpression;
        if (mappingAttribute.getFetchStrategy() != FetchStrategy.MULTISET) {
            subviewMappingPrefix = mapperBuilder.getMapping(mappingAttribute, isKey);
            subviewIdPrefix = mapperBuilder.getMapping(mappingAttribute, isKey);
            multisetCorrelationExpression = null;
        } else {
            // Must be in sync with com.blazebit.persistence.view.impl.objectbuilder.mapper.MultisetTupleElementMapper.applyMapping
            subviewMappingPrefix = getMultisetResultAlias(subviewAttributePath);
            subviewIdPrefix = subviewMappingPrefix;
            multisetCorrelationExpression = mapperBuilder.getMapping(mappingAttribute);
        }
        String oldViewPath = viewJpqlMacro.getViewPath();
        viewJpqlMacro.setViewPath(subviewMappingPrefix);

        if (managedViewType instanceof ViewType<?>) {
            // When the attribute is update mappable i.e. a subset mapping, we already have the proper parent id set
            // When the attribute is not update mappable i.e. joining over other associations, we use its parent's parent id
            if (((AbstractAttribute<?, ?>) mappingAttribute).isUpdateMappable()) {
                subviewIdDescriptor = new TupleIdDescriptor();
            } else {
                subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
            }
        } else {
            subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
            subviewIdDescriptor.addIdPosition(flatViewIdPosition(mappingAttribute));
            subviewTupleIdDescriptor.addIdPosition(flatViewIdPosition(mappingAttribute));
        }

        Map<ManagedViewType<? extends Object[]>, String> inheritanceSubtypeMappings;

        if (isKey) {
            inheritanceSubtypeMappings = (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) ((MapAttribute<?, ?, ?>) mappingAttribute).getKeyInheritanceSubtypeMappings();
        } else if (mappingAttribute instanceof PluralAttribute<?, ?, ?>) {
            inheritanceSubtypeMappings = (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) ((PluralAttribute<?, ?, ?>) mappingAttribute).getElementInheritanceSubtypeMappings();
        } else {
            inheritanceSubtypeMappings = (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) ((SingularAttribute<?, ?>) mappingAttribute).getInheritanceSubtypeMappings();
        }

        String embeddingViewPath = mapperBuilder.getMapping();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewTupleIdDescriptor, subviewIdDescriptor,
                startIndex, viewJpqlMacro, embeddingViewJpqlMacro, inheritanceSubtypeMappings, evm, ef, managedViewType, null, proxyFactory);
        if (mappingAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            String multisetResultAlias = getMultisetResultAlias(subviewAttributePath);
            mapperBuilder.addMapper(new MultisetTupleElementMapper(template, multisetCorrelationExpression, subviewAttributePath, multisetResultAlias, embeddingViewPath, createLimiter(multisetResultAlias, mappingAttribute)));
        } else {
            mapperBuilder.addMappers(template.mappers);
            mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
            mapperBuilder.addTupleTransformerFactory(new SubviewTupleTransformerFactory(template, updatableObjectCache, nullIfEmpty));
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
        return template;
    }

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate<Object[]> applyCorrelatedSubviewMapping(AbstractAttribute<?, ?> attribute, String attributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                                                                                  ExpressionFactory ef, int batchSize, boolean dirtyTracking) {
        Expression correlationResult = attribute.getCorrelationResultExpression();
        String correlationBasis = attribute.getCorrelationBasis();
        CorrelationProviderFactory factory = attribute.getCorrelationProviderFactory();
        String correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);

        if (attribute.getFetchStrategy() == FetchStrategy.JOIN || attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            @SuppressWarnings("unchecked")
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            correlationBasis = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            Limiter limiter = createLimiter(correlationAlias, attribute);
            String correlationExternalAlias;
            if (limiter == null) {
                correlationExternalAlias = correlationAlias;
            } else {
                correlationExternalAlias = CorrelationProviderHelper.getDefaultExternalCorrelationAlias(attributePath);
            }
            String subviewIdPrefix = correlationExternalAlias;
            if (!ExpressionUtils.isEmptyOrThis(correlationResult)) {
                subviewIdPrefix = PrefixingQueryGenerator.prefix(ef, correlationResult, correlationExternalAlias, true);
            }
            String subviewMappingPrefix = subviewIdPrefix;

            int startIndex;
            if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                startIndex = 0;
            } else {
                startIndex = tupleOffset + mapperBuilder.mapperIndex();
            }
            TupleIdDescriptor subviewTupleIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
            TupleIdDescriptor subviewIdDescriptor;

            if (managedViewType instanceof ViewType<?>) {
                // When the attribute is update mappable i.e. a subset mapping, we already have the proper parent id set
                // When the attribute is not update mappable i.e. joining over other associations, we use its parent's parent id
                if (attribute.isUpdateMappable()) {
                    subviewIdDescriptor = new TupleIdDescriptor();
                } else {
                    subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
                }
            } else {
                subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
                subviewIdDescriptor.addIdPosition(flatViewIdPosition(attribute));
                subviewTupleIdDescriptor.addIdPosition(flatViewIdPosition(attribute));
            }

            Map<ManagedViewType<? extends Object[]>, String> inheritanceSubtypeMappings;

            if (attribute instanceof PluralAttribute<?, ?, ?>) {
                inheritanceSubtypeMappings = (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) ((PluralAttribute<?, ?, ?>) attribute).getElementInheritanceSubtypeMappings();
            } else {
                inheritanceSubtypeMappings = (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) ((SingularAttribute<?, ?>) attribute).getInheritanceSubtypeMappings();
            }

            String embeddingViewPath = mapperBuilder.getMapping();
            String oldViewPath = viewJpqlMacro.getViewPath();
            String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
            viewJpqlMacro.setViewPath(subviewMappingPrefix);
            embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
            @SuppressWarnings("unchecked")
            ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, attributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewTupleIdDescriptor, subviewIdDescriptor,
                    startIndex, viewJpqlMacro, embeddingViewJpqlMacro, inheritanceSubtypeMappings, evm, ef, managedViewType, null, proxyFactory);
            if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                mapperBuilder.addMapper(new CorrelationMultisetTupleElementMapper(template, factory, correlationBasis, correlationExternalAlias, attributePath, mapperBuilder.getMapping(), limiter));
            } else {
                mapperBuilder.addMappers(template.mappers);
                mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
                mapperBuilder.addTupleTransformerFactory(new CorrelatedSubviewJoinTupleTransformerFactory(template, factory, correlationAlias, mapperBuilder.getMapping(), correlationBasis, correlationExternalAlias, attributePath, embeddingViewPath, attribute.getFetches(), limiter));
            }
            embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
            viewJpqlMacro.setViewPath(oldViewPath);
            return template;
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = (viewType.hasSubtypes() ? 1 : 0) + tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), ef);
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = AbstractAttribute.stripThisFromMapping(correlationBasis);
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            String embeddingViewPath = mapperBuilder.getMapping();
            boolean correlatesThis = correlatesThis(evm, ef, managedTypeClass, attribute.getCorrelated(), correlationBasisExpression, attribute.getCorrelationPredicate(), attribute.getCorrelationKeyAlias());
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            // We need a special mapping for the VIEW_ROOT/EMBEDDING_VIEW macro in certain cases
            viewRootIndex = addViewRootMappingIfNeeded(mapperBuilder, featuresFound, subviewAliasPrefix, attributePath, viewRootIndex);
            embeddingViewIndex = addEmbeddingViewMappingIfNeeded(mapperBuilder, featuresFound, subviewAliasPrefix, attributePath, embeddingViewIndex);

            if (batchSize == -1) {
                batchSize = 1;
            }

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case SET:
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute),
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute)));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = (viewType.hasSubtypes() ? 1 : 0) + tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), ef);
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            String embeddingViewPath = mapperBuilder.getMapping();
            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case SET:
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }

                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute),
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute)));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }

        return null;
    }

    private Limiter createLimiter(String prefix, Attribute<?, ?> attribute) {
        if (attribute.getLimitExpression() != null) {
            List<OrderByItem> orderByItems = attribute.getOrderByItems();
            List<OrderByItem> items;
            if (orderByItems.isEmpty()) {
                items = Collections.emptyList();
            } else {
                ExpressionFactory expressionFactory = evm.getService(ExpressionFactory.class);
                PrefixingQueryGenerator prefixingQueryGenerator = new PrefixingQueryGenerator(expressionFactory, prefix, null, null, PrefixingQueryGenerator.DEFAULT_QUERY_ALIASES, true, false);
                StringBuilder sb = new StringBuilder();
                prefixingQueryGenerator.setQueryBuffer(sb);
                items = new ArrayList<>(attribute.getOrderByItems().size());
                for (int i = 0; i < orderByItems.size(); i++) {
                    OrderByItem orderByItem = orderByItems.get(i);
                    Expression expr = expressionFactory.createSimpleExpression(orderByItem.getExpression(), false, false, true);
                    sb.setLength(0);
                    expr.accept(prefixingQueryGenerator);
                    items.add(new OrderByItem(sb.toString(), orderByItem.isAscending(), orderByItem.isNullsFirst()));
                }
            }
            return new Limiter(attribute.getLimitExpression(), attribute.getOffsetExpression(), items);
        }
        return null;
    }

    private int addViewRootMappingIfNeeded(TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound, String subviewAliasPrefix, String attributePath, int viewRootIndex) {
        if (viewRoot.getJpaManagedType() instanceof EntityType<?>) {
            boolean viewRootMapping = false;
            if (viewRoot instanceof ViewType<?>) {
                MethodAttribute idAttribute = ((ViewType) viewRoot).getIdAttribute();
                if (idAttribute.isSubview() || !((AbstractMethodAttribute) idAttribute).isUpdateMappable()) {
                    viewRootMapping = true;
                }
            } else {
                viewRootMapping = true;
            }
            if (viewRootMapping) {
                featuresFound[FEATURE_SUBQUERY_CORRELATION] = true;
                viewRootIndex = tupleOffset + mapperBuilder.mapperIndex();
                javax.persistence.metamodel.SingularAttribute<?, ?> singleIdAttribute = JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) viewRoot.getJpaManagedType());
                mapperBuilder.addMapper(createMapper((BasicUserTypeStringSupport<?>) null, singleIdAttribute.getName(), subviewAliasPrefix + "_view_root_id", attributePath, null, null, EMPTY));
            }
        }
        return viewRootIndex;
    }

    private int addEmbeddingViewMappingIfNeeded(TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound, String subviewAliasPrefix, String attributePath, int embeddingViewIndex) {
        if (viewType.getJpaManagedType() instanceof EntityType<?>) {
            boolean embeddingViewMapping = false;
            if (viewType instanceof ViewType<?>) {
                MethodAttribute idAttribute = ((ViewType) viewType).getIdAttribute();
                if (idAttribute.isSubview() || !((AbstractMethodAttribute) idAttribute).isUpdateMappable()) {
                    embeddingViewMapping = true;
                }
            } else {
                embeddingViewMapping = true;
            }
            if (embeddingViewMapping) {
                embeddingViewIndex = tupleOffset + mapperBuilder.mapperIndex();
                javax.persistence.metamodel.SingularAttribute<?, ?> singleIdAttribute = JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) viewType.getJpaManagedType());
                mapperBuilder.addMapper(createMapper((BasicUserTypeStringSupport<?>) null, singleIdAttribute.getName(), subviewAliasPrefix + "_embedding_view_id", attributePath, null, null, EMPTY));
            }
        }
        return embeddingViewIndex;
    }

    private boolean correlatesThis(EntityViewManagerImpl evm, ExpressionFactory ef, Class<?> managedTypeClass, Class<?> correlationBasisEntity, String correlationBasisExpression, Predicate correlationPredicate, String correlationKeyAlias) {
        if (correlationBasisEntity == null || !correlationBasisEntity.isAssignableFrom(managedTypeClass)) {
            return false;
        }
        ExtendedManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, managedTypeClass);
        ExtendedManagedType<?> correlationBasisManagedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, correlationBasisEntity);

        Iterator<? extends javax.persistence.metamodel.SingularAttribute<?, ?>> iterator = managedType.getIdAttributes().iterator();
        javax.persistence.metamodel.SingularAttribute<?, ?> idAttribute = iterator.next();
        if (iterator.hasNext()) {
            return false;
        }

        if (!"this".equals(correlationBasisExpression) && !correlationBasisExpression.equals(idAttribute.getName())) {
            return false;
        }

        String left;
        String right;
        if (correlationPredicate instanceof EqPredicate) {
            left = ((EqPredicate) correlationPredicate).getLeft().toString();
            right = ((EqPredicate) correlationPredicate).getRight().toString();
        } else if (correlationPredicate instanceof InPredicate) {
            left = ((InPredicate) correlationPredicate).getLeft().toString();
            List<Expression> list = ((InPredicate) correlationPredicate).getRight();
            if (list.size() > 1) {
                return false;
            }
            right = list.get(0).toString();
        } else {
            return false;
        }

        if ("this".equals(correlationBasisExpression)) {
            return "this".equalsIgnoreCase(left) && correlationKeyAlias.equals(right)
                    || "this".equalsIgnoreCase(right) && correlationKeyAlias.equals(left);
        } else if (correlationBasisExpression.equals(idAttribute.getName())) {
            iterator = correlationBasisManagedType.getIdAttributes().iterator();
            javax.persistence.metamodel.SingularAttribute<?, ?> correlatedIdAttribute = iterator.next();
            if (iterator.hasNext()) {
                return false;
            }

            return correlatedIdAttribute.getName().equals(left) && correlationKeyAlias.equals(right)
                    || correlatedIdAttribute.getName().equals(right) && correlationKeyAlias.equals(left);
        } else {
            return false;
        }
    }

    private static int flatViewIdPosition(Attribute<?, ?> mappingAttribute) {
        // We encode the negative attribute or parameter index to identify the correct embeddable role
        int index;
        if (mappingAttribute instanceof AbstractMethodAttribute<?, ?>) {
            index = ((AbstractMethodAttribute<?, ?>) mappingAttribute).getAttributeIndex();
        } else {
            index = ((ParameterAttribute<?, ?>) mappingAttribute).getIndex();
        }

        if (index == 0) {
            // Use the minimum value to represent the "negative" of zero
            return Integer.MIN_VALUE;
        }
        return -index;
    }

    private Type<?> getType(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute<?, ?, ?>) {
            return ((PluralAttribute<?, ?, ?>) attribute).getElementType();
        } else {
            return ((SingularAttribute<?, ?>) attribute).getType();
        }
    }

    private void applyBasicIdMapping(MappingAttribute<? super T, ?> mappingAttribute, String parentAttributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String attributePath = getAttributePath(parentAttributePath, mappingAttribute, false);
        mapperBuilder.addMapper(createMapper(getType(mappingAttribute), mapperBuilder.getIdMapping(mappingAttribute, false), mapperBuilder.getAlias(mappingAttribute, false), attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), mappingAttribute.getFetches()));
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        mapperBuilder.addMapper(createMapper(getType(mappingAttribute), mapperBuilder.getMapping(mappingAttribute), mapperBuilder.getAlias(mappingAttribute, false), attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), mappingAttribute.getFetches()));
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder) {
        mapperBuilder.addQueryParam(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, String attributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        @SuppressWarnings("unchecked")
        SubqueryProviderFactory factory = attribute.getSubqueryProviderFactory();
        String alias = mapperBuilder.getAlias(attribute, false);
        String subqueryAlias = attribute.getSubqueryAlias();
        String viewPath = mapperBuilder.getMapping();
        String subqueryExpression = attribute.getSubqueryExpression();

        TupleElementMapper mapper;
        if (subqueryExpression.isEmpty()) {
            if (alias != null) {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedAliasSubqueryTupleElementMapper(attribute.getType(), factory, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), alias);
                } else {
                    mapper = new AliasSubqueryTupleElementMapper(attribute.getType(), factory.create(null, null), attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), alias);
                }
            } else {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedSubqueryTupleElementMapper(attribute.getType(), factory, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath());
                } else {
                    mapper = new SimpleSubqueryTupleElementMapper(attribute.getType(), factory.create(null, null), attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath());
                }
            }
        } else {
            subqueryExpression = mapperBuilder.getMapping(attribute);
            if (alias != null) {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedAliasExpressionSubqueryTupleElementMapper(attribute.getType(), factory, subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), alias);
                } else {
                    mapper = new AliasExpressionSubqueryTupleElementMapper(attribute.getType(), factory.create(null, null), subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), alias);
                }
            } else {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedExpressionSubqueryTupleElementMapper(attribute.getType(), factory, subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath());
                } else {
                    mapper = new ExpressionSubqueryTupleElementMapper(attribute.getType(), factory.create(null, null), subqueryExpression, subqueryAlias, attributePath, viewPath, embeddingViewJpqlMacro.getEmbeddingViewPath());
                }
            }
        }
        mapperBuilder.addMapper(mapper);
    }

    private void applyBasicCorrelatedMapping(AbstractAttribute<?, ?> attribute, String attributePath, TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound, ExpressionFactory ef, int batchSize, boolean dirtyTracking, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        Expression correlationResult = attribute.getCorrelationResultExpression();
        CorrelationProviderFactory factory = attribute.getCorrelationProviderFactory();
        String correlationBasis = attribute.getCorrelationBasis();
        String correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        if (attribute.getFetchStrategy() == FetchStrategy.JOIN) {
            String alias = mapperBuilder.getAlias(attribute, false);
            correlationBasis = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());

            TupleElementMapper mapper;
            String joinBase = mapperBuilder.getMapping();
            String joinCorrelationAttributePath = mapperBuilder.getJoinCorrelationAttributePath(attributePath);
            String embeddingViewPath = joinBase;
            if (factory.isParameterized()) {
                mapper = new ParameterizedExpressionCorrelationJoinTupleElementMapper(factory, ef, joinBase, correlationBasis, attribute.getCorrelationResultExpression(), alias, joinCorrelationAttributePath, embeddingViewPath, attribute.getFetches(), createLimiter(correlationAlias, attribute));
            } else {
                mapper = new ExpressionCorrelationJoinTupleElementMapper(factory.create(null, null), ef, joinBase, correlationBasis, attribute.getCorrelationResultExpression(), alias, joinCorrelationAttributePath, embeddingViewPath, attribute.getFetches(), createLimiter(correlationAlias, attribute));
            }
            mapperBuilder.addMapper(mapper);
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), ef);
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = AbstractAttribute.stripThisFromMapping(correlationBasis);
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            String embeddingViewPath = mapperBuilder.getMapping();
            boolean correlatesThis = correlatesThis(evm, ef, managedTypeClass, attribute.getCorrelated(), correlationBasisExpression, attribute.getCorrelationPredicate(), attribute.getCorrelationKeyAlias());
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            // We need a special mapping for the VIEW_ROOT/EMBEDDING_VIEW macro in certain cases
            viewRootIndex = addViewRootMappingIfNeeded(mapperBuilder, featuresFound, subviewAliasPrefix, attributePath, viewRootIndex);
            embeddingViewIndex = addEmbeddingViewMappingIfNeeded(mapperBuilder, featuresFound, subviewAliasPrefix, attributePath, embeddingViewIndex);

            if (batchSize == -1) {
                batchSize = 1;
            }

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case SET:
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute),
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute)));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), ef);
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            String embeddingViewPath = mapperBuilder.getMapping();
            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        }
                        break;
                    case SET:
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute),
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(correlationAlias, attribute)));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }
    }

    private Class<?> getCorrelationBasisType(Expression correlationBasisExpression, String correlationBasis, ExpressionFactory ef) {
        if (correlationBasis.isEmpty()) {
            return managedTypeClass;
        }
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedTypeClass, entityMetamodel, evm.getCriteriaBuilderFactory().getRegisteredFunctions());
        correlationBasisExpression.accept(visitor);
        Collection<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTypes = visitor.getPossibleTargetTypes();
        if (possibleTypes.size() > 1) {
            throw new IllegalArgumentException("The correlation basis '" + correlationBasis + "' is ambiguous in the context of the managed type '" + managedTypeClass.getName() + "'!");
        }
        // It must have one, otherwise a parse error would have been thrown already
        Class<?> entityClazz = possibleTypes.iterator().next().getLeafBaseValueClass();

        if (entityClazz == null) {
            throw new IllegalArgumentException("Could not resolve the correlation basis '" + correlationBasis + "' in the context of the managed type '" + managedTypeClass.getName() + "'!");
        }

        return entityClazz;
    }

    private Class<?> getCorrelationBasisEntityType(Class<?> entityClazz) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ManagedType<?> managedType = entityMetamodel.getManagedType(entityClazz);
        // Return the class if it is identifiable, otherwise return null. When null, it will use fromIdentifiableValues in correlation builders to correlate values
        if (JpaMetamodelUtils.isIdentifiable(managedType)) {
            return entityClazz;
        }
        return null;
    }

    private BasicUserTypeStringSupport<Object> getCorrelationKeyExpressionBasicTypeSupport(Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        if (correlationBasisEntity == null) {
            return (BasicUserTypeStringSupport<Object>) evm.getMetamodel().getBasicUserType(correlationBasisType);
        }
        javax.persistence.metamodel.SingularAttribute<?, ?> idAttribute = ((ExtendedManagedType<?>) evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, correlationBasisEntity))
                .getIdAttributes()
                .iterator()
                .next();
        return (BasicUserTypeStringSupport<Object>) evm.getMetamodel().getBasicUserType(JpaMetamodelUtils.resolveFieldClass(correlationBasisEntity, idAttribute));
    }

    private String getAttributePath(String attributePath, Attribute<?, ?> attribute, boolean isKey) {
        String attributeName;
        if (attribute instanceof MethodAttribute<?, ?>) {
            attributeName = ((MethodAttribute<?, ?>) attribute).getName();
        } else {
            attributeName = "$" + ((ParameterAttribute<?, ?>) attribute).getIndex();
        }

        if (attributePath == null || attributePath.isEmpty()) {
            if (isKey) {
                return ("KEY(" + attributeName + ")").intern();
            } else {
                return attributeName.intern();
            }
        }

        if (isKey) {
            return ("KEY(" + attributePath + "." + attributeName + ")").intern();
        } else {
            return (attributePath + "." + attributeName).intern();
        }
    }

    public ObjectBuilder<T> createObjectBuilder(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, int suffix, boolean isSubview, boolean nullFlatViewIfEmpty) {
        boolean hasOffset = tupleOffset != 0 || suffix != 0;
        ObjectBuilder<T> result;

        result = new ViewTypeObjectBuilder<T>(this, parameterHolder, optionalParameters, entityViewConfiguration == null ? null : entityViewConfiguration.getViewJpqlMacro(), entityViewConfiguration == null ? null : entityViewConfiguration.getEmbeddingViewJpqlMacro(), entityViewConfiguration == null ? Collections.<String>emptySet() : entityViewConfiguration.getFetches(), nullFlatViewIfEmpty);

        if (hasSubtypes) {
            result = new InheritanceReducerViewTypeObjectBuilder<>((ViewTypeObjectBuilder<T>) result, tupleOffset, suffix, mappers.length, !isSubview && (tupleOffset > 0 || suffix > 0), subtypeInstantiators);
        } else if (hasOffset || isSubview || hasIndexedCollections || hasSubviews || hasSubqueryCorrelation) {
            result = new ReducerViewTypeObjectBuilder<T>(result, tupleOffset, suffix, mappers.length, !isSubview && (tupleOffset > 0 || suffix > 0));
        }

        if (hasParameters) {
            result = new ParameterViewTypeObjectBuilder<T>(result, this, parameterHolder, optionalParameters, tupleOffset);
        }

        if (tupleTransformatorFactory.hasTransformers() && !isSubview) {
            result = new ChainingObjectBuilder<T>(tupleTransformatorFactory, result, parameterHolder, optionalParameters, entityViewConfiguration, tupleOffset);
        }

        return result;
    }

    public Class<?> getViewClass() {
        return viewType.getJavaType();
    }

    public ManagedViewType<?> getViewRoot() {
        return viewRoot;
    }

    public ObjectInstantiator<T> getObjectInstantiator() {
        return objectInstantiator;
    }

    public TupleElementMapper[] getMappers() {
        return mappers;
    }

    public TupleParameterMapper getParameterMapper() {
        return parameterMapper;
    }

    public boolean hasId() {
        return hasId;
    }

    public boolean hasSubtypes() {
        return hasSubtypes;
    }

    public int[] getIdPositions() {
        return idPositions;
    }

    public boolean hasParameters() {
        return hasParameters;
    }

    public int getTupleOffset() {
        return tupleOffset;
    }

    public int getEffectiveTupleSize() {
        return effectiveTupleSize;
    }

    public TupleTransformatorFactory getTupleTransformatorFactory() {
        return tupleTransformatorFactory;
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static class Key {

        private final ExpressionFactory ef;
        private final Map<String, MacroFunction> macros;
        private final ManagedViewTypeImpl<Object> viewType;
        private final MappingConstructorImpl<Object> constructor;
        private final String entityViewRoot;
        private final String embeddingViewPath;
        private final int offset;
        private final boolean cacheable;

        public Key(MacroConfigurationExpressionFactory ef, ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> constructor, String entityViewRoot, String embeddingViewPath, int offset) {
            this.ef = ef.getExpressionFactory();
            Map<String, MacroFunction> macros;
            if (ef.getDefaultMacroConfiguration() == null) {
                macros = Collections.emptyMap();
                this.cacheable = true;
            } else {
                boolean cacheable = true;
                Map<String, MacroFunction> map = ef.getDefaultMacroConfiguration().getMacros();
                macros = new HashMap<>(Math.max(0, map.size() - 2));
                // We don't care about the view root and embedding view macro as the relevant state that is interesting for caching is already present in entityViewRoot and embeddingViewPath
                for (Map.Entry<String, MacroFunction> entry : map.entrySet()) {
                    switch (entry.getKey()) {
                        case "VIEW_ROOT":
                            break;
                        case "EMBEDDING_VIEW":
                            break;
                        default:
                            if (!entry.getValue().supportsCaching()) {
                                cacheable = false;
                            }
                            macros.put(entry.getKey(), entry.getValue());
                            break;
                    }
                }

                this.cacheable = cacheable;
            }
            this.macros = macros;
            this.viewType = (ManagedViewTypeImpl<Object>) viewType;
            this.constructor = (MappingConstructorImpl<Object>) constructor;
            this.entityViewRoot = entityViewRoot;
            this.embeddingViewPath = embeddingViewPath;
            this.offset = offset;
        }

        public ViewTypeObjectBuilderTemplate<?> createValue(EntityViewManagerImpl evm, ProxyFactory proxyFactory, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, MacroConfigurationExpressionFactory ef) {
            return new ViewTypeObjectBuilderTemplate<Object>(viewType, entityViewRoot, "", viewType.getJavaType().getSimpleName(), entityViewRoot, entityViewRoot, new TupleIdDescriptor(), new TupleIdDescriptor(), offset, viewJpqlMacro, embeddingViewJpqlMacro, null, evm, ef, viewType, constructor, proxyFactory);
        }

        public boolean isCacheable() {
            return cacheable;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.ef != null ? this.ef.hashCode() : 0);
            hash = 83 * hash + (this.macros != null ? this.macros.hashCode() : 0);
            hash = 83 * hash + (this.viewType != null ? this.viewType.hashCode() : 0);
            hash = 83 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
            hash = 83 * hash + (this.entityViewRoot != null ? this.entityViewRoot.hashCode() : 0);
            hash = 83 * hash + (this.embeddingViewPath != null ? this.embeddingViewPath.hashCode() : 0);
            hash = 83 * hash + offset;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.ef != other.ef && (this.ef == null || !this.ef.equals(other.ef))) {
                return false;
            }
            if (this.macros != other.macros && (this.macros == null || !this.macros.equals(other.macros))) {
                return false;
            }
            if (this.viewType != other.viewType && (this.viewType == null || !this.viewType.equals(other.viewType))) {
                return false;
            }
            if (this.constructor != other.constructor && (this.constructor == null || !this.constructor.equals(other.constructor))) {
                return false;
            }
            if (this.entityViewRoot != other.entityViewRoot && (this.entityViewRoot == null || !this.entityViewRoot.equals(other.entityViewRoot))) {
                return false;
            }
            if (this.embeddingViewPath != other.embeddingViewPath && (this.embeddingViewPath == null || !this.embeddingViewPath.equals(other.embeddingViewPath))) {
                return false;
            }
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }
    }
}
