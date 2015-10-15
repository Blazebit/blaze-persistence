/*
 * Copyright 2014 Blazebit.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasExpressionTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.AliasSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionSubqueryTupleElementMapper;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.ExpressionTupleElementMapper;
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
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.impl.proxy.ReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.UnsafeInstantiator;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
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

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewTypeObjectBuilderTemplate<T> {

    private final ObjectInstantiator<T> objectInstantiator;
    private final TupleElementMapper[] mappers;
    private final TupleParameterMapper parameterMapper;
    private final int effectiveTupleSize;
    private final boolean hasParameters;
    private final boolean hasIndexedCollections;
    private final boolean hasSubviews;

    private final String aliasPrefix;
    private final List<String> mappingPrefix;
    private final String idPrefix;
    private final int[] idPositions;
    private final int tupleOffset;
    private final Metamodel metamodel;
    private final EntityViewManagerImpl evm;
    private final ExpressionFactory ef;
    private final ProxyFactory proxyFactory;
    private final TupleTransformatorFactory tupleTransformatorFactory = new TupleTransformatorFactory();
    
    private static final int FEATURE_PARAMETERS = 0;
    private static final int FEATURE_INDEXED_COLLECTIONS = 1;
    private static final int FEATURE_SUBVIEWS = 2;

    @SuppressWarnings("unchecked")
	private ViewTypeObjectBuilderTemplate(String aliasPrefix, List<String> mappingPrefix, String idPrefix, int[] idPositions, int tupleOffset, Metamodel metamodel, EntityViewManagerImpl evm, ExpressionFactory ef, ViewType<T> viewType, MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory) {
        if (mappingConstructor == null) {
            if (viewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + viewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (viewType.getConstructors().size() == 1) {
                mappingConstructor = (MappingConstructor<T>) viewType.getConstructors().toArray()[0];
            }
        }

        this.aliasPrefix = aliasPrefix;
        this.mappingPrefix = mappingPrefix;
        this.idPrefix = idPrefix;
        this.idPositions = idPositions;
        this.tupleOffset = tupleOffset;
        this.metamodel = metamodel;
        this.evm = evm;
        this.ef = ef;
        this.proxyFactory = proxyFactory;

        Set<MethodAttribute<? super T, ?>> attributeSet = viewType.getAttributes();
        // We have special handling for the id attribute since we need to know it's position in advance
        // Therefore we have to remove it so that it doesn't get processed as normal attribute
        attributeSet.remove(viewType.getIdAttribute());
        MethodAttribute<?, ?>[] attributes = attributeSet.toArray(new MethodAttribute<?, ?>[attributeSet.size()]);
        ParameterAttribute<?, ?>[] parameterAttributes;

        if (mappingConstructor == null) {
            parameterAttributes = new ParameterAttribute<?, ?>[0];
        } else {
            List<ParameterAttribute<? super T, ?>> parameterAttributeList = mappingConstructor.getParameterAttributes();
            parameterAttributes = parameterAttributeList.toArray(new ParameterAttribute<?, ?>[parameterAttributeList.size()]);
        }

        int length = 1 + attributes.length + parameterAttributes.length;

        // First we add the id attribute
        EntityType<?> entityType = metamodel.entity(viewType.getEntityClass());
        javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttr = entityType.getId(entityType.getIdType().getJavaType());
        Class<?> idAttributeType;
        
        if (jpaIdAttr.getJavaMember() instanceof Field) {
            idAttributeType = ReflectionUtils.getResolvedFieldType(viewType.getEntityClass(), (Field) jpaIdAttr.getJavaMember());
        } else {
            idAttributeType = ReflectionUtils.getResolvedMethodReturnType(viewType.getEntityClass(), (Method) jpaIdAttr.getJavaMember());
        }
        
        if (idAttributeType == null) {
            throw new IllegalArgumentException("The id attribute type is not resolvable " + "for the attribute '" + jpaIdAttr.getName() + "' of the class '" + viewType.getEntityClass().getName() + "'!");
        }
        
        String idAttributeName = jpaIdAttr.getName();
        MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
        MappingAttribute<?, ?> idMappingAttribute = (MappingAttribute<?, ?>) idAttribute;
        
        if (!idAttributeName.equals(idMappingAttribute.getMapping())) {
            throw new IllegalArgumentException("Invalid id mapping '" + idMappingAttribute.getMapping() +"' for entity view '" + viewType.getJavaType().getName() + "'! Expected '" + idAttributeName +"'!");
        }
        
        String idMapping = idPrefix == null? idAttributeName : idPrefix + "." + idAttributeName;
        
        List<Object> mappingList = new ArrayList<Object>(length);
        List<String> parameterMappingList = new ArrayList<String>(length);
        Class<?>[] parameterTypes = new Class<?>[length];
        boolean[] featuresFound = new boolean[3];
        
        parameterTypes[0] = idAttributeType;
        mappingList.add(0, new Object[]{ idMapping, getAlias(aliasPrefix, idAttribute) });
        parameterMappingList.add(0, null);
        
        for (int i = 0; i < attributes.length; i++) {
            parameterTypes[i + 1] = attributes[i].getJavaType();
        }
        for (int i = 0; i < parameterAttributes.length; i++) {
            parameterTypes[i + attributes.length + 1] = parameterAttributes[i].getJavaType();
        }
        
        if (viewType.getConstructors().isEmpty() || evm.isUnsafeDisabled()) {
        	this.objectInstantiator = new ReflectionInstantiator<T>(mappingConstructor, proxyFactory, viewType, parameterTypes);
        } else {
        	this.objectInstantiator = new UnsafeInstantiator<T>(mappingConstructor, proxyFactory, viewType, parameterTypes);
        }
        
        for (int i = 0; i < attributes.length; i++) {
            applyMapping(attributes[i], mappingList, parameterMappingList, featuresFound);
        }
        for (int i = 0; i < parameterAttributes.length; i++) {
            applyMapping(parameterAttributes[i], mappingList, parameterMappingList, featuresFound);
        }

        this.hasParameters = featuresFound[FEATURE_PARAMETERS];
        this.hasIndexedCollections = featuresFound[FEATURE_INDEXED_COLLECTIONS];
        this.hasSubviews = featuresFound[FEATURE_SUBVIEWS];
        this.effectiveTupleSize = length;
        this.mappers = getMappers(mappingList);
        this.parameterMapper = new TupleParameterMapper(parameterMappingList, tupleOffset);
    }

    private static TupleElementMapper[] getMappers(List<Object> mappingList) {
        TupleElementMapper[] mappers = new TupleElementMapper[mappingList.size()];

        for (int i = 0; i < mappers.length; i++) {
            Object mappingElement = mappingList.get(i);

            if (mappingElement instanceof TupleElementMapper) {
                mappers[i] = (TupleElementMapper) mappingElement;
                continue;
            }

            Object[] mapping = (Object[]) mappingElement;

            if (mapping[0] instanceof Class) {
                @SuppressWarnings("unchecked")
				Class<? extends SubqueryProvider> subqueryProviderClass = (Class<? extends SubqueryProvider>) mapping[0];
                SubqueryProvider provider;

                try {
                    provider = subqueryProviderClass.newInstance();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Could not instantiate the subquery provider: " + subqueryProviderClass.getName(), ex);
                }

                String subqueryAlias = (String) mapping[2];
                String subqueryExpression = (String) mapping[3];

                if (subqueryExpression.isEmpty()) {
                    if (mapping[1] != null) {
                        mappers[i] = new AliasSubqueryTupleElementMapper(provider, (String) mapping[1]);
                    } else {
                        mappers[i] = new SubqueryTupleElementMapper(provider);
                    }
                } else {
                    if (mapping[1] != null) {
                        mappers[i] = new AliasExpressionSubqueryTupleElementMapper(provider, subqueryExpression, subqueryAlias, (String) mapping[1]);
                    } else {
                        mappers[i] = new ExpressionSubqueryTupleElementMapper(provider, subqueryExpression, subqueryAlias);
                    }
                }
            } else {
                if (mapping[1] != null) {
                    mappers[i] = new AliasExpressionTupleElementMapper((String) mapping[0], (String) mapping[1]);
                } else {
                    mappers[i] = new ExpressionTupleElementMapper((String) mapping[0]);
                }
            }
        }

        return mappers;
    }

    @SuppressWarnings("unchecked")
	private void applyMapping(Attribute<?, ?> attribute, List<Object> mappingList, List<String> parameterMappingList, boolean[] featuresFound) {
        if (attribute.isSubquery()) {
            applySubqueryMapping((SubqueryAttribute<? super T, ?>) attribute, mappingList, parameterMappingList);
        } else {
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                boolean listKey = pluralAttribute.isIndexed() && attribute instanceof ListAttribute<?, ?>;
                boolean mapKey = pluralAttribute.isIndexed() && attribute instanceof MapAttribute<?, ?, ?>;
                int startIndex = tupleOffset + mappingList.size();

                if (listKey) {
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping("INDEX", "_KEY", mappingAttribute, attribute, mappingList, parameterMappingList);
                } else if (mapKey) {
                    featuresFound[FEATURE_INDEXED_COLLECTIONS] = true;
                    applyCollectionFunctionMapping("KEY", "_KEY", mappingAttribute, attribute, mappingList, parameterMappingList);
                }

                if (attribute.isSubview()) {
                    featuresFound[FEATURE_SUBVIEWS] = true;

                    int[] newIdPositions;

                    if (listKey || mapKey) {
                        newIdPositions = new int[idPositions.length + 1];
                        System.arraycopy(idPositions, 0, newIdPositions, 0, idPositions.length);
                        newIdPositions[idPositions.length] = mappingList.size();
                    } else {
                        newIdPositions = idPositions;
                    }

                    applySubviewMapping(attribute, newIdPositions, pluralAttribute.getElementType(), mappingAttribute, mappingList, parameterMappingList);
                } else if (mapKey) {
                    applyCollectionFunctionMapping("VALUE", "", mappingAttribute, attribute, mappingList, parameterMappingList);
//                    applyBasicMapping(mappingAttribute, attribute, mappingList, parameterMappingList);
                } else {
                    applyBasicMapping(mappingAttribute, attribute, mappingList, parameterMappingList);
                }

                if (listKey) {
                    if (pluralAttribute.isSorted()) {
                        throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                    } else {
                        tupleTransformatorFactory.add(new IndexedListTupleListTransformer(idPositions, startIndex));
                    }
                } else if (mapKey) {
                    if (pluralAttribute.isSorted()) {
                        tupleTransformatorFactory.add(new SortedMapTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                    } else if (pluralAttribute.isOrdered()) {
                        tupleTransformatorFactory.add(new OrderedMapTupleListTransformer(idPositions, startIndex));
                    } else {
                        tupleTransformatorFactory.add(new MapTupleListTransformer(idPositions, startIndex));
                    }
                } else {
                    switch (pluralAttribute.getCollectionType()) {
                        case COLLECTION:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The collection attribute '" + pluralAttribute + "' can not be sorted!");
                            } else {
                                tupleTransformatorFactory.add(new OrderedListTupleListTransformer(idPositions, startIndex));
                            }
                            break;
                        case LIST:
                            if (pluralAttribute.isSorted()) {
                                throw new IllegalArgumentException("The list attribute '" + pluralAttribute + "' can not be sorted!");
                            } else {
                                tupleTransformatorFactory.add(new OrderedListTupleListTransformer(idPositions, startIndex));
                            }
                            break;
                        case SET:
                            if (pluralAttribute.isSorted()) {
                                tupleTransformatorFactory.add(new SortedSetTupleListTransformer(idPositions, startIndex, pluralAttribute.getComparator()));
                            } else if (pluralAttribute.isOrdered()) {
                                tupleTransformatorFactory.add(new OrderedSetTupleListTransformer(idPositions, startIndex));
                            } else {
                                tupleTransformatorFactory.add(new SetTupleListTransformer(idPositions, startIndex));
                            }
                            break;
                        case MAP:
                            throw new IllegalArgumentException("Ignoring the index on the attribute '" + pluralAttribute + "' is not possible!");
                    }
                }
            } else if (((SingularAttribute<?, ?>) attribute).isQueryParameter()) {
                featuresFound[FEATURE_PARAMETERS] = true;
                applyQueryParameterMapping(mappingAttribute, mappingList, parameterMappingList);
            } else if (attribute.isSubview()) {
                featuresFound[FEATURE_SUBVIEWS] = true;
                applySubviewMapping(attribute, idPositions, attribute.getJavaType(), mappingAttribute, mappingList, parameterMappingList);
            } else {
                applyBasicMapping(mappingAttribute, attribute, mappingList, parameterMappingList);
            }
        }
    }

    private void applyCollectionFunctionMapping(String function, String aliasSuffix, MappingAttribute<? super T, ?> mappingAttribute, Attribute<?, ?> attribute, List<Object> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = function + "(" + getMapping(mappingPrefix, mappingAttribute) + ")";
        String alias = getAlias(aliasPrefix, attribute);
        mapping[1] = alias == null ? null : alias + aliasSuffix;
        mappingList.add(mapping);
        parameterMappingList.add(null);
    }

    private void applySubviewMapping(Attribute<?, ?> attribute, int[] idPositions, Class<?> subviewClass, MappingAttribute<? super T, ?> mappingAttribute, List<Object> mappingList, List<String> parameterMappingList) {
        @SuppressWarnings("unchecked")
		ViewType<Object[]> subviewType = (ViewType<Object[]>) evm.getMetamodel().view(subviewClass);
        String subviewAliasPrefix = getAlias(aliasPrefix, attribute);
        List<String> subviewMappingPrefix = createSubviewMappingPrefix(mappingPrefix, mappingAttribute);
        String subviewIdPrefix = getMapping(idPrefix, mappingAttribute);
        int[] subviewIdPositions = new int[idPositions.length + 1];
        System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
        subviewIdPositions[idPositions.length] = tupleOffset + mappingList.size();
        int startIndex = tupleOffset + mappingList.size();
        ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions,
                                                                                                       startIndex, metamodel, evm, ef, subviewType, null, proxyFactory);
        Collections.addAll(mappingList, template.mappers);
        // We do not copy because the subview object builder will populate the subview's parameters
        for (int i = 0; i < template.mappers.length; i++) {
            parameterMappingList.add(null);
        }
        tupleTransformatorFactory.add(template.tupleTransformatorFactory);
        tupleTransformatorFactory.add(new SubviewTupleTransformerFactory(template));
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, Attribute<?, ?> attribute, List<Object> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = getMapping(mappingPrefix, mappingAttribute);
        mapping[1] = getAlias(aliasPrefix, attribute);
        mappingList.add(mapping);
        parameterMappingList.add(null);
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, List<Object> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = "NULLIF(1,1)";
        mappingList.add(mapping);
        parameterMappingList.add(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, List<Object> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[4];
        mapping[0] = attribute.getSubqueryProvider();
        mapping[1] = getAlias(aliasPrefix, attribute);
        mapping[2] = attribute.getSubqueryAlias();
        mapping[3] = attribute.getSubqueryExpression();
        mappingList.add(mapping);
        parameterMappingList.add(null);
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

    private String getMapping(List<String> prefixParts, String mapping) {
        if (prefixParts != null && prefixParts.size() > 0) {
            Expression expr = ef.createSimpleExpression(mapping);
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

    public ObjectBuilder<T> createObjectBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters) {
        return createObjectBuilder(queryBuilder, optionalParameters, false);
    }

    public ObjectBuilder<T> createObjectBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, boolean isSubview) {
        boolean hasOffset = tupleOffset != 0;
        ObjectBuilder<T> result;

        result = new ViewTypeObjectBuilder<T>(this);

        if (hasOffset || isSubview || hasIndexedCollections || hasSubviews) {
            result = new ReducerViewTypeObjectBuilder<T>(result, tupleOffset, mappers.length);
        }

        if (hasParameters) {
            result = new ParameterViewTypeObjectBuilder<T>(result, this, queryBuilder, optionalParameters, tupleOffset);
        }

        if (tupleTransformatorFactory.hasTransformers() && !isSubview) {
            result = new ChainingObjectBuilder<T>(tupleTransformatorFactory, result, queryBuilder, optionalParameters, tupleOffset);
        }

        return result;
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

    public static class Key<T> {

    	private final ExpressionFactory ef;
        private final ViewType<T> viewType;
        private final MappingConstructor<T> constructor;

        public Key(ExpressionFactory ef, ViewType<T> viewType, MappingConstructor<T> constructor) {
        	this.ef = ef;
            this.viewType = viewType;
            this.constructor = constructor;
        }

        public ViewTypeObjectBuilderTemplate<T> createValue(Metamodel metamodel, EntityViewManagerImpl evm, ProxyFactory proxyFactory) {
            int[] idPositions = new int[]{ 0 };
            return new ViewTypeObjectBuilderTemplate<T>(viewType.getName(), null, null, idPositions, 0, metamodel, evm, ef, viewType, constructor, proxyFactory);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.ef != null ? this.ef.hashCode() : 0);
            hash = 83 * hash + (this.viewType != null ? this.viewType.hashCode() : 0);
            hash = 83 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
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
            final Key<?> other = (Key<?>) obj;
            if (this.ef != other.ef && (this.ef == null || !this.ef.equals(other.ef))) {
                return false;
            }
            if (this.viewType != other.viewType && (this.viewType == null || !this.viewType.equals(other.viewType))) {
                return false;
            }
            if (this.constructor != other.constructor && (this.constructor == null || !this.constructor.equals(other.constructor))) {
                return false;
            }
            return true;
        }
    }
}
