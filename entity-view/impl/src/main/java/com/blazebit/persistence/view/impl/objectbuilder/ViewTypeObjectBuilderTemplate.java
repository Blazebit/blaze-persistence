/*
 * Copyright 2014 - 2016 Blazebit.
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
import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.PathTargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.SubqueryProviderFactory;
import com.blazebit.persistence.view.impl.SubqueryProviderHelper;
import com.blazebit.persistence.view.impl.metamodel.ClassUtils;
import com.blazebit.persistence.view.impl.metamodel.EntityMetamodel;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedAliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionCorrelationJoinTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ParameterizedSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.SubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleParameterMapper;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.IndexedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.MapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.OrderedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.OrderedMapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.OrderedSetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SortedMapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SortedSetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.SubviewTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableIndexedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableMapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableOrderedListTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableOrderedMapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableOrderedSetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableSetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableSortedMapTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.UpdatableSortedSetTupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.BasicCorrelator;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedListBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedListSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedOrderedSetBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedOrderedSetSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSetBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSetSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSingularSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSortedSetBatchTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSortedSetSubselectTupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.CorrelatedSubviewJoinTupleTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.SubviewCorrelator;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.ReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.UnsafeInstantiator;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeObjectBuilderTemplate<T> {

    private static final int FEATURE_PARAMETERS = 0;
    private static final int FEATURE_INDEXED_COLLECTIONS = 1;
    private static final int FEATURE_SUBVIEWS = 2;

    private final ObjectInstantiator<T> objectInstantiator;
    private final TupleElementMapper[] mappers;
    private final TupleParameterMapper parameterMapper;
    private final int effectiveTupleSize;
    private final boolean hasParameters;
    private final boolean hasIndexedCollections;
    private final boolean hasSubviews;

    private final ManagedViewType<?> viewRoot;
    private final String viewRootAlias;
    private final Class<?> managedTypeClass;
    private final String aliasPrefix;
    private final List<String> mappingPrefix;
    private final String idPrefix;
    private final int[] idPositions;
    private final int tupleOffset;
    private final EntityViewManagerImpl evm;
    private final ExpressionFactory ef;
    private final ProxyFactory proxyFactory;
    private final TupleTransformatorFactory tupleTransformatorFactory = new TupleTransformatorFactory();

    @SuppressWarnings("unchecked")
    private ViewTypeObjectBuilderTemplate(ManagedViewType<?> viewRoot, String viewRootAlias, String attributePath, String aliasPrefix, List<String> mappingPrefix, String idPrefix, int[] idPositions, int tupleOffset,
                                          EntityViewManagerImpl evm, ExpressionFactory ef, ManagedViewType<T> managedViewType, MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory) {
        if (mappingConstructor == null) {
            if (managedViewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + managedViewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (managedViewType.getConstructors().size() == 1) {
                mappingConstructor = (MappingConstructor<T>) managedViewType.getConstructors().toArray()[0];
            }
        }

        this.viewRoot = viewRoot;
        this.viewRootAlias = viewRootAlias;
        this.managedTypeClass = managedViewType.getEntityClass();
        this.aliasPrefix = aliasPrefix;
        this.mappingPrefix = mappingPrefix;
        this.idPrefix = idPrefix;
        this.idPositions = idPositions;
        this.tupleOffset = tupleOffset;
        this.evm = evm;
        this.ef = ef;
        this.proxyFactory = proxyFactory;

        Set<MethodAttribute<? super T, ?>> attributeSet = managedViewType.getAttributes();
        int attributeCount = attributeSet.size();
        
        javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttr = null;
        Class<?> idAttributeType = null;
        // We have special handling for the id attribute since we need to know it's position in advance
        // Therefore we have to remove it so that it doesn't get processed as normal attribute
        if (managedViewType instanceof ViewType<?>) {
            attributeSet.remove(((ViewType<?>) managedViewType).getIdAttribute());

            // First we add the id attribute
            ManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().managedType(managedViewType.getEntityClass());
            
            if (!(managedType instanceof IdentifiableType<?>)) {
                throw new IllegalArgumentException("The given managed type '" + managedViewType.getEntityClass().getName() + "' of the entity view type '" + managedViewType.getJavaType().getName() + "' is not an identifiable type!");
            }
            
            jpaIdAttr = getIdAttribute((IdentifiableType<?>) managedType);
            
            if (jpaIdAttr.getJavaMember() instanceof Field) {
                idAttributeType = ReflectionUtils.getResolvedFieldType(managedViewType.getEntityClass(), (Field) jpaIdAttr.getJavaMember());
            } else {
                idAttributeType = ReflectionUtils.getResolvedMethodReturnType(managedViewType.getEntityClass(), (Method) jpaIdAttr.getJavaMember());
            }
            
            if (idAttributeType == null) {
                throw new IllegalArgumentException("The id attribute type is not resolvable " + "for the attribute '" + jpaIdAttr.getName() + "' of the class '" + managedViewType.getEntityClass().getName() + "'!");
            }
        }
        
        MethodAttribute<?, ?>[] attributes = attributeSet.toArray(new MethodAttribute<?, ?>[attributeSet.size()]);
        ParameterAttribute<?, ?>[] parameterAttributes;

        if (mappingConstructor == null) {
            parameterAttributes = new ParameterAttribute<?, ?>[0];
        } else {
            List<ParameterAttribute<? super T, ?>> parameterAttributeList = mappingConstructor.getParameterAttributes();
            parameterAttributes = parameterAttributeList.toArray(new ParameterAttribute<?, ?>[parameterAttributeList.size()]);
        }

        attributeCount += parameterAttributes.length;

        List<TupleElementMapper> mappingList = new ArrayList<TupleElementMapper>(attributeCount);
        List<String> parameterMappingList = new ArrayList<String>(attributeCount);
        Class<?>[] parameterTypes = new Class<?>[attributeCount];
        boolean[] featuresFound = new boolean[3];
        int parameterOffset = 0;

        if (managedViewType instanceof ViewType<?>) {
            ViewType<?> viewType = (ViewType<?>) managedViewType;
            String idAttributeName = jpaIdAttr.getName();
            MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
            MappingAttribute<?, ?> idMappingAttribute = (MappingAttribute<?, ?>) idAttribute;

            if (!idAttributeName.equals(idMappingAttribute.getMapping())) {
                throw new IllegalArgumentException("Invalid id mapping '" + idMappingAttribute.getMapping() + "' for entity view '" + viewType.getJavaType().getName() + "'! Expected '" + idAttributeName + "'!");
            }

            String idMapping = idPrefix == null ? idAttributeName : idPrefix + "." + idAttributeName;

            parameterTypes[0] = idAttribute.getJavaType();
            mappingList.add(0, createMapper(idMapping, getAlias(aliasPrefix, idAttribute)));
            parameterMappingList.add(0, null);
            parameterOffset = 1;
        }

        for (int i = 0; i < attributes.length; i++) {
            parameterTypes[i + parameterOffset] = attributes[i].getJavaType();
        }
        for (int i = 0; i < parameterAttributes.length; i++) {
            parameterTypes[i + attributes.length + parameterOffset] = parameterAttributes[i].getJavaType();
        }

        if (managedViewType.getConstructors().isEmpty() || evm.isUnsafeDisabled()) {
            this.objectInstantiator = new ReflectionInstantiator<T>(mappingConstructor, proxyFactory, managedViewType, parameterTypes);
        } else {
            this.objectInstantiator = new UnsafeInstantiator<T>(mappingConstructor, proxyFactory, managedViewType, parameterTypes);
        }
        
        for (int i = 0; i < attributes.length; i++) {
            applyMapping(attributes[i], attributePath, mappingList, parameterMappingList, featuresFound);
        }
        for (int i = 0; i < parameterAttributes.length; i++) {
            applyMapping(parameterAttributes[i], attributePath, mappingList, parameterMappingList, featuresFound);
        }

        this.hasParameters = featuresFound[FEATURE_PARAMETERS];
        this.hasIndexedCollections = featuresFound[FEATURE_INDEXED_COLLECTIONS];
        this.hasSubviews = featuresFound[FEATURE_SUBVIEWS];
        this.effectiveTupleSize = attributeCount;
        this.mappers = mappingList.toArray(new TupleElementMapper[mappingList.size()]);
        this.parameterMapper = new TupleParameterMapper(parameterMappingList, tupleOffset);
    }

    // TODO: copied from JpaUtils
    private static javax.persistence.metamodel.SingularAttribute<?, ?> getIdAttribute(IdentifiableType<?> entityType) {
        Class<?> idClass = null;
        try {
            idClass = entityType.getIdType().getJavaType();
            return entityType.getId(idClass);
        } catch (IllegalArgumentException e) {
            /**
             * Eclipselink returns wrapper types from entityType.getIdType().getJavaType() even if the id type
             * is a primitive.
             * In this case, entityType.getId(...) throws an IllegalArgumentException. We catch it here and try again
             * with the corresponding primitive type.
             */
            if (idClass != null) {
                final Class<?> primitiveIdClass = ClassUtils.getPrimitiveClassOfWrapper(idClass);
                if (primitiveIdClass != null) {
                    return entityType.getId(primitiveIdClass);
                }
            }
            throw e;
        }
    }

    private TupleElementMapper createMapper(String expression) {
        return createMapper(expression, null);
    }

    private TupleElementMapper createMapper(String expression, String alias) {
        if (alias != null) {
            return new AliasExpressionTupleElementMapper(expression, alias);
        } else {
            return new ExpressionTupleElementMapper(expression);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyMapping(Attribute<?, ?> attribute, String attributePath, List<TupleElementMapper> mappingList, List<String> parameterMappingList, boolean[] featuresFound) {
        int batchSize = attribute.getBatchSize();

        if (batchSize == -1) {
            batchSize = attribute.getDeclaringType().getDefaultBatchSize();
        }

        if (attribute.isSubquery()) {
            applySubqueryMapping((SubqueryAttribute<? super T, ?>) attribute, mappingList, parameterMappingList);
        } else {
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
            if (mappingAttribute.isCollection()) {
                PluralAttribute<? super T, ?, ?> pluralAttribute = (PluralAttribute<? super T, ?, ?>) mappingAttribute;
                boolean listKey = pluralAttribute.isIndexed() && pluralAttribute instanceof ListAttribute<?, ?>;
                boolean mapKey = pluralAttribute.isIndexed() && pluralAttribute instanceof MapAttribute<?, ?, ?>;
                int startIndex = tupleOffset + mappingList.size();

                if (listKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping("INDEX", "_KEY", pluralAttribute, mappingList, parameterMappingList);
                } else if (mapKey) {
                    if (pluralAttribute.isCorrelated()) {
                        throw new IllegalArgumentException("Correlated mappings can't be indexed!");
                    }
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping("KEY", "_KEY", pluralAttribute, mappingList, parameterMappingList);
                }

                if (pluralAttribute.isSubview()) {
                    featuresFound[FEATURE_SUBVIEWS] = true;

                    int[] newIdPositions;

                    if (listKey || mapKey) {
                        newIdPositions = new int[idPositions.length + 1];
                        System.arraycopy(idPositions, 0, newIdPositions, 0, idPositions.length);
                        newIdPositions[idPositions.length] = mappingList.size();
                    } else {
                        newIdPositions = idPositions;
                    }

                    if (pluralAttribute.isCorrelated()) {
                        applyCorrelatedSubviewMapping(pluralAttribute, attributePath, newIdPositions, pluralAttribute.getElementType(), mappingList, parameterMappingList, batchSize);
                    } else {
                        applySubviewMapping(pluralAttribute, attributePath, newIdPositions, pluralAttribute.getElementType(), mappingList, parameterMappingList, batchSize);
                    }
                } else if (mapKey) {
                    applyCollectionFunctionMapping("VALUE", "", pluralAttribute, mappingList, parameterMappingList);
                } else {
                    if (pluralAttribute.isCorrelated()) {
                        applyBasicCorrelatedMapping((CorrelatedAttribute<? super T, ?>) attribute, attributePath, mappingList, parameterMappingList, batchSize);
                    } else {
                        applyBasicMapping(pluralAttribute, mappingList, parameterMappingList, batchSize);
                    }
                }

                if (listKey) {
                    if (pluralAttribute.isSorted()) {
                        throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                    } else {
                        if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                            tupleTransformatorFactory.add(new UpdatableIndexedListTupleListTransformer(idPositions, startIndex));
                        } else {
                            tupleTransformatorFactory.add(new IndexedListTupleListTransformer(idPositions, startIndex));
                        }
                    }
                } else if (mapKey) {
                    if (pluralAttribute.isSorted()) {
                        if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                            tupleTransformatorFactory.add(new UpdatableSortedMapTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                        } else {
                            tupleTransformatorFactory.add(new SortedMapTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                        }
                    } else if (pluralAttribute.isOrdered()) {
                        if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                            tupleTransformatorFactory.add(new UpdatableOrderedMapTupleListTransformer(idPositions, startIndex));
                        } else {
                            tupleTransformatorFactory.add(new OrderedMapTupleListTransformer(idPositions, startIndex));
                        }
                    } else {
                        if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                            tupleTransformatorFactory.add(new UpdatableMapTupleListTransformer(idPositions, startIndex));
                        } else {
                            tupleTransformatorFactory.add(new MapTupleListTransformer(idPositions, startIndex));
                        }
                    }
                } else if (!pluralAttribute.isCorrelated() || ((CorrelatedAttribute<?, ?>) pluralAttribute).getFetchStrategy() == FetchStrategy.JOIN) {
                    switch (pluralAttribute.getCollectionType()) {
                        case COLLECTION:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                            } else {
                                if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                                    tupleTransformatorFactory.add(new UpdatableOrderedListTupleListTransformer(idPositions, startIndex));
                                } else {
                                    tupleTransformatorFactory.add(new OrderedListTupleListTransformer(idPositions, startIndex));
                                }
                            }
                            break;
                        case LIST:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                            } else {
                                if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                                    tupleTransformatorFactory.add(new UpdatableOrderedListTupleListTransformer(idPositions, startIndex));
                                } else {
                                    tupleTransformatorFactory.add(new OrderedListTupleListTransformer(idPositions, startIndex));
                                }
                            }
                            break;
                        case SET:
                            if (pluralAttribute.isSorted()) {
                                if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                                    tupleTransformatorFactory.add(new UpdatableSortedSetTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                                } else {
                                    tupleTransformatorFactory.add(new SortedSetTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                                }
                            } else if (pluralAttribute.isOrdered()) {
                                if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                                    tupleTransformatorFactory.add(new UpdatableOrderedSetTupleListTransformer(idPositions, startIndex));
                                } else {
                                    tupleTransformatorFactory.add(new OrderedSetTupleListTransformer(idPositions, startIndex));
                                }
                            } else {
                                if (pluralAttribute instanceof MethodAttribute<?, ?> && ((MethodAttribute<?, ?>) pluralAttribute).isUpdatable()) {
                                    tupleTransformatorFactory.add(new UpdatableSetTupleListTransformer(idPositions, startIndex));
                                } else {
                                    tupleTransformatorFactory.add(new SetTupleListTransformer(idPositions, startIndex));
                                }
                            }
                            break;
                        case MAP:
                            throw new IllegalArgumentException("Ignoring the index on the attribute '" + pluralAttribute + "' is not possible!");
                        default:
                            throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                    }
                }
            } else if (((SingularAttribute<?, ?>) mappingAttribute).isQueryParameter()) {
                featuresFound[FEATURE_PARAMETERS] = true;
                applyQueryParameterMapping(mappingAttribute, mappingList, parameterMappingList);
            } else if (mappingAttribute.isSubview()) {
                featuresFound[FEATURE_SUBVIEWS] = true;
                if (mappingAttribute.isCorrelated()) {
                    applyCorrelatedSubviewMapping(mappingAttribute, attributePath, idPositions, mappingAttribute.getJavaType(), mappingList, parameterMappingList, batchSize);
                } else {
                    applySubviewMapping(mappingAttribute, attributePath, idPositions, mappingAttribute.getJavaType(), mappingList, parameterMappingList, batchSize);
                }
            } else {
                if (mappingAttribute.isCorrelated()) {
                    applyBasicCorrelatedMapping((CorrelatedAttribute<? super T, ?>) attribute, attributePath, mappingList, parameterMappingList, batchSize);
                } else {
                    applyBasicMapping(mappingAttribute, mappingList, parameterMappingList, batchSize);
                }
            }
        }
    }

    private void applyCollectionFunctionMapping(String function, String aliasSuffix, MappingAttribute<? super T, ?> mappingAttribute, List<TupleElementMapper> mappingList, List<String> parameterMappingList) {
        String expression = function + "(" + getMapping(mappingPrefix, mappingAttribute) + ")";
        String alias = getAlias(aliasPrefix, mappingAttribute);
        TupleElementMapper mapper;
        if (alias == null) {
            mapper = createMapper(expression);
        } else {
            mapper = createMapper(expression, alias + aliasSuffix);
        }
        mappingList.add(mapper);
        parameterMappingList.add(null);
    }

    private void applySubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, int[] idPositions, Class<?> subviewClass, List<TupleElementMapper> mappingList, List<String> parameterMappingList, int batchSize) {
        @SuppressWarnings("unchecked")
        ManagedViewType<Object[]> managedViewType = (ManagedViewType<Object[]>) evm.getMetamodel().managedView(subviewClass);
        String subviewAttributePath = getAttributePath(attributePath, mappingAttribute);
        String subviewAliasPrefix = getAlias(aliasPrefix, mappingAttribute);
        List<String> subviewMappingPrefix = createSubviewMappingPrefix(mappingPrefix, mappingAttribute);
        String subviewIdPrefix = getMapping(idPrefix, mappingAttribute);
        int[] subviewIdPositions;
        int startIndex;

        if (managedViewType instanceof ViewType<?>) {
            subviewIdPositions = new int[idPositions.length + 1];
            System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
            subviewIdPositions[idPositions.length] = tupleOffset + mappingList.size();
            startIndex = tupleOffset + mappingList.size();
        } else {
            subviewIdPositions = idPositions;
            startIndex = tupleOffset + mappingList.size();
        }

        ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions,
                startIndex, evm, ef, managedViewType, null, proxyFactory);
        Collections.addAll(mappingList, template.mappers);
        // We do not copy because the subview object builder will populate the subview's parameters
        for (int i = 0; i < template.mappers.length; i++) {
            parameterMappingList.add(null);
        }
        tupleTransformatorFactory.add(template.tupleTransformatorFactory);
        tupleTransformatorFactory.add(new SubviewTupleTransformerFactory(template));
    }

    private void applyCorrelatedSubviewMapping(MappingAttribute<? super T, ?> mappingAttribute, String attributePath, int[] idPositions, Class<?> subviewClass, List<TupleElementMapper> mappingList, List<String> parameterMappingList, int batchSize) {
        String subviewAttributePath = getAttributePath(attributePath, mappingAttribute);
        CorrelatedAttribute<? super T, ?> correlatedAttribute = (CorrelatedAttribute<? super T, ?>) mappingAttribute;
        CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlatedAttribute.getCorrelationProvider());
        String correlationResult = correlatedAttribute.getCorrelationResult();

        if (correlatedAttribute.getFetchStrategy() == FetchStrategy.JOIN) {
            @SuppressWarnings("unchecked")
            ManagedViewType<Object[]> managedViewType = (ManagedViewType<Object[]>) evm.getMetamodel().managedView(subviewClass);
            String subviewAliasPrefix = getAlias(aliasPrefix, correlatedAttribute);
            String correlationBasis = getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis());
            List<String> subviewMappingPrefix = Collections.singletonList(correlationResult);
            String subviewIdPrefix = correlationResult;
            int[] subviewIdPositions;
            int startIndex;

            if (managedViewType instanceof ViewType<?>) {
                subviewIdPositions = new int[idPositions.length + 1];
                System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
                subviewIdPositions[idPositions.length] = tupleOffset + mappingList.size();
                startIndex = tupleOffset + mappingList.size();
            } else {
                subviewIdPositions = idPositions;
                startIndex = tupleOffset + mappingList.size();
            }

            ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(viewRoot, viewRootAlias, subviewAttributePath, subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions,
                    startIndex, evm, ef, managedViewType, null, proxyFactory);
            Collections.addAll(mappingList, template.mappers);
            // We do not copy because the subview object builder will populate the subview's parameters
            for (int i = 0; i < template.mappers.length; i++) {
                parameterMappingList.add(null);
            }
            tupleTransformatorFactory.add(template.tupleTransformatorFactory);
            tupleTransformatorFactory.add(new CorrelatedSubviewJoinTupleTransformerFactory(template, factory, correlationBasis, correlationResult));
        } else if (correlatedAttribute.getFetchStrategy() == FetchStrategy.SUBQUERY) {
            String subviewAliasPrefix = getAlias(aliasPrefix, correlatedAttribute);
            int startIndex = tupleOffset + mappingList.size();
            Class<?> correlationBasisType = getCorrelationBasisType(correlatedAttribute.getCorrelationBasis());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlatedAttribute.getCorrelationBasis(), correlationBasisType);

            mappingList.add(createMapper(getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis(), correlationBasisEntity), subviewAliasPrefix));
            parameterMappingList.add(null);

            @SuppressWarnings("unchecked")
            ManagedViewType<Object> managedViewType = (ManagedViewType<Object>) evm.getMetamodel().managedView(subviewClass);

            if (batchSize == -1) {
                batchSize = 1;
            }

            if (!correlatedAttribute.isCollection()) {
                tupleTransformatorFactory.add(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                        subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                ));
            } else {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) correlatedAttribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedSingularBatchTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListBatchTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case SET:
                        if (pluralAttribute.isSorted()) {
                            tupleTransformatorFactory.add(new CorrelatedSortedSetBatchTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity, pluralAttribute.getComparator()
                            ));
                        } else if (pluralAttribute.isOrdered()) {
                            tupleTransformatorFactory.add(new CorrelatedOrderedSetBatchTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedSetBatchTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, correlationResult, factory, subviewAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
            }
        } else if (correlatedAttribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = getAlias(aliasPrefix, correlatedAttribute);
            int startIndex = tupleOffset + mappingList.size();
            Class<?> correlationBasisType = getCorrelationBasisType(correlatedAttribute.getCorrelationBasis());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlatedAttribute.getCorrelationBasis(), correlationBasisType);
            String correlationKeyExpression = getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis(), correlationBasisEntity);

            mappingList.add(createMapper(correlationKeyExpression, subviewAliasPrefix));
            parameterMappingList.add(null);

            @SuppressWarnings("unchecked")
            ManagedViewType<Object> managedViewType = (ManagedViewType<Object>) evm.getMetamodel().managedView(subviewClass);

            if (!correlatedAttribute.isCollection()) {
                tupleTransformatorFactory.add(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                        subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                ));
            } else {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) correlatedAttribute;
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListSubselectTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListSubselectTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case SET:
                        if (pluralAttribute.isSorted()) {
                            tupleTransformatorFactory.add(new CorrelatedSortedSetSubselectTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity, pluralAttribute.getComparator()
                            ));
                        } else if (pluralAttribute.isOrdered()) {
                            tupleTransformatorFactory.add(new CorrelatedOrderedSetSubselectTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedSetSubselectTupleListTransformerFactory(
                                    new SubviewCorrelator(managedViewType, evm, subviewAliasPrefix),
                                    subviewClass, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + correlatedAttribute.getFetchStrategy());
        }
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, List<TupleElementMapper> mappingList, List<String> parameterMappingList, int batchSize) {
        mappingList.add(createMapper(getMapping(mappingPrefix, mappingAttribute), getAlias(aliasPrefix, mappingAttribute)));
        parameterMappingList.add(null);
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, List<TupleElementMapper> mappingList, List<String> parameterMappingList) {
        mappingList.add(createMapper("NULL"));
        parameterMappingList.add(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, List<TupleElementMapper> mappingList, List<String> parameterMappingList) {
        @SuppressWarnings("unchecked")
        SubqueryProviderFactory factory = SubqueryProviderHelper.getFactory(attribute.getSubqueryProvider());
        String alias = getAlias(aliasPrefix, attribute);
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
                    mapper = new SubqueryTupleElementMapper(factory.create(null, null));
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
        mappingList.add(mapper);
        parameterMappingList.add(null);
    }

    private void applyBasicCorrelatedMapping(CorrelatedAttribute<?, ?> correlatedAttribute, String attributePath, List<TupleElementMapper> mappingList, List<String> parameterMappingList, int batchSize) {
        if (correlatedAttribute.getFetchStrategy() == FetchStrategy.JOIN) {
            @SuppressWarnings("unchecked")
            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlatedAttribute.getCorrelationProvider());
            String alias = getAlias(aliasPrefix, correlatedAttribute);
            String correlationResult = correlatedAttribute.getCorrelationResult();
            String correlationBasis = getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis());

            TupleElementMapper mapper;
            if (factory.isParameterized()) {
                mapper = new ParameterizedExpressionCorrelationJoinTupleElementMapper(factory, correlationBasis, correlationResult, alias);
            } else {
                mapper = new ExpressionCorrelationJoinTupleElementMapper(factory.create(null, null), correlationBasis, correlationResult, alias);
            }
            mappingList.add(mapper);
            parameterMappingList.add(null);
        } else if (correlatedAttribute.getFetchStrategy() == FetchStrategy.SUBQUERY) {
            String subviewAliasPrefix = getAlias(aliasPrefix, correlatedAttribute);
            int startIndex = tupleOffset + mappingList.size();
            Class<?> correlationBasisType = getCorrelationBasisType(correlatedAttribute.getCorrelationBasis());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlatedAttribute.getCorrelationBasis(), correlationBasisType);
            String correlationKeyExpression = getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis(), correlationBasisEntity);

            mappingList.add(createMapper(correlationKeyExpression, subviewAliasPrefix));
            parameterMappingList.add(null);

            Class<?> resultType;
            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlatedAttribute.getCorrelationProvider());
            String correlationResult = correlatedAttribute.getCorrelationResult();

            if (batchSize == -1) {
                batchSize = 1;
            }

            String basicAttributePath = getAttributePath(attributePath, correlatedAttribute);
            if (!correlatedAttribute.isCollection()) {
                // TODO: shouldn't we embed this query no matter what strategy is used?
                resultType = correlatedAttribute.getJavaType();
                tupleTransformatorFactory.add(new CorrelatedSingularBatchTupleListTransformerFactory(
                        new BasicCorrelator(),
                        resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                ));
            } else {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) correlatedAttribute;
                resultType = pluralAttribute.getElementType();
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListBatchTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListBatchTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case SET:
                        if (pluralAttribute.isSorted()) {
                            tupleTransformatorFactory.add(new CorrelatedSortedSetBatchTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity, pluralAttribute.getComparator()
                            ));
                        } else if (pluralAttribute.isOrdered()) {
                            tupleTransformatorFactory.add(new CorrelatedOrderedSetBatchTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedSetBatchTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, correlationResult, factory, basicAttributePath, startIndex, batchSize, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
            }
        } else if (correlatedAttribute.getFetchStrategy() == FetchStrategy.SUBSELECT) {
            String subviewAliasPrefix = getAlias(aliasPrefix, correlatedAttribute);
            int startIndex = tupleOffset + mappingList.size();
            Class<?> correlationBasisType = getCorrelationBasisType(correlatedAttribute.getCorrelationBasis());
            Class<?> correlationBasisEntity = getCorrelationBasisEntityType(correlatedAttribute.getCorrelationBasis(), correlationBasisType);
            String correlationKeyExpression = getMapping(mappingPrefix, correlatedAttribute.getCorrelationBasis(), correlationBasisEntity);

            mappingList.add(createMapper(correlationKeyExpression, subviewAliasPrefix));
            parameterMappingList.add(null);

            Class<?> resultType;
            CorrelationProviderFactory factory = CorrelationProviderHelper.getFactory(correlatedAttribute.getCorrelationProvider());
            String correlationResult = correlatedAttribute.getCorrelationResult();

            if (!correlatedAttribute.isCollection()) {
                resultType = correlatedAttribute.getJavaType();
                tupleTransformatorFactory.add(new CorrelatedSingularSubselectTupleListTransformerFactory(
                        new BasicCorrelator(),
                        resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                ));
            } else {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) correlatedAttribute;
                resultType = pluralAttribute.getElementType();
                switch (pluralAttribute.getCollectionType()) {
                    case COLLECTION:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListSubselectTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case LIST:
                        if (pluralAttribute.isSorted()) {
                            throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedListSubselectTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case SET:
                        if (pluralAttribute.isSorted()) {
                            tupleTransformatorFactory.add(new CorrelatedSortedSetSubselectTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity, pluralAttribute.getComparator()
                            ));
                        } else if (pluralAttribute.isOrdered()) {
                            tupleTransformatorFactory.add(new CorrelatedOrderedSetSubselectTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        } else {
                            tupleTransformatorFactory.add(new CorrelatedSetSubselectTupleListTransformerFactory(
                                    new BasicCorrelator(),
                                    resultType, viewRoot, viewRootAlias, correlationResult, correlationKeyExpression, factory, startIndex, correlationBasisType, correlationBasisEntity
                            ));
                        }
                        break;
                    case MAP:
                        throw new IllegalArgumentException("Map type unsupported for correlated mappings!");
                    default:
                        throw new IllegalArgumentException("Unknown collection type: " + pluralAttribute.getCollectionType());
                }
            }
        } else {
            throw new UnsupportedOperationException("Unknown fetch strategy: " + correlatedAttribute.getFetchStrategy());
        }
    }

    private List<String> createSubviewMappingPrefix(List<String> prefixParts, String mapping) {
        if (prefixParts == null || prefixParts.isEmpty()) {
            return Collections.singletonList(mapping);
        }

        List<String> subviewMappingPrefix = new ArrayList<String>(prefixParts.size() + 1);
        subviewMappingPrefix.addAll(prefixParts);
        subviewMappingPrefix.add(mapping);
        return subviewMappingPrefix;
    }

    private List<String> createSubviewMappingPrefix(List<String> prefixParts, MappingAttribute<?, ?> mappingAttribute) {
        return createSubviewMappingPrefix(prefixParts, mappingAttribute.getMapping());
    }

    private Class<?> getCorrelationBasisType(String correlationBasis) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(entityMetamodel, managedTypeClass);
        ef.createPathExpression(correlationBasis).accept(visitor);
        Collection<Class<?>> possibleTypes = visitor.getPossibleTargets().values();
        if (possibleTypes.size() > 1) {
            throw new IllegalArgumentException("The correlation basis '" + correlationBasis + "' is ambiguous in the context of the managed type '" + managedTypeClass.getName() + "'!");
        }
        // It must have one, otherwise a parse error would have been thrown already
        Class<?> entityClazz = possibleTypes.iterator().next();

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

    private String getMapping(List<String> prefixParts, String mapping, Class<?> expressionType) {
        if (expressionType == null) {
            return getMapping(prefixParts, mapping);
        }

        ManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(expressionType);
        if (managedType == null || !(managedType instanceof IdentifiableType<?>)) {
            return getMapping(prefixParts, mapping);
        }

        IdentifiableType<?> identifiableType = (IdentifiableType<?>) managedType;
        javax.persistence.metamodel.SingularAttribute<?, ?> idAttr = identifiableType.getId(identifiableType.getIdType().getJavaType());
        return getMapping(prefixParts, mapping + '.' + idAttr.getName());
    }

    private String getMapping(List<String> prefixParts, String mapping) {
        if (prefixParts != null && prefixParts.size() > 0) {
            Expression expr = ef.createSimpleExpression(mapping, false);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(prefixParts);
            StringBuilder sb = new StringBuilder();
            generator.setQueryBuffer(sb);
            expr.accept(generator);
            return sb.toString();
        }

        return mapping;
    }

    private String getMapping(List<String> prefixParts, MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(prefixParts, mappingAttribute.getMapping());
    }

    private String getMapping(String prefix, String mapping) {
        if (prefix != null) {
            return prefix + "." + mapping;
        }

        return mapping;
    }

    private String getMapping(String prefix, MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(prefix, mappingAttribute.getMapping());
    }

    private static String getAlias(String prefix, String attributeName) {
        if (prefix == null) {
            return attributeName;
        } else {
            return prefix + "_" + attributeName;
        }
    }

    private static <T> String getAlias(String prefix, Attribute<?, ?> attribute) {
        if (attribute instanceof MethodAttribute<?, ?>) {
            return getAlias(prefix, ((MethodAttribute<?, ?>) attribute).getName());
        } else {
            return getAlias(prefix, "$" + ((ParameterAttribute<?, ?>) attribute).getIndex());
        }
    }

    private String getAttributePath(String attributePath, Attribute<?, ?> attribute) {
        String attributeName;
        if (attribute instanceof MethodAttribute<?, ?>) {
            attributeName = ((MethodAttribute<?, ?>) attribute).getName();
        } else {
            attributeName = "$" + ((ParameterAttribute<?, ?>) attribute).getIndex();
        }

        if (attributePath == null || attributePath.isEmpty()) {
            return attributeName;
        }

        return attributePath + "." + attributeName;
    }

    public ObjectBuilder<T> createObjectBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return createObjectBuilder(queryBuilder, optionalParameters, entityViewConfiguration, false);
    }

    public ObjectBuilder<T> createObjectBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, boolean isSubview) {
        boolean hasOffset = tupleOffset != 0;
        ObjectBuilder<T> result;

        result = new ViewTypeObjectBuilder<T>(this, queryBuilder, optionalParameters);

        if (hasOffset || isSubview || hasIndexedCollections || hasSubviews) {
            result = new ReducerViewTypeObjectBuilder<T>(result, tupleOffset, mappers.length);
        }

        if (hasParameters) {
            result = new ParameterViewTypeObjectBuilder<T>(result, this, queryBuilder, optionalParameters, tupleOffset);
        }

        if (tupleTransformatorFactory.hasTransformers() && !isSubview) {
            result = new ChainingObjectBuilder<T>(tupleTransformatorFactory, result, queryBuilder, optionalParameters, entityViewConfiguration, tupleOffset);
        }

        return result;
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
        private final ManagedViewType<Object> viewType;
        private final MappingConstructor<Object> constructor;
        private final String name;
        private final String entityViewRoot;
        private final int offset;

        public Key(ExpressionFactory ef, ManagedViewType<?> viewType, MappingConstructor<?> constructor, String name, String entityViewRoot, int offset) {
            this.ef = ef;
            this.viewType = (ManagedViewType<Object>) viewType;
            this.constructor = (MappingConstructor<Object>) constructor;
            this.name = name;
            this.entityViewRoot = entityViewRoot;
            this.offset = offset;
        }

        public ViewTypeObjectBuilderTemplate<?> createValue(EntityViewManagerImpl evm, ProxyFactory proxyFactory) {
            int[] idPositions = new int[]{ 0 };
            List<String> mappingPrefixes = null;
            if (entityViewRoot != null && entityViewRoot.length() > 0) {
                mappingPrefixes = Arrays.asList(entityViewRoot);
            }
            return new ViewTypeObjectBuilderTemplate<Object>(viewType, entityViewRoot, "", name, mappingPrefixes, entityViewRoot, idPositions, offset, evm, ef, viewType, constructor, proxyFactory);
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
