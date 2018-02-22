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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.ScalarTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.SubqueryProviderFactory;
import com.blazebit.persistence.view.impl.SubqueryProviderHelper;
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
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.SimpleSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapperBuilder;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleParameterMapper;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.CollectionTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.IndexedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.MapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SubviewTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.BasicCorrelator;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSubviewJoinTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.SubviewCorrelator;
import com.blazebit.persistence.view.impl.proxy.AbstractReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ConstructorReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.StaticFactoryReflectionInstantiator;
import com.blazebit.persistence.view.impl.type.NormalMapUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.NormalSetUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.OrderedCollectionUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.OrderedMapUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.OrderedSetUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.SortedMapUserTypeWrapper;
import com.blazebit.persistence.view.impl.type.SortedSetUserTypeWrapper;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeObjectBuilderTemplate<T> {

    private static final String[] EMPTY = new String[0];
    private static final int FEATURE_PARAMETERS = 0;
    private static final int FEATURE_INDEXED_COLLECTIONS = 1;
    private static final int FEATURE_SUBVIEWS = 2;

    private final Class<?> viewClass;
    private final ObjectInstantiator<T> objectInstantiator;
    private final ObjectInstantiator<T>[] subtypeInstantiators;
    private final TupleElementMapper[] mappers;
    private final TupleParameterMapper parameterMapper;
    private final int effectiveTupleSize;
    private final boolean hasId;
    private final boolean hasParameters;
    private final boolean hasIndexedCollections;
    private final boolean hasSubviews;
    private final boolean hasSubtypes;

    private final ManagedViewTypeImplementor<?> viewRoot;
    private final String viewRootAlias;
    private final Class<?> managedTypeClass;
    private final int[] idPositions;
    private final int tupleOffset;
    private final EntityViewManagerImpl evm;
    private final ExpressionFactory ef;
    private final ProxyFactory proxyFactory;
    private final TupleTransformatorFactory tupleTransformatorFactory;

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate(ManagedViewTypeImplementor<?> viewRoot, String viewRootAlias, String attributePath, String aliasPrefix, String mappingPrefix, String idPrefix, int[] idPositions, int tupleOffset,
                                          Map<ManagedViewTypeImplementor<? extends T>, String> inheritanceSubtypeMappings, EntityViewManagerImpl evm, ExpressionFactory ef, ManagedViewTypeImplementor<T> managedViewType, MappingConstructorImpl<T> mappingConstructor, ProxyFactory proxyFactory) {
        ViewType<T> viewType;
        if (managedViewType instanceof ViewType<?>) {
            viewType = (ViewType<T>) managedViewType;
            this.hasId = true;
        } else {
            viewType = null;
            this.hasId = false;
        }

        if (mappingConstructor == null) {
            if (managedViewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + managedViewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (managedViewType.getConstructors().size() == 1) {
                mappingConstructor = (MappingConstructorImpl<T>) managedViewType.getConstructors().toArray()[0];
            }
        }

        this.viewClass = managedViewType.getJavaType();
        this.viewRoot = viewRoot;
        this.viewRootAlias = viewRootAlias;
        this.managedTypeClass = managedViewType.getEntityClass();
        this.idPositions = idPositions;
        this.tupleOffset = tupleOffset;
        this.evm = evm;
        this.ef = ef;
        this.proxyFactory = proxyFactory;

        ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> inheritanceSubtypeConfiguration = managedViewType.getInheritanceSubtypeConfiguration(inheritanceSubtypeMappings);
        Map<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>>> attributeMap = new LinkedHashMap<>(inheritanceSubtypeConfiguration.getAttributesClosure());

        int attributeCount = attributeMap.size();

        // We have special handling for the id attribute since we need to know it's position in advance
        // Therefore we have to remove it so that it doesn't get processed as normal attribute
        if (viewType != null) {
            attributeMap.remove(new ManagedViewTypeImpl.AttributeKey(0, viewType.getIdAttribute().getName()));
        }
        
        List<AbstractParameterAttribute<? super T, ?>> parameterAttributeList;

        if (mappingConstructor == null) {
            parameterAttributeList = Collections.emptyList();
        } else {
            parameterAttributeList = mappingConstructor.getSubtypeConstructorConfiguration(inheritanceSubtypeMappings).getParameterAttributesClosure();
        }

        attributeCount += parameterAttributeList.size();

        List<TupleElementMapper> mappingList = new ArrayList<>(attributeCount);
        List<String> parameterMappingList = new ArrayList<>(attributeCount);
        List<Class<?>> parameterTypes = new ArrayList<>(attributeCount);
        boolean[] featuresFound = new boolean[3];

        final TupleTransformatorFactory tupleTransformatorFactory = new TupleTransformatorFactory();
        final EntityMetamodel metamodel = evm.getMetamodel().getEntityMetamodel();
        TupleElementMapperBuilder mainMapperBuilder = new TupleElementMapperBuilder(0, null, aliasPrefix, mappingPrefix, idPrefix, null, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);

        boolean collectMutableBasicTypes = viewType != null && (managedViewType.isUpdatable() || managedViewType.isCreatable()) && managedViewType.getFlushMode() != FlushMode.FULL;
        List<AbstractReflectionInstantiator.MutableBasicUserTypeEntry> mutableBasicUserTypes = new ArrayList<>();
        List<AbstractReflectionInstantiator.TypeConverterEntry> typeConverterEntries = new ArrayList<>();
        int initialStateIndex = 0;

        // Add inheritance type extraction
        if (inheritanceSubtypeConfiguration.hasSubtypes()) {
            String mapping = inheritanceSubtypeConfiguration.getInheritanceDiscriminatorMapping();
            mainMapperBuilder.addMapper(createMapper(mainMapperBuilder.getMapping(mapping), mainMapperBuilder.getAlias("class"), EMPTY));
        }

        if (viewType != null) {
            MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) idAttribute;

            parameterTypes.add(idAttribute.getConvertedJavaType());

            // An id mapping can only be basic or a flat subview
            if (idAttribute.isSubview()) {
                ManagedViewTypeImpl<Object[]> subViewType = (ManagedViewTypeImpl<Object[]>) ((SingularAttribute<?, ?>) mappingAttribute).getType();
                applySubviewIdMapping(mappingAttribute, attributePath, idPositions, subViewType, mainMapperBuilder, false);
            } else {
                applyBasicIdMapping(mappingAttribute, mainMapperBuilder);
            }

        }

        // Add tuple element mappers for attributes
        for (Map.Entry<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>>> attributeEntry : attributeMap.entrySet()) {
            ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>> constrainedAttribute = attributeEntry.getValue();
            parameterTypes.add(constrainedAttribute.getAttribute().getConvertedJavaType());
            if (constrainedAttribute.requiresCaseWhen()) {
                // Collect all mappers for all constraints
                List<Map.Entry<String, TupleElementMapperBuilder>> builders = new ArrayList<>(constrainedAttribute.getSelectionConstrainedAttributes().size());
                for (Map.Entry<String, AbstractMethodAttribute<? super T, ?>> entry : constrainedAttribute.getSelectionConstrainedAttributes()) {
                    String constraint = entry.getKey();
                    AbstractMethodAttribute<? super T, ?> attribute = entry.getValue();
                    EntityType<?> treatType = getTreatType(metamodel, managedViewType, attribute.getDeclaringType());
                    TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(mappingList.size(), constraint, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef);
                    applyMapping(constrainedAttribute.getAttribute(), attributePath, mapperBuilder, featuresFound);
                    builders.add(new AbstractMap.SimpleEntry<>(constraint, mapperBuilder));
                }
                ConstrainedTupleElementMapper.addMappers(mappingList, parameterMappingList, tupleTransformatorFactory, builders);
                // TODO: if we want to support inheritance with updatable entity views, we should collect mutable basic types here too
            } else {
                AbstractMethodAttribute<? super T, ?> attribute = constrainedAttribute.getAttribute();

                if (attribute instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                    TypeConverter<Object, Object> converter = (TypeConverter<Object, Object>) singularAttribute.getType().getConverter();
                    if (converter != null) {
                        typeConverterEntries.add(new AbstractReflectionInstantiator.TypeConverterEntry(attribute.getAttributeIndex(), converter));
                    }
                }

                if (collectMutableBasicTypes && attribute.isMutable()) {
                    if (attribute instanceof SingularAttribute<?, ?>) {
                        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                        Type<?> t = singularAttribute.getType();
                        BasicUserType<Object> elementType = t instanceof BasicType<?> ? ((BasicType<Object>) t).getUserType() : null;
                        if (isMutableBasicUserType(elementType)) {
                            mutableBasicUserTypes.add(new AbstractReflectionInstantiator.MutableBasicUserTypeEntry(initialStateIndex, ((BasicType) singularAttribute.getType()).getUserType()));
                        }
                    } else {
                        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                        Type<?> t = pluralAttribute.getElementType();
                        BasicUserType<Object> elementType = t instanceof BasicType<?> ? ((BasicType<Object>) t).getUserType() : null;
                        if (pluralAttribute instanceof MapAttribute<?, ?, ?>) {
                            MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                            t = mapAttribute.getKeyType();
                            BasicUserType<Object> keyType = t instanceof BasicType<?> ? ((BasicType<Object>) t).getUserType() : null;

                            if (isMutableBasicUserType(keyType) || isMutableBasicUserType(elementType)) {
                                mutableBasicUserTypes.add(new AbstractReflectionInstantiator.MutableBasicUserTypeEntry(initialStateIndex, createMapUserTypeWrapper(mapAttribute, keyType, elementType)));
                            }
                        } else {
                            if (isMutableBasicUserType(elementType)) {
                                mutableBasicUserTypes.add(new AbstractReflectionInstantiator.MutableBasicUserTypeEntry(initialStateIndex, createCollectionUserTypeWrapper(pluralAttribute, elementType)));
                            }
                        }
                    }

                    initialStateIndex++;
                }

                EntityType<?> treatType = getTreatType(metamodel, managedViewType, attribute.getDeclaringType());
                TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);
                applyMapping(attribute, attributePath, mapperBuilder, featuresFound);
            }
        }

        int subtypeIndex = -1;
        MappingConstructor<?> lastConstructor;
        if (inheritanceSubtypeConfiguration.hasSubtypes()) {
            lastConstructor = null;
        } else {
            lastConstructor = mappingConstructor;
        }

        // Add tuple element mappers for constructor parameters
        for (ParameterAttribute<? super T, ?> parameterAttribute : parameterAttributeList) {
            String paramAliasPrefix;
            if (lastConstructor == parameterAttribute.getDeclaringConstructor()) {
                paramAliasPrefix = aliasPrefix;
            } else {
                lastConstructor = parameterAttribute.getDeclaringConstructor();
                paramAliasPrefix = aliasPrefix + "_" + (++subtypeIndex) + "_" + lastConstructor.getDeclaringType().getJavaType().getSimpleName();
            }
            parameterTypes.add(parameterAttribute.getConvertedJavaType());
            EntityType<?> treatType = getTreatType(metamodel, managedViewType, parameterAttribute.getDeclaringType());
            TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, paramAliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, tupleTransformatorFactory);
            applyMapping((AbstractAttribute<?, ?>) parameterAttribute, attributePath, mapperBuilder, featuresFound);
        }

        ManagedViewTypeImplementor<T> viewTypeBase = null;
        if (this.hasSubtypes = inheritanceSubtypeConfiguration.hasSubtypes()) {
            viewTypeBase = managedViewType;
        }
        Class<?>[] constructorParameterTypes = parameterTypes.toArray(new Class[parameterTypes.size()]);
        // This can only happen for subview mappings
        if (!inheritanceSubtypeConfiguration.getInheritanceSubtypes().contains(managedViewType)) {
            this.objectInstantiator = null;
        } else {
            this.objectInstantiator = createInstantiator(managedViewType, viewTypeBase, inheritanceSubtypeConfiguration.getConfigurationIndex(), mappingConstructor, constructorParameterTypes, evm, mutableBasicUserTypes, typeConverterEntries);
        }

        List<ObjectInstantiator<T>> subtypeInstantiators = new ArrayList<>(inheritanceSubtypeConfiguration.getInheritanceSubtypes().size());

        for (ManagedViewTypeImplementor<?> subtype : inheritanceSubtypeConfiguration.getInheritanceSubtypes()) {
            if (subtype == managedViewType) {
                subtypeInstantiators.add(0, objectInstantiator);
            } else {
                ObjectInstantiator<T> instantiator = createInstantiator((ManagedViewType<? extends T>) subtype, managedViewType, inheritanceSubtypeConfiguration.getConfigurationIndex(), mappingConstructor, constructorParameterTypes, evm, mutableBasicUserTypes, typeConverterEntries);
                subtypeInstantiators.add(instantiator);
            }
        }

        this.hasParameters = featuresFound[FEATURE_PARAMETERS];
        this.hasIndexedCollections = featuresFound[FEATURE_INDEXED_COLLECTIONS];
        this.hasSubviews = featuresFound[FEATURE_SUBVIEWS];
        this.subtypeInstantiators = subtypeInstantiators.toArray(new ObjectInstantiator[subtypeInstantiators.size()]);
        this.effectiveTupleSize = attributeCount;
        this.mappers = mappingList.toArray(new TupleElementMapper[mappingList.size()]);
        this.parameterMapper = new TupleParameterMapper(parameterMappingList, tupleOffset);
        this.tupleTransformatorFactory = tupleTransformatorFactory;
    }

    private boolean isMutableBasicUserType(BasicUserType<Object> elementType) {
        return elementType != null && elementType.isMutable() && (!elementType.supportsDirtyChecking() && elementType.supportsDeepCloning() || elementType.supportsDirtyTracking());
    }

    @SuppressWarnings("unchecked")
    private BasicUserType<Object> createCollectionUserTypeWrapper(PluralAttribute<?, ?, ?> pluralAttribute, BasicUserType<Object> elementType) {
        if (pluralAttribute instanceof SetAttribute<?, ?>) {
            if (pluralAttribute.isSorted()) {
                return (BasicUserType<Object>) (BasicUserType<?>) new SortedSetUserTypeWrapper<>(elementType, (Comparator<Object>) pluralAttribute.getComparator());
            } else if (pluralAttribute.isOrdered()) {
                return (BasicUserType<Object>) (BasicUserType<?>) new OrderedSetUserTypeWrapper<>(elementType);
            } else {
                return (BasicUserType<Object>) (BasicUserType<?>) new NormalSetUserTypeWrapper<>(elementType);
            }
        } else {
            return (BasicUserType<Object>) (BasicUserType<?>) new OrderedCollectionUserTypeWrapper<>(elementType);
        }
    }

    @SuppressWarnings("unchecked")
    private BasicUserType<Object> createMapUserTypeWrapper(MapAttribute<?, ?, ?> mapAttribute, BasicUserType<Object> keyType, BasicUserType<Object> elementType) {
        if (mapAttribute.isSorted()) {
            return (BasicUserType<Object>) (BasicUserType<?>) new SortedMapUserTypeWrapper(keyType, elementType, (Comparator<Object>) mapAttribute.getComparator());
        } else if (mapAttribute.isOrdered()) {
            return (BasicUserType<Object>) (BasicUserType<?>) new OrderedMapUserTypeWrapper<>(keyType, elementType);
        } else {
            return (BasicUserType<Object>) (BasicUserType<?>) new NormalMapUserTypeWrapper<>(keyType, elementType);
        }
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
    private ObjectInstantiator<T> createInstantiator(ManagedViewType<? extends T> managedViewType, ManagedViewTypeImplementor<T> viewTypeBase, int inheritanceConfigurationIndex, MappingConstructorImpl<? extends T> mappingConstructor, Class<?>[] constructorParameterTypes,
                                                     EntityViewManagerImpl entityViewManager, List<AbstractReflectionInstantiator.MutableBasicUserTypeEntry> mutableBasicUserTypes, List<AbstractReflectionInstantiator.TypeConverterEntry> typeConverterEntries) {
        if (viewTypeBase == null) {
            return new ConstructorReflectionInstantiator<>((MappingConstructorImpl<T>) mappingConstructor, proxyFactory, (ManagedViewTypeImplementor<T>) managedViewType, viewTypeBase, constructorParameterTypes, entityViewManager, mutableBasicUserTypes, typeConverterEntries);
        } else {
            return new StaticFactoryReflectionInstantiator<>((MappingConstructorImpl<T>) mappingConstructor, proxyFactory, (ManagedViewTypeImplementor<T>) managedViewType, viewTypeBase, inheritanceConfigurationIndex, constructorParameterTypes, entityViewManager, mutableBasicUserTypes, typeConverterEntries);
        }
    }

    private TupleElementMapper createMapper(String expression, String[] fetches) {
        return createMapper(expression, null, fetches);
    }

    private TupleElementMapper createMapper(String expression, String alias, String[] originalFetches) {
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
            return new AliasExpressionTupleElementMapper(expression, alias, fetches);
        } else {
            return new ExpressionTupleElementMapper(expression, fetches);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyMapping(AbstractAttribute<?, ?> attribute, String parentAttributePath, TupleElementMapperBuilder mapperBuilder, boolean[] featuresFound) {
        String attributePath = getAttributePath(parentAttributePath, attribute, false);
        int batchSize = attribute.getBatchSize();

        if (batchSize == -1) {
            batchSize = attribute.getDeclaringType().getDefaultBatchSize();
        }

        if (attribute.isSubquery()) {
            applySubqueryMapping((SubqueryAttribute<? super T, ?>) attribute, mapperBuilder);
        } else {
            if (attribute.isCollection()) {
                PluralAttribute<? super T, ?, ?> pluralAttribute = (PluralAttribute<? super T, ?, ?>) attribute;
                TypeConverter<Object, Object> keyConverter = null;
                TypeConverter<Object, Object> valueConverter = (TypeConverter<Object, Object>) pluralAttribute.getElementType().getConverter();
                boolean listKey = pluralAttribute.isIndexed() && pluralAttribute instanceof ListAttribute<?, ?>;
                boolean mapKey = pluralAttribute.isIndexed() && pluralAttribute instanceof MapAttribute<?, ?, ?>;
                int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                int mapValueStartIndex = startIndex + 1;
                Set<Class<?>> allowedSubtypes = attribute.getAllowedSubtypes();

                if (listKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    if (pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        throw new IllegalArgumentException("When using a non-join fetch strategy, mappings can't be indexed!");
                    }
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping("INDEX", "_KEY", mappingAttribute, mapperBuilder, EMPTY);
                } else if (mapKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    if (pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        throw new IllegalArgumentException("When using a non-join fetch strategy, mappings can't be indexed!");

                    }
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) pluralAttribute;
                    keyConverter = (TypeConverter<Object, Object>) mapAttribute.getKeyType().getConverter();
                    if (mapAttribute.isKeySubview()) {
                        featuresFound[FEATURE_SUBVIEWS] = true;
                        ManagedViewTypeImpl<Object[]> managedViewType = (ManagedViewTypeImpl<Object[]>) mapAttribute.getKeyType();
                        applySubviewMapping(mappingAttribute, attributePath, idPositions, managedViewType, mapperBuilder, true);
                        mapValueStartIndex = tupleOffset + (mapperBuilder.mapperIndex() - startIndex) + 1;
                    } else {
                        applyCollectionFunctionMapping("KEY", "_KEY", mappingAttribute, mapperBuilder, EMPTY);
                    }
                }

                if (pluralAttribute.isSubview()) {
                    featuresFound[FEATURE_SUBVIEWS] = true;

                    int[] newIdPositions;

                    if (listKey || mapKey) {
                        newIdPositions = new int[idPositions.length + 1];
                        System.arraycopy(idPositions, 0, newIdPositions, 0, idPositions.length);
                        newIdPositions[idPositions.length] = startIndex;
                    } else {
                        newIdPositions = idPositions;
                    }

                    if (pluralAttribute.isCorrelated() || pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        ManagedViewTypeImplementor<Object> managedViewType = (ManagedViewTypeImplementor<Object>) pluralAttribute.getElementType();
                        applyCorrelatedSubviewMapping(attribute, attributePath, newIdPositions, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, batchSize);
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) pluralAttribute.getElementType();
                        applySubviewMapping(mappingAttribute, attributePath, newIdPositions, managedViewType, mapperBuilder, false);
                    }
                } else if (mapKey) {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyCollectionFunctionMapping("VALUE", "", mappingAttribute, mapperBuilder, mappingAttribute.getFetches());
                } else {
                    if (pluralAttribute.isCorrelated() || pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN) {
                        applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, batchSize);
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        applyBasicMapping(mappingAttribute, mapperBuilder);
                    }
                }

                boolean dirtyTracking = pluralAttribute instanceof MethodAttribute<?, ?> && attribute.needsDirtyTracker();
                if (listKey) {
                    if (pluralAttribute.isSorted()) {
                        throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                    } else {
                        mapperBuilder.setTupleListTransformer(new IndexedListTupleListTransformer(idPositions, startIndex, attribute.getCollectionInstantiator(), dirtyTracking, valueConverter));
                    }
                } else if (mapKey) {
                    mapperBuilder.setTupleListTransformer(new MapTupleListTransformer(idPositions, startIndex, mapValueStartIndex, attribute.getMapInstantiator(), dirtyTracking, keyConverter, valueConverter));
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
                    mapperBuilder.setTupleListTransformer(new CollectionTupleListTransformer(idPositions, startIndex, attribute.getCollectionInstantiator(), dirtyTracking, valueConverter));
                }
            } else if (attribute.isQueryParameter()) {
                MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                featuresFound[FEATURE_PARAMETERS] = true;
                applyQueryParameterMapping(mappingAttribute, mapperBuilder);
            } else if (attribute.isSubview()) {
                featuresFound[FEATURE_SUBVIEWS] = true;
                if (attribute.isCorrelated() || attribute.getFetchStrategy() != FetchStrategy.JOIN) {
                    ManagedViewTypeImplementor<Object> managedViewType = (ManagedViewTypeImplementor<Object>) ((SingularAttribute<?, ?>) attribute).getType();
                    applyCorrelatedSubviewMapping(attribute, attributePath, idPositions, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, batchSize);
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) ((SingularAttribute<?, ?>) attribute).getType();
                    applySubviewMapping(mappingAttribute, attributePath, idPositions, managedViewType, mapperBuilder, false);
                }
            } else {
                if (attribute.isCorrelated() || attribute.getFetchStrategy() != FetchStrategy.JOIN) {
                    applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, batchSize);
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyBasicMapping(mappingAttribute, mapperBuilder);
                }
            }
        }
    }

    private void applyCollectionFunctionMapping(String function, String aliasSuffix, MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder, String[] fetches) {
        String expression = function + "(" + mapperBuilder.getMapping(mappingAttribute) + ")";
        String alias = mapperBuilder.getAlias(mappingAttribute, false);
        TupleElementMapper mapper;
        if (alias == null) {
            mapper = createMapper(expression, fetches);
        } else {
            mapper = createMapper(expression, alias + aliasSuffix, fetches);
        }
        mapperBuilder.addMapper(mapper);
    }

    private void applySubviewIdMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, int[] idPositions, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, boolean isKey) {
        applySubviewMapping(mappingAttribute, attributePath, idPositions, managedViewType, mapperBuilder, isKey, true);
    }

    private void applySubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, int[] idPositions, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, boolean isKey) {
        applySubviewMapping(mappingAttribute, attributePath, idPositions, managedViewType, mapperBuilder, isKey, false);
    }

    @SuppressWarnings("unchecked")
    private void applySubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, int[] idPositions, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, boolean isKey, boolean nullIfEmpty) {
        String subviewAttributePath = getAttributePath(attributePath, mappingAttribute, isKey);
        String subviewAliasPrefix = mapperBuilder.getAlias(mappingAttribute, isKey);
        String subviewMappingPrefix = mapperBuilder.getMapping(mappingAttribute, isKey);
        String subviewIdPrefix = mapperBuilder.getMapping(mappingAttribute, isKey);
        int[] subviewIdPositions;
        int startIndex;
        boolean updatableObjectCache;

        if (managedViewType instanceof ViewType<?>) {
            subviewIdPositions = new int[idPositions.length + 1];
            System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
            subviewIdPositions[idPositions.length] = tupleOffset + mapperBuilder.mapperIndex();
            startIndex = tupleOffset + mapperBuilder.mapperIndex();
            updatableObjectCache = managedViewType.isUpdatable();
        } else {
            subviewIdPositions = idPositions;
            startIndex = tupleOffset + mapperBuilder.mapperIndex();
            updatableObjectCache = false;
        }

        Map<ManagedViewTypeImplementor<? extends Object[]>, String> inheritanceSubtypeMappings;

        if (isKey) {
            inheritanceSubtypeMappings = (Map<ManagedViewTypeImplementor<? extends Object[]>, String>) (Map<?, ?>) ((MapAttribute<?, ?, ?>) mappingAttribute).getKeyInheritanceSubtypeMappings();
        } else if (mappingAttribute instanceof PluralAttribute<?, ?, ?>) {
            inheritanceSubtypeMappings = (Map<ManagedViewTypeImplementor<? extends Object[]>, String>) (Map<?, ?>) ((PluralAttribute<?, ?, ?>) mappingAttribute).getElementInheritanceSubtypeMappings();
        } else {
            inheritanceSubtypeMappings = (Map<ManagedViewTypeImplementor<? extends Object[]>, String>) (Map<?, ?>) ((SingularAttribute<?, ?>) mappingAttribute).getInheritanceSubtypeMappings();
        }

        ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions,
                startIndex, inheritanceSubtypeMappings, evm, ef, managedViewType, getSubviewMappingConstructor(managedViewType), proxyFactory);
        mapperBuilder.addMappers(template.mappers);
        mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
        mapperBuilder.addTupleTransformerFactory(new SubviewTupleTransformerFactory(template, updatableObjectCache, nullIfEmpty));
    }

    @SuppressWarnings("unchecked")
    private void applyCorrelatedSubviewMapping(AbstractAttribute<?, ?> attribute, String attributePath, int[] idPositions, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, int batchSize) {
        String correlationResult = attribute.getCorrelationResult();
        Class<? extends CorrelationProvider> correlationProvider = attribute.getCorrelationProvider();
        String correlationBasis = attribute.getCorrelationBasis();
        String subviewAttributePath = getAttributePath(attributePath, attribute, false);
        CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlationProvider);

        if (attribute.getFetchStrategy() == FetchStrategy.JOIN) {
            @SuppressWarnings("unchecked")
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            correlationBasis = mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis));
            String subviewIdPrefix;
            if (correlationResult.isEmpty()) {
                subviewIdPrefix = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
            } else {
                subviewIdPrefix = correlationResult;
            }
            String subviewMappingPrefix = subviewIdPrefix;
            int[] subviewIdPositions;
            int startIndex;

            if (managedViewType instanceof ViewType<?>) {
                subviewIdPositions = new int[idPositions.length + 1];
                System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
                subviewIdPositions[idPositions.length] = tupleOffset + mapperBuilder.mapperIndex();
                startIndex = tupleOffset + mapperBuilder.mapperIndex();
            } else {
                subviewIdPositions = idPositions;
                startIndex = tupleOffset + mapperBuilder.mapperIndex();
            }

            Map<ManagedViewTypeImplementor<? extends Object[]>, String> inheritanceSubtypeMappings;

            if (attribute instanceof PluralAttribute<?, ?, ?>) {
                inheritanceSubtypeMappings = (Map<ManagedViewTypeImplementor<? extends Object[]>, String>) (Map<?, ?>) ((PluralAttribute<?, ?, ?>) attribute).getElementInheritanceSubtypeMappings();
            } else {
                inheritanceSubtypeMappings = (Map<ManagedViewTypeImplementor<? extends Object[]>, String>) (Map<?, ?>) ((SingularAttribute<?, ?>) attribute).getInheritanceSubtypeMappings();
            }

            @SuppressWarnings("unchecked")
            ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions,
                    startIndex, inheritanceSubtypeMappings, evm, ef, managedViewType, getSubviewMappingConstructor(managedViewType), proxyFactory);
            mapperBuilder.addMappers(template.mappers);

            mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
            mapperBuilder.addTupleTransformerFactory(new CorrelatedSubviewJoinTupleTransformerFactory(template, factory, mapperBuilder.getMapping(""), correlationBasis, correlationResult, attributePath, attribute.getFetches()));
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(AbstractAttribute.stripThisFromMapping(correlationBasis));
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisType);

            mapperBuilder.addMapper(createMapper(mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisEntity), subviewAliasPrefix, attribute.getFetches()));

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
                        new SubviewCorrelator(managedViewType, getSubviewMappingConstructor(managedViewType), evm, subviewAliasPrefix),
                        viewRoot, correlationResult, factory, attributePath, attribute.getFetches(), startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated()
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, getSubviewMappingConstructor(managedViewType), evm, subviewAliasPrefix),
                        viewRoot, correlationResult, factory, attributePath, attribute.getFetches(), startIndex, batchSize, correlationBasisType, correlationBasisEntity
                ));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(AbstractAttribute.stripThisFromMapping(correlationBasis));
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisType);
            String correlationKeyExpression = mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis));

            mapperBuilder.addMapper(createMapper(correlationKeyExpression, subviewAliasPrefix, attribute.getFetches()));

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
                        new SubviewCorrelator(managedViewType, getSubviewMappingConstructor(managedViewType), evm, subviewAliasPrefix),
                        viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, attributePath, attribute.getFetches(), startIndex, correlationBasisType, correlationBasisEntity,
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated()
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, getSubviewMappingConstructor(managedViewType), evm, subviewAliasPrefix),
                        viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, attributePath, attribute.getFetches(), startIndex, correlationBasisType, correlationBasisEntity
                ));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }
    }

    private MappingConstructorImpl<Object[]> getSubviewMappingConstructor(ManagedViewTypeImplementor<Object[]> managedViewType) {
        // If there is none or only a single constructor, the selection will be done later
        if (managedViewType.getConstructors().size() <= 1) {
            return null;
        }

        // Otherwise use the default constructor named "init"
        return (MappingConstructorImpl<Object[]>) managedViewType.getConstructor("init");
    }

    private void applyBasicIdMapping(MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder) {
        mapperBuilder.addMapper(createMapper(mapperBuilder.getIdMapping(mappingAttribute, false), mapperBuilder.getAlias(mappingAttribute, false), mappingAttribute.getFetches()));
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder) {
        mapperBuilder.addMapper(createMapper(mapperBuilder.getMapping(mappingAttribute), mapperBuilder.getAlias(mappingAttribute, false), mappingAttribute.getFetches()));
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder) {
        mapperBuilder.addQueryParam(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, TupleElementMapperBuilder mapperBuilder) {
        @SuppressWarnings("unchecked")
        SubqueryProviderFactory factory = SubqueryProviderHelper.getFactory(attribute.getSubqueryProvider());
        String alias = mapperBuilder.getAlias(attribute, false);
        String subqueryAlias = attribute.getSubqueryAlias();
        String subqueryExpression = attribute.getSubqueryExpression();

        TupleElementMapper mapper;
        if (subqueryExpression.isEmpty()) {
            if (alias != null) {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedAliasSubqueryTupleElementMapper(factory, alias);
                } else {
                    mapper = new AliasSubqueryTupleElementMapper(factory.create(null, null), alias);
                }
            } else {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedSubqueryTupleElementMapper(factory);
                } else {
                    mapper = new SimpleSubqueryTupleElementMapper(factory.create(null, null));
                }
            }
        } else {
            if (alias != null) {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedAliasExpressionSubqueryTupleElementMapper(factory, subqueryExpression, subqueryAlias, alias);
                } else {
                    mapper = new AliasExpressionSubqueryTupleElementMapper(factory.create(null, null), subqueryExpression, subqueryAlias, alias);
                }
            } else {
                if (factory.isParameterized()) {
                    mapper = new ParameterizedExpressionSubqueryTupleElementMapper(factory, subqueryExpression, subqueryAlias);
                } else {
                    mapper = new ExpressionSubqueryTupleElementMapper(factory.create(null, null), subqueryExpression, subqueryAlias);
                }
            }
        }
        mapperBuilder.addMapper(mapper);
    }

    private void applyBasicCorrelatedMapping(AbstractAttribute<?, ?> attribute, String attributePath, TupleElementMapperBuilder mapperBuilder, int batchSize) {
        String correlationResult = attribute.getCorrelationResult();
        Class<? extends CorrelationProvider> correlationProvider = attribute.getCorrelationProvider();
        String correlationBasis = attribute.getCorrelationBasis();
        if (attribute.getFetchStrategy() == FetchStrategy.JOIN) {
            @SuppressWarnings("unchecked")
            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlationProvider);
            String alias = mapperBuilder.getAlias(attribute, false);
            correlationBasis = mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis));

            TupleElementMapper mapper;
            if (factory.isParameterized()) {
                mapper = new ParameterizedExpressionCorrelationJoinTupleElementMapper(factory, ef, mapperBuilder.getMapping(""), correlationBasis, correlationResult, alias, attributePath, attribute.getFetches());
            } else {
                mapper = new ExpressionCorrelationJoinTupleElementMapper(factory.create(null, null), ef, mapperBuilder.getMapping(""), correlationBasis, correlationResult, alias, attributePath, attribute.getFetches());
            }
            mapperBuilder.addMapper(mapper);
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(AbstractAttribute.stripThisFromMapping(correlationBasis));
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisType);
            String correlationKeyExpression = mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisEntity);

            mapperBuilder.addMapper(createMapper(correlationKeyExpression, subviewAliasPrefix, attribute.getFetches()));

            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlationProvider);

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
                        viewRoot, correlationResult, factory, attributePath, attribute.getFetches(), startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated()
                ));
            } else {
                // TODO: shouldn't we embed this query no matter what strategy is used?
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, correlationResult, factory, attributePath, attribute.getFetches(), startIndex, batchSize, correlationBasisType, correlationBasisEntity
                ));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(AbstractAttribute.stripThisFromMapping(correlationBasis));
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(AbstractAttribute.stripThisFromMapping(correlationBasis), correlationBasisType);
            String correlationKeyExpression = mapperBuilder.getMapping(AbstractAttribute.stripThisFromMapping(correlationBasis));

            mapperBuilder.addMapper(createMapper(correlationKeyExpression, subviewAliasPrefix, attribute.getFetches()));

            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlationProvider);

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
                        viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, attributePath, attribute.getFetches(), startIndex, correlationBasisType, correlationBasisEntity,
                        attribute.getCollectionInstantiator(),
                        !attribute.isCorrelated()
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, attributePath, attribute.getFetches(), startIndex, correlationBasisType, correlationBasisEntity
                ));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }
    }

    private Class<?> getCorrelationBasisType(String correlationBasis) {
        if (correlationBasis.isEmpty()) {
            return managedTypeClass;
        }
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedTypeClass, entityMetamodel, evm.getCriteriaBuilderFactory().getRegisteredFunctions());
        ef.createSimpleExpression(correlationBasis, false).accept(visitor);
        Collection<ScalarTargetResolvingExpressionVisitor.TargetType> possibleTypes = visitor.getPossibleTargets();
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

    private Class<?> getCorrelationBasisEntityType(String correlationBasis, Class<?> entityClazz) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ManagedType<?> managedType = entityMetamodel.getManagedType(entityClazz);
        if (managedType == null) {
            return null;
        }
        if (managedType instanceof IdentifiableType<?>) {
            return entityClazz;
        }

        throw new IllegalArgumentException("The correlation basis '" + correlationBasis + "' in the context of the managed type '" + managedTypeClass.getName() + "' resolved to the non-identifiable type '" + entityClazz.getName() + "'!");
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
                return "KEY(" + attributeName + ")";
            } else {
                return attributeName;
            }
        }

        if (isKey) {
            return "KEY(" + attributePath + "." + attributeName + ")";
        } else {
            return attributePath + "." + attributeName;
        }
    }

    public ObjectBuilder<T> createObjectBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return createObjectBuilder(queryBuilder, optionalParameters, entityViewConfiguration, false, false);
    }

    public ObjectBuilder<T> createObjectBuilder(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, boolean isSubview, boolean nullIfEmpty) {
        boolean hasOffset = tupleOffset != 0;
        ObjectBuilder<T> result;

        result = new ViewTypeObjectBuilder<T>(this, parameterHolder, optionalParameters, nullIfEmpty);

        if (hasSubtypes) {
            result = new InheritanceReducerViewTypeObjectBuilder<>(result, tupleOffset, mappers.length, !isSubview && tupleOffset > 0, subtypeInstantiators);
        } else if (hasOffset || isSubview || hasIndexedCollections || hasSubviews) {
            result = new ReducerViewTypeObjectBuilder<T>(result, tupleOffset, mappers.length, !isSubview && tupleOffset > 0);
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
        return viewClass;
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

    public boolean hasParameters() {
        return hasParameters;
    }

    public int getTupleOffset() {
        return tupleOffset;
    }

    public int getEffectiveTupleSize() {
        return effectiveTupleSize;
    }

    public static class Key {

        private final ExpressionFactory ef;
        private final ManagedViewTypeImpl<Object> viewType;
        private final MappingConstructorImpl<Object> constructor;
        private final String name;
        private final String entityViewRoot;
        private final int offset;

        public Key(ExpressionFactory ef, ManagedViewTypeImplementor<?> viewType, MappingConstructorImpl<?> constructor, String name, String entityViewRoot, int offset) {
            this.ef = ef;
            this.viewType = (ManagedViewTypeImpl<Object>) viewType;
            this.constructor = (MappingConstructorImpl<Object>) constructor;
            this.name = name;
            this.entityViewRoot = entityViewRoot;
            this.offset = offset;
        }

        public ViewTypeObjectBuilderTemplate<?> createValue(EntityViewManagerImpl evm, ProxyFactory proxyFactory) {
            int[] idPositions = new int[offset + 1];
            // If it has subtype, the first value is the type discriminator
            for (int i = 0; i <= offset; i++) {
                idPositions[i] = i + (viewType.hasSubtypes() ? 1 : 0);
            }
            return new ViewTypeObjectBuilderTemplate<Object>(viewType, entityViewRoot, "", name, entityViewRoot, entityViewRoot, idPositions, offset, null, evm, ef, viewType, constructor, proxyFactory);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.ef != null ? this.ef.hashCode() : 0);
            hash = 83 * hash + (this.viewType != null ? this.viewType.hashCode() : 0);
            hash = 83 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
            hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 83 * hash + (this.entityViewRoot != null ? this.entityViewRoot.hashCode() : 0);
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
            if (this.viewType != other.viewType && (this.viewType == null || !this.viewType.equals(other.viewType))) {
                return false;
            }
            if (this.constructor != other.constructor && (this.constructor == null || !this.constructor.equals(other.constructor))) {
                return false;
            }
            if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            if (this.entityViewRoot != other.entityViewRoot && (this.entityViewRoot == null || !this.entityViewRoot.equals(other.entityViewRoot))) {
                return false;
            }
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }
    }
}
