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
import com.blazebit.persistence.view.CTEProvider;
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
import com.blazebit.persistence.view.impl.objectbuilder.transformer.IndexedTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.NonIndexedTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SingularMultisetTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SubviewTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.BasicCorrelator;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedCollectionSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedMapBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedMapSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSubviewJoinTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.Correlator;
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
import com.blazebit.persistence.view.metamodel.ViewRoot;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewTypeObjectBuilderTemplate<T> {

    private static final String[] EMPTY = new String[0];


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
    private final SecondaryMapper[] secondaryMappers;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ViewTypeObjectBuilderTemplate(ManagedViewTypeImplementor<?> viewRoot, String viewRootAlias, String attributePath, String aliasPrefix, String mappingPrefix, String idPrefix, TupleIdDescriptor tupleIdDescriptor, TupleIdDescriptor viewIdDescriptor, int tupleOffset, int endTupleElementsToAdd, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
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
        List<SecondaryMapper> secondaryMappers = new ArrayList<>(viewRoot.getEntityViewRootTypes().size() + viewRoot.getCteProviders().size());
        Set<Feature> features = EnumSet.noneOf(Feature.class);

        final TupleTransformatorFactory tupleTransformatorFactory = new TupleTransformatorFactory();
        final EntityMetamodel metamodel = evm.getMetamodel().getEntityMetamodel();
        TupleElementMapperBuilder mainMapperBuilder = new TupleElementMapperBuilder(0, null, null, aliasPrefix, mappingPrefix, idPrefix, null, metamodel, ef, mappingList, parameterMappingList, secondaryMappers, tupleTransformatorFactory, viewRoot.getEntityViewRootTypes());

        for (ViewRoot entityViewRoot : managedViewType.getEntityViewRoots()) {
            mainMapperBuilder.addSecondaryMapper(createEntityViewRoot(mainMapperBuilder, entityViewRoot, attributePath, mainMapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath()));
        }
        for (CTEProvider cteProvider : managedViewType.getCteProviders()) {
            mainMapperBuilder.addSecondaryMapper(new CteProviderSecondaryMapper(attributePath, cteProvider));
        }

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
                features.add(Feature.SUBVIEWS);
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
                    TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(mappingList.size(), constraint, entry.getSubtypeIndex(), aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, viewRoot.getEntityViewRootTypes());
                    applyMapping(attribute, attributePath, mapperBuilder, features, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
                    builders.add(new ConstrainedTupleElementMapper.ConstrainedTupleElementMapperBuilder(constraint, entry.getSubtypeIndexes(), mapperBuilder));
                }
                ConstrainedTupleElementMapper.addMappers(classMappingIndex, mappingList, parameterMappingList, tupleTransformatorFactory, builders);
            } else {
                AbstractMethodAttribute<? super T, ?> attribute = constrainedAttribute.getSubAttribute(managedViewType);

                EntityType<?> treatType = getTreatType(metamodel, managedViewType, attribute.getDeclaringType());
                TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, null, aliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, secondaryMappers, tupleTransformatorFactory, viewRoot.getEntityViewRootTypes());
                applyMapping(attribute, attributePath, mapperBuilder, features, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
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
                TupleElementMapperBuilder mapperBuilder = new TupleElementMapperBuilder(0, null, null, paramAliasPrefix, mappingPrefix, idPrefix, treatType, metamodel, ef, mappingList, parameterMappingList, secondaryMappers, tupleTransformatorFactory, viewRoot.getEntityViewRootTypes());
                applyMapping((AbstractAttribute<?, ?>) parameterAttribute, attributePath, mapperBuilder, features, tupleIdDescriptor, viewJpqlMacro, embeddingViewJpqlMacro, ef);
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

        for (int i = 0; i < endTupleElementsToAdd; i++) {
            tupleIdDescriptor.addIdPosition(tupleOffset + mainMapperBuilder.mapperIndex() + i);
            viewIdDescriptor.addIdPosition(tupleOffset + mainMapperBuilder.mapperIndex() + i);
        }

        this.idPositions = viewIdDescriptor.createIdPositions();
        this.hasParameters = features.contains(Feature.PARAMETERS);
        this.hasIndexedCollections = features.contains(Feature.INDEXED_COLLECTIONS);
        this.hasSubviews = features.contains(Feature.SUBVIEWS);
        this.hasSubqueryCorrelation = features.contains(Feature.SUBQUERY_CORRELATION);
        this.subtypeInstantiators = subtypeInstantiators.toArray(new ObjectInstantiator[subtypeInstantiators.size()]);
        this.effectiveTupleSize = attributeCount;
        this.mappers = mappingList.toArray(new TupleElementMapper[mappingList.size()]);
        this.parameterMapper = new TupleParameterMapper(parameterMappingList, tupleOffset);
        this.secondaryMappers = secondaryMappers.toArray(new SecondaryMapper[secondaryMappers.size()]);
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
    private void applyMapping(AbstractAttribute<?, ?> attribute, String parentAttributePath, TupleElementMapperBuilder mapperBuilder, Set<Feature> features, TupleIdDescriptor tupleIdDescriptor, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, ExpressionFactory ef) {
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
                int valueStartIndex = startIndex + 1;

                if (pluralAttribute.getFetchStrategy() == FetchStrategy.JOIN) {
                    if (listKey) {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        features.add(Feature.INDEXED_COLLECTIONS);
                        applyIndexCollectionFunctionMapping(IntegerBasicUserType.INSTANCE, mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                    } else if (mapKey) {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        features.add(Feature.INDEXED_COLLECTIONS);
                        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) pluralAttribute;
                        keyConverter = (TypeConverter<Object, Object>) mapAttribute.getKeyType().getConverter();
                        if (mapAttribute.isKeySubview()) {
                            features.add(Feature.SUBVIEWS);
                            ManagedViewTypeImpl<Object[]> managedViewType = (ManagedViewTypeImpl<Object[]>) mapAttribute.getKeyType();
                            applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, true, true);
                            valueStartIndex = tupleOffset + mapperBuilder.mapperIndex();
                        } else {
                            applyIndexCollectionFunctionMapping(TypeUtils.forType(mapAttribute.getKeyType()), mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                        }
                    }
                }

                boolean dirtyTracking = pluralAttribute instanceof MethodAttribute<?, ?> && attribute.needsDirtyTracker();
                if (pluralAttribute.isSubview()) {
                    features.add(Feature.SUBVIEWS);

                    TupleIdDescriptor newTupleIdDescriptor;

                    if ((listKey || mapKey) && pluralAttribute.getFetchStrategy() == FetchStrategy.JOIN && !pluralAttribute.isCorrelated()) {
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
                            ViewTypeObjectBuilderTemplate<Object[]>[] templates = applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, features, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false);
                            ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = templates[0];
                            ViewTypeObjectBuilderTemplate<Object[]> indexTemplate = templates[1];
                            SubviewTupleTransformerFactory indexTransformerFactory = null;
                            BasicUserTypeStringSupport<?> indexBasicTypeSupport = null;
                            if (indexTemplate != null) {
                                boolean updatableKeyObjectCache = indexTemplate.viewType.isUpdatable() || indexTemplate.viewType.isCreatable();
                                indexTransformerFactory = new SubviewTupleTransformerFactory(attributePath, indexTemplate, updatableKeyObjectCache, true);
                            } else if (mapKey) {
                                indexBasicTypeSupport = TypeUtils.forType(((MapAttribute<?, ?, ?>) attribute).getKeyType());
                            } else if (listKey) {
                                indexBasicTypeSupport = IntegerBasicUserType.INSTANCE;
                            }
                            mapperBuilder.setTupleListTransformerFactory(new CollectionMultisetTupleListTransformerFactory(startIndex, null, attributePath, getMultisetResultAlias(attributePath), valueConverter, attribute.getContainerAccumulator(), dirtyTracking,
                                    subviewTemplate, indexTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(attributePath, subviewTemplate, updatableObjectCache, nullIfEmpty), indexTransformerFactory, null, indexBasicTypeSupport));
                        } else {
                            applyCorrelatedSubviewMapping(attribute, attributePath, newTupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, features, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, dirtyTracking);
                        }
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) pluralAttribute.getElementType();
                        boolean nullIfEmpty = managedViewType instanceof ViewType<?> || !listKey && !mapKey;
                        if (pluralAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                            boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                            String mapping = mapperBuilder.getMapping(mappingAttribute);
                            ViewTypeObjectBuilderTemplate<Object[]>[] templates = applySubviewMapping(mappingAttribute, attributePath, newTupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                            ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = templates[0];
                            ViewTypeObjectBuilderTemplate<Object[]> indexTemplate = templates[1];
                            SubviewTupleTransformerFactory indexTransformerFactory = null;
                            BasicUserTypeStringSupport<?> indexBasicTypeSupport = null;
                            if (indexTemplate != null) {
                                boolean updatableKeyObjectCache = indexTemplate.viewType.isUpdatable() || indexTemplate.viewType.isCreatable();
                                indexTransformerFactory = new SubviewTupleTransformerFactory(attributePath, indexTemplate, updatableKeyObjectCache, true);
                            } else if (mapKey) {
                                indexBasicTypeSupport = TypeUtils.forType(((MapAttribute<?, ?, ?>) attribute).getKeyType());
                            } else if (listKey) {
                                indexBasicTypeSupport = IntegerBasicUserType.INSTANCE;
                            }
                            mapperBuilder.setTupleListTransformerFactory(new CollectionMultisetTupleListTransformerFactory(startIndex, mapping, attributePath, getMultisetResultAlias(attributePath), valueConverter, attribute.getContainerAccumulator(), dirtyTracking,
                                    subviewTemplate, indexTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(attributePath, subviewTemplate, updatableObjectCache, nullIfEmpty), indexTransformerFactory, null, indexBasicTypeSupport));
                        } else {
                            // Obviously, we produce null if the object type is identifiable i.e. a ViewType and it is empty = null id
                            // Additionally, we also consider empty embeddables as null when we have a non-indexed collection so we can filter out these elements
                            applySubviewMapping(mappingAttribute, attributePath, newTupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                        }
                    }
                } else if (mapKey) {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyCollectionFunctionMapping(TypeUtils.forType(getType(mappingAttribute)), mappingAttribute, attributePath, mapperBuilder, mappingAttribute.getFetches(), embeddingViewJpqlMacro);
                } else {
                    // TODO: Multiset basic fetching?
                    if (pluralAttribute.isCorrelated() || attribute.getFetchStrategy() == FetchStrategy.JOIN && !attribute.getOrderByItems().isEmpty() || pluralAttribute.getFetchStrategy() != FetchStrategy.JOIN && pluralAttribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                        applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, features, ef, batchSize, dirtyTracking, embeddingViewJpqlMacro);
                    } else {
                        MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                        applyBasicMapping(mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                    }
                }

                if (pluralAttribute.getFetchStrategy() == FetchStrategy.JOIN) {
                    if (listKey) {
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            mapperBuilder.setTupleListTransformer(new IndexedTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, valueStartIndex, attribute.getContainerAccumulator(), dirtyTracking, null, valueConverter));
                        }
                    } else if (mapKey) {
                        mapperBuilder.setTupleListTransformer(new IndexedTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, valueStartIndex, attribute.getContainerAccumulator(), dirtyTracking, keyConverter, valueConverter));
                    } else {
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
                        mapperBuilder.setTupleListTransformer(new NonIndexedTupleListTransformer(tupleIdDescriptor.createIdPositions(), startIndex, attribute.getCollectionInstantiator(), dirtyTracking, valueConverter));
                    }
                }
            } else if (attribute.isQueryParameter()) {
                MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                features.add(Feature.PARAMETERS);
                applyQueryParameterMapping(mappingAttribute, mapperBuilder);
            } else if (attribute.isSubview()) {
                features.add(Feature.SUBVIEWS);
                boolean nullIfEmpty = !((SingularAttribute<?, ?>) attribute).isCreateEmptyFlatView();
                if (attribute.isCorrelated() || attribute.getFetchStrategy() == FetchStrategy.JOIN && !attribute.getOrderByItems().isEmpty() || attribute.getFetchStrategy() != FetchStrategy.JOIN && attribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                    ManagedViewTypeImplementor<Object> managedViewType = (ManagedViewTypeImplementor<Object>) ((SingularAttribute<?, ?>) attribute).getType();
                    if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                        int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                        boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                        ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, features, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false)[0];
                        TypeConverter<Object, Object> elementConverter = (TypeConverter<Object, Object>) (TypeConverter<?, ?>) managedViewType.getConverter();
                        mapperBuilder.setTupleListTransformerFactory(new SingularMultisetTupleListTransformerFactory(startIndex, null, attributePath, getMultisetResultAlias(attributePath), elementConverter, subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(attributePath, subviewTemplate, updatableObjectCache, nullIfEmpty)));
                    } else {
                        applyCorrelatedSubviewMapping(attribute, attributePath, tupleIdDescriptor, (ManagedViewTypeImplementor<Object[]>) (ManagedViewTypeImplementor<?>) managedViewType, mapperBuilder, features, viewJpqlMacro, embeddingViewJpqlMacro, ef, batchSize, false);
                    }
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    ManagedViewTypeImplementor<Object[]> managedViewType = (ManagedViewTypeImplementor<Object[]>) ((SingularAttribute<?, ?>) attribute).getType();
                    if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                        int startIndex = tupleOffset + mapperBuilder.mapperIndex();
                        boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
                        String mapping = mapperBuilder.getMapping(mappingAttribute);
                        ViewTypeObjectBuilderTemplate<Object[]> subviewTemplate = applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty)[0];
                        TypeConverter<Object, Object> elementConverter = (TypeConverter<Object, Object>) (TypeConverter<?, ?>) managedViewType.getConverter();
                        mapperBuilder.setTupleListTransformerFactory(new SingularMultisetTupleListTransformerFactory(startIndex, mapping, attributePath, getMultisetResultAlias(attributePath), elementConverter, subviewTemplate, managedViewType.hasSelectOrSubselectFetchedAttributes(), new SubviewTupleTransformerFactory(attributePath, subviewTemplate, updatableObjectCache, nullIfEmpty)));
                    } else {
                        applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, false, nullIfEmpty);
                    }
                }
            } else {
                if (attribute.isCorrelated() || attribute.getFetchStrategy() == FetchStrategy.JOIN && !attribute.getOrderByItems().isEmpty() || attribute.getFetchStrategy() != FetchStrategy.JOIN && attribute.getFetchStrategy() != FetchStrategy.MULTISET) {
                    applyBasicCorrelatedMapping(attribute, attributePath, mapperBuilder, features, ef, batchSize, false, embeddingViewJpqlMacro);
                } else {
                    MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
                    applyBasicMapping(mappingAttribute, attributePath, mapperBuilder, embeddingViewJpqlMacro);
                }
            }
        }
    }

    private void applyBasicIdMapping(MappingAttribute<? super T, ?> mappingAttribute, String parentAttributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String attributePath = getAttributePath(parentAttributePath, mappingAttribute, false);
        mapperBuilder.addMapper(createMapper(getType(mappingAttribute), mapperBuilder.getIdMapping(mappingAttribute), mapperBuilder.getAlias(mappingAttribute, false), attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), mappingAttribute.getFetches()));
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        mapperBuilder.addMapper(createMapper(getType(mappingAttribute), mapperBuilder.getMapping(mappingAttribute), mapperBuilder.getAlias(mappingAttribute, false), attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), mappingAttribute.getFetches()));
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, TupleElementMapperBuilder mapperBuilder) {
        mapperBuilder.addQueryParam(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, String attributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
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

    private void applyBasicCorrelatedMapping(AbstractAttribute<?, ?> attribute, String attributePath, TupleElementMapperBuilder mapperBuilder, Set<Feature> features, ExpressionFactory ef, int batchSize, boolean dirtyTracking, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
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
                mapper = new ParameterizedExpressionCorrelationJoinTupleElementMapper(factory, ef, joinBase, correlationBasis, attribute.getCorrelationResultExpression(), alias, joinCorrelationAttributePath, embeddingViewPath, attribute.getFetches(), createLimiter(mapperBuilder, correlationAlias, attribute), viewRoot.getEntityViewRootTypes().keySet());
            } else {
                mapper = new ExpressionCorrelationJoinTupleElementMapper(factory.create(null, null), ef, joinBase, correlationBasis, attribute.getCorrelationResultExpression(), alias, joinCorrelationAttributePath, embeddingViewPath, attribute.getFetches(), createLimiter(mapperBuilder, correlationAlias, attribute), viewRoot.getEntityViewRootTypes().keySet());
            }
            mapperBuilder.addMapper(mapper);
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), attribute.getDeclaringType().getEntityViewRootTypes());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = AbstractAttribute.stripThisFromMapping(correlationBasis);
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            String embeddingViewPath = mapperBuilder.getMapping();
            boolean correlatesThis = correlatesThis(evm, ef, managedTypeClass, attribute.getCorrelated(), correlationBasisExpression, attribute.getCorrelationPredicate(), attribute.getCorrelationKeyAlias());
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            // We need a special mapping for the VIEW_ROOT/EMBEDDING_VIEW macro in certain cases
            viewRootIndex = addViewRootMappingIfNeeded(mapperBuilder, features, subviewAliasPrefix, attributePath, viewRootIndex);
            embeddingViewIndex = addEmbeddingViewMappingIfNeeded(mapperBuilder, features, subviewAliasPrefix, attributePath, embeddingViewIndex);

            if (batchSize == -1) {
                batchSize = 1;
            }

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                String[] indexFetches = EMPTY;
                Expression indexExpression = null;
                Correlator indexCorrelator = null;
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
                        indexExpression = attribute.getMappingIndexExpression();
                        indexCorrelator = indexExpression == null ? null : new BasicCorrelator();
                        break;
                    case SET:
                        break;
                    case MAP:
                        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                        indexExpression = attribute.getKeyMappingExpression();
                        indexFetches = mapAttribute.getKeyFetches();
                        if (mapAttribute.isKeySubview()) {
                            indexCorrelator = new SubviewCorrelator((ManagedViewTypeImplementor<?>) mapAttribute.getKeyType(), null, evm, subviewAliasPrefix, attributePath);
                        } else {
                            indexCorrelator = new BasicCorrelator();
                        }
                        mapperBuilder.setTupleListTransformerFactory(new CorrelatedMapBatchTupleListTransformerFactory(
                                new BasicCorrelator(),
                                viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                                createLimiter(mapperBuilder, correlationAlias, attribute),
                                indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                                dirtyTracking
                        ));
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute),
                        indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute)));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), attribute.getDeclaringType().getEntityViewRootTypes());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            String embeddingViewPath = mapperBuilder.getMapping();
            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                String[] indexFetches = EMPTY;
                Expression indexExpression = null;
                Correlator indexCorrelator = null;
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
                        indexExpression = attribute.getMappingIndexExpression();
                        indexCorrelator = indexExpression == null ? null : new BasicCorrelator();
                        break;
                    case SET:
                        break;
                    case MAP:
                        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                        indexExpression = attribute.getKeyMappingExpression();
                        indexFetches = mapAttribute.getKeyFetches();
                        if (mapAttribute.isKeySubview()) {
                            indexCorrelator = new SubviewCorrelator((ManagedViewTypeImplementor<?>) mapAttribute.getKeyType(), null, evm, subviewAliasPrefix, attributePath);
                        } else {
                            indexCorrelator = new BasicCorrelator();
                        }
                        mapperBuilder.setTupleListTransformerFactory(new CorrelatedMapSubselectTupleListTransformerFactory(
                                new BasicCorrelator(),
                                evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                                createLimiter(mapperBuilder, correlationAlias, attribute),
                                indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                                dirtyTracking
                        ));
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute),
                        indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute)));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }
    }

    private void applyIndexCollectionFunctionMapping(BasicUserTypeStringSupport<?> basicUserTypeStringSupport, MappingAttribute<? super T, ?> mappingAttribute, String attributePath, TupleElementMapperBuilder mapperBuilder, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String expression;
        String[] fetches = EMPTY;
        if (mappingAttribute instanceof MapAttribute<?, ?, ?>) {
            expression = mapperBuilder.getKeyMapping((MapAttribute<?, ?, ?>) mappingAttribute);
            fetches = ((MapAttribute<?, ?, ?>) mappingAttribute).getKeyFetches();
        } else {
            expression = mapperBuilder.getIndexMapping((ListAttribute<?, ?>) mappingAttribute);
        }
        String alias = mapperBuilder.getAlias(mappingAttribute, true);
        TupleElementMapper mapper = createMapper(basicUserTypeStringSupport, expression, alias, attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), fetches);
        mapperBuilder.addMapper(mapper);
    }

    private void applyCollectionFunctionMapping(BasicUserTypeStringSupport<?> basicUserTypeStringSupport, MappingAttribute<? super T, ?> mappingAttribute, String attributePath, TupleElementMapperBuilder mapperBuilder, String[] fetches, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        String expression = "VALUE(" + mapperBuilder.getMapping(mappingAttribute) + ")";
        String alias = mapperBuilder.getAlias(mappingAttribute, false);
        TupleElementMapper mapper = createMapper(basicUserTypeStringSupport, expression, alias, attributePath, mapperBuilder.getMapping(), embeddingViewJpqlMacro.getEmbeddingViewPath(), fetches);
        mapperBuilder.addMapper(mapper);
    }

    private void applySubviewIdMapping(MappingAttribute<? super T, ?> mappingAttribute, String parentAttributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, ExpressionFactory ef, boolean isKey) {
        String attributePath = getAttributePath(parentAttributePath, mappingAttribute, false);
        applySubviewMapping(mappingAttribute, attributePath, tupleIdDescriptor, managedViewType, mapperBuilder, viewJpqlMacro, embeddingViewJpqlMacro, ef, isKey, true);
    }

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate<Object[]>[] applySubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String subviewAttributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                                                                        ExpressionFactory ef, boolean isKey, boolean nullIfEmpty) {
        AbstractAttribute<?, ?> attribute = (AbstractAttribute<?, ?>) mappingAttribute;
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
            if (isKey) {
                subviewMappingPrefix = mapperBuilder.getKeyMapping((MapAttribute<?, ?, ?>) mappingAttribute);
            } else {
                subviewMappingPrefix = mapperBuilder.getMapping(mappingAttribute);
            }
            subviewIdPrefix = subviewMappingPrefix;
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
            if (attribute.isUpdateMappable()) {
                subviewIdDescriptor = new TupleIdDescriptor();
            } else {
                subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
            }
        } else {
            subviewIdDescriptor = new TupleIdDescriptor(tupleIdDescriptor);
            subviewIdDescriptor.addIdPosition(flatViewIdPosition(mappingAttribute));
            subviewTupleIdDescriptor.addIdPosition(flatViewIdPosition(mappingAttribute));
        }

        int endTupleElementsToAdd = 0;
        String indexExpression = null;
        ViewTypeObjectBuilderTemplate<Object[]> indexTemplate = null;
        if (mappingAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            if (attribute.getKeyMappingExpression() != null) {
                MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                indexExpression = mapperBuilder.getKeyMapping(subviewMappingPrefix, mapAttribute);
                if (mapAttribute.isKeySubview()) {
                    indexTemplate = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, indexExpression, indexExpression, subviewTupleIdDescriptor, subviewIdDescriptor,
                            1, 0, viewJpqlMacro, embeddingViewJpqlMacro, (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) mapAttribute.getKeyInheritanceSubtypeMappings(), evm, ef, (ManagedViewTypeImplementor<Object[]>) mapAttribute.getKeyType(), null, proxyFactory);
                }
            } else if (attribute.getMappingIndexExpression() != null) {
                indexExpression = mapperBuilder.getIndexMapping(subviewMappingPrefix, (ListAttribute<?, ?>) attribute);
            }
            if (updatableObjectCache && managedViewType.getMappingType() == Type.MappingType.FLAT_VIEW) {
                if (indexExpression != null) {
                    endTupleElementsToAdd = 1;
                } else if (indexTemplate != null) {
                    endTupleElementsToAdd = indexTemplate.effectiveTupleSize;
                }
            }
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
                startIndex, endTupleElementsToAdd, viewJpqlMacro, embeddingViewJpqlMacro, inheritanceSubtypeMappings, evm, ef, managedViewType, null, proxyFactory);
        ViewTypeObjectBuilderTemplate<Object[]>[] templates = null;
        if (mappingAttribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            String multisetResultAlias = getMultisetResultAlias(subviewAttributePath);
            mapperBuilder.addMapper(new MultisetTupleElementMapper(template, multisetCorrelationExpression, subviewAttributePath, multisetResultAlias, embeddingViewPath, indexExpression, indexTemplate, createLimiter(mapperBuilder, multisetResultAlias, mappingAttribute)));
            templates = new ViewTypeObjectBuilderTemplate[]{ template, indexTemplate };
        } else {
            mapperBuilder.addMappers(template.mappers);
            mapperBuilder.addSecondaryMappers(template.secondaryMappers);
            mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
            mapperBuilder.addTupleTransformerFactory(new SubviewTupleTransformerFactory(subviewAttributePath, template, updatableObjectCache, nullIfEmpty));
        }
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        viewJpqlMacro.setViewPath(oldViewPath);
        return templates;
    }

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate<Object[]>[] applyCorrelatedSubviewMapping(AbstractAttribute<?, ?> attribute, String attributePath, TupleIdDescriptor tupleIdDescriptor, ManagedViewTypeImplementor<Object[]> managedViewType, TupleElementMapperBuilder mapperBuilder, Set<Feature> features, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                                                                                  ExpressionFactory ef, int batchSize, boolean dirtyTracking) {
        Expression correlationResult = attribute.getCorrelationResultExpression();
        String correlationBasis = attribute.getCorrelationBasis();
        CorrelationProviderFactory factory = attribute.getCorrelationProviderFactory();
        String correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);

        if (attribute.getFetchStrategy() == FetchStrategy.JOIN || attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
            @SuppressWarnings("unchecked")
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            correlationBasis = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            Limiter limiter = createLimiter(mapperBuilder, correlationAlias, attribute);
            String correlationExternalAlias;
            if (limiter == null) {
                correlationExternalAlias = correlationAlias;
            } else {
                correlationExternalAlias = CorrelationProviderHelper.getDefaultExternalCorrelationAlias(attributePath);
            }
            String subviewIdPrefix = correlationExternalAlias;
            if (!ExpressionUtils.isEmptyOrThis(correlationResult)) {
                subviewIdPrefix = PrefixingQueryGenerator.prefix(ef, correlationResult, correlationExternalAlias, viewRoot.getEntityViewRootTypes().keySet(), true);
            }
            String subviewMappingPrefix = subviewIdPrefix;

            int startIndex;
            if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                startIndex = 0;
            } else {
                startIndex = tupleOffset + mapperBuilder.mapperIndex();
            }
            boolean updatableObjectCache = managedViewType.isUpdatable() || managedViewType.isCreatable();
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
            int endTupleElementsToAdd = 0;
            String indexExpression = null;
            ViewTypeObjectBuilderTemplate<Object[]> indexTemplate = null;
            if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                if (attribute.getKeyMappingExpression() != null) {
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                    indexExpression = mapperBuilder.getKeyMapping(subviewMappingPrefix, mapAttribute);
                    if (mapAttribute.isKeySubview()) {
                        indexTemplate = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, attributePath, subviewAliasPrefix, indexExpression, indexExpression, subviewTupleIdDescriptor, subviewIdDescriptor,
                                1, 0, viewJpqlMacro, embeddingViewJpqlMacro, (Map<ManagedViewType<? extends Object[]>, String>) (Map<?, ?>) mapAttribute.getKeyInheritanceSubtypeMappings(), evm, ef, (ManagedViewTypeImplementor<Object[]>) mapAttribute.getKeyType(), null, proxyFactory);
                    }
                } else if (attribute.getMappingIndexExpression() != null) {
                    indexExpression = mapperBuilder.getIndexMapping(subviewMappingPrefix, (ListAttribute<?, ?>) attribute);
                }
                if (updatableObjectCache && managedViewType.getMappingType() == Type.MappingType.FLAT_VIEW) {
                    if (indexExpression != null) {
                        endTupleElementsToAdd = 1;
                    } else if (indexTemplate != null) {
                        endTupleElementsToAdd = indexTemplate.effectiveTupleSize;
                    }
                }
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
            ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, attributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewTupleIdDescriptor, subviewIdDescriptor,
                    startIndex, endTupleElementsToAdd, viewJpqlMacro, embeddingViewJpqlMacro, inheritanceSubtypeMappings, evm, ef, managedViewType, null, proxyFactory);
            if (attribute.getFetchStrategy() == FetchStrategy.MULTISET) {
                mapperBuilder.addMapper(new CorrelationMultisetTupleElementMapper(template, factory, correlationBasis, correlationExternalAlias, attributePath, mapperBuilder.getMapping(), indexExpression, indexTemplate, limiter));
            } else {
                mapperBuilder.addMappers(template.mappers);
                mapperBuilder.addSecondaryMappers(template.secondaryMappers);
                mapperBuilder.addTupleTransformatorFactory(template.tupleTransformatorFactory);
                mapperBuilder.addTupleTransformerFactory(new CorrelatedSubviewJoinTupleTransformerFactory(template, factory, correlationAlias, mapperBuilder.getMapping(), correlationBasis, correlationExternalAlias, attributePath, embeddingViewPath, attribute.getFetches(), limiter));
            }
            embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
            viewJpqlMacro.setViewPath(oldViewPath);
            return new ViewTypeObjectBuilderTemplate[]{ template, indexTemplate };
        } else if (attribute.getFetchStrategy() == FetchStrategy.SELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = (viewType.hasSubtypes() ? 1 : 0) + tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), attribute.getDeclaringType().getEntityViewRootTypes());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = AbstractAttribute.stripThisFromMapping(correlationBasis);
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            String embeddingViewPath = mapperBuilder.getMapping();
            boolean correlatesThis = correlatesThis(evm, ef, managedTypeClass, attribute.getCorrelated(), correlationBasisExpression, attribute.getCorrelationPredicate(), attribute.getCorrelationKeyAlias());
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            // We need a special mapping for the VIEW_ROOT/EMBEDDING_VIEW macro in certain cases
            viewRootIndex = addViewRootMappingIfNeeded(mapperBuilder, features, subviewAliasPrefix, attributePath, viewRootIndex);
            embeddingViewIndex = addEmbeddingViewMappingIfNeeded(mapperBuilder, features, subviewAliasPrefix, attributePath, embeddingViewIndex);

            if (batchSize == -1) {
                batchSize = 1;
            }

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                String[] indexFetches = EMPTY;
                Expression indexExpression = null;
                Correlator indexCorrelator = null;
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
                        indexExpression = attribute.getMappingIndexExpression();
                        indexCorrelator = indexExpression == null ? null : new BasicCorrelator();
                        break;
                    case SET:
                        break;
                    case MAP:
                        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                        indexExpression = attribute.getKeyMappingExpression();
                        indexFetches = mapAttribute.getKeyFetches();
                        if (mapAttribute.isKeySubview()) {
                            indexCorrelator = new SubviewCorrelator((ManagedViewTypeImplementor<?>) mapAttribute.getKeyType(), null, evm, subviewAliasPrefix, attributePath);
                        } else {
                            indexCorrelator = new BasicCorrelator();
                        }
                        mapperBuilder.setTupleListTransformerFactory(new CorrelatedMapBatchTupleListTransformerFactory(
                                new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                                viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                                createLimiter(mapperBuilder, correlationAlias, attribute),
                                indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                                dirtyTracking
                        ));
                        return null;
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute),
                        indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        viewRoot, viewType, correlationResult, factory, attributePath, attribute.getFetches(), correlatesThis, viewRootIndex, embeddingViewIndex, startIndex, batchSize, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute)));
            }
        } else if (attribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = mapperBuilder.getAlias(attribute, false);
            int viewRootIndex = viewRoot.hasSubtypes() ? 1 : 0;
            int embeddingViewIndex = (viewType.hasSubtypes() ? 1 : 0) + tupleOffset;
            int startIndex = tupleOffset + mapperBuilder.mapperIndex();
            Class<?> correlationBasisType = getCorrelationBasisType(attribute.getCorrelationBasisExpression(), AbstractAttribute.stripThisFromMapping(correlationBasis), attribute.getDeclaringType().getEntityViewRootTypes());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlationBasisType);
            String correlationBasisExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression());
            String correlationKeyExpression = mapperBuilder.getMapping(attribute.getCorrelationBasisExpression(), correlationBasisEntity);
            BasicUserTypeStringSupport<Object> correlationKeyExpressionBasicTypeType = getCorrelationKeyExpressionBasicTypeSupport(correlationBasisType, correlationBasisEntity);

            String embeddingViewPath = mapperBuilder.getMapping();
            mapperBuilder.addMapper(createMapper(correlationKeyExpressionBasicTypeType, correlationKeyExpression, subviewAliasPrefix, attributePath, embeddingViewPath, embeddingViewJpqlMacro.getEmbeddingViewPath(), attribute.getFetches()));

            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                String[] indexFetches = EMPTY;
                Expression indexExpression = null;
                Correlator indexCorrelator = null;
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
                        indexExpression = attribute.getMappingIndexExpression();
                        indexCorrelator = indexExpression == null ? null : new BasicCorrelator();
                        break;
                    case SET:
                        break;
                    case MAP:
                        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                        indexExpression = attribute.getKeyMappingExpression();
                        indexFetches = mapAttribute.getKeyFetches();
                        if (mapAttribute.isKeySubview()) {
                            indexCorrelator = new SubviewCorrelator((ManagedViewTypeImplementor<?>) mapAttribute.getKeyType(), null, evm, subviewAliasPrefix, attributePath);
                        } else {
                            indexCorrelator = new BasicCorrelator();
                        }
                        mapperBuilder.setTupleListTransformerFactory(new CorrelatedMapSubselectTupleListTransformerFactory(
                                new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                                evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                                createLimiter(mapperBuilder, correlationAlias, attribute),
                                indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                                dirtyTracking
                        ));
                        return null;
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }

                mapperBuilder.setTupleListTransformerFactory(new CorrelatedCollectionSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute),
                        indexFetches, indexExpression, indexCorrelator, attribute.getContainerAccumulator(),
                        dirtyTracking
                ));
            } else {
                mapperBuilder.setTupleListTransformerFactory(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, null, evm, subviewAliasPrefix, attributePath),
                        evm, viewRoot, viewRootAlias, viewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, factory, attributePath, attribute.getFetches(), viewRootIndex, embeddingViewIndex, startIndex, correlationBasisType, correlationBasisEntity,
                        createLimiter(mapperBuilder, correlationAlias, attribute)));
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + attribute.getFetchStrategy());
        }

        return null;
    }

    private String getMultisetResultAlias(String attributePath) {
        return "multiset_" + attributePath.replace('.', '_');
    }

    private Limiter createLimiter(TupleElementMapperBuilder mapperBuilder, String prefix, Attribute<?, ?> attribute) {
        return createLimiter(mapperBuilder, prefix, attribute.getLimitExpression(), attribute.getOffsetExpression(), attribute.getOrderByItems());
    }

    private Limiter createLimiter(TupleElementMapperBuilder mapperBuilder, String prefix, String limitExpression, String offsetExpression, List<OrderByItem> orderByItems) {
        return viewRoot.createLimiter(mapperBuilder.getService(ExpressionFactory.class), prefix, limitExpression, offsetExpression, orderByItems);
    }

    private EntityViewRootSecondaryMapper createEntityViewRoot(TupleElementMapperBuilder mapperBuilder, ViewRoot entityViewRoot, String attributePath, String viewPath, String embeddingViewPath) {
        return new EntityViewRootSecondaryMapper(
                entityViewRoot.getName(),
                attributePath,
                viewPath,
                embeddingViewPath,
                entityViewRoot.getCorrelationProviderFactory(),
                entityViewRoot.getJoinType(),
                entityViewRoot.getFetches(),
                createLimiter(mapperBuilder, viewPath, entityViewRoot.getLimitExpression(), entityViewRoot.getOffsetExpression(), entityViewRoot.getOrderByItems())
        );
    }

    private int addViewRootMappingIfNeeded(TupleElementMapperBuilder mapperBuilder, Set<Feature> features, String subviewAliasPrefix, String attributePath, int viewRootIndex) {
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
                features.add(Feature.SUBQUERY_CORRELATION);
                viewRootIndex = tupleOffset + mapperBuilder.mapperIndex();
                javax.persistence.metamodel.SingularAttribute<?, ?> singleIdAttribute = JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) viewRoot.getJpaManagedType());
                mapperBuilder.addMapper(createMapper((BasicUserTypeStringSupport<?>) null, singleIdAttribute.getName(), subviewAliasPrefix + "_view_root_id", attributePath, null, null, EMPTY));
            }
        }
        return viewRootIndex;
    }

    private int addEmbeddingViewMappingIfNeeded(TupleElementMapperBuilder mapperBuilder, Set<Feature> features, String subviewAliasPrefix, String attributePath, int embeddingViewIndex) {
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

    private Class<?> getCorrelationBasisType(Expression correlationBasisExpression, String correlationBasis, Map<String, javax.persistence.metamodel.Type<?>> rootTypes) {
        if (correlationBasis.isEmpty()) {
            return managedTypeClass;
        }
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ScalarTargetResolvingExpressionVisitor visitor = new ScalarTargetResolvingExpressionVisitor(managedTypeClass, entityMetamodel, evm.getCriteriaBuilderFactory().getRegisteredFunctions(), rootTypes);
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
            if (tupleTransformatorFactory.hasListTransformers()) {
                result = new ChainingCollectionObjectBuilder<T>(tupleTransformatorFactory, result, parameterHolder, optionalParameters, entityViewConfiguration);
            } else {
                result = new ChainingObjectBuilder<T>(tupleTransformatorFactory, result, parameterHolder, optionalParameters, entityViewConfiguration);
            }
        }

        return result;
    }

    public Class<?> getViewClass() {
        return viewType.getJavaType();
    }

    public ManagedViewType<?> getViewRoot() {
        return viewRoot;
    }

    public SecondaryMapper[] getSecondaryMappers() {
        return secondaryMappers;
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
            return new ViewTypeObjectBuilderTemplate<Object>(viewType, entityViewRoot, "", viewType.getJavaType().getSimpleName(), entityViewRoot, entityViewRoot, new TupleIdDescriptor(), new TupleIdDescriptor(), offset, 0, viewJpqlMacro, embeddingViewJpqlMacro, null, evm, ef, viewType, constructor, proxyFactory);
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

    /**
     *
     * @author Christian Beikov
     * @since 1.6.0
     */
    private enum Feature {
        PARAMETERS,
        INDEXED_COLLECTIONS,
        SUBVIEWS,
        SUBQUERY_CORRELATION
    }
}
