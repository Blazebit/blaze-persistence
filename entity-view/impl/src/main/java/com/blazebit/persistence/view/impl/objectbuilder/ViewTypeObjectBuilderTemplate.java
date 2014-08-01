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

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author cpbec
 */
public class ViewTypeObjectBuilderTemplate<T> {
    
    private final Constructor<? extends T> proxyConstructor;
    private final Object[][] mappings;
    private final String[] parameterMappings;
    private final int effectiveTupleSize;
    private final boolean hasParameters;
    private final boolean hasCollections;
    private final boolean transformersHaveParameters;
    
    private final String aliasPrefix;
    private final String mappingPrefix;
    private final String idPrefix;
    private final int[] idPositions;
    private final Metamodel metamodel;
    private final EntityViewManager evm;
    private final ViewType<T> viewType;
    private final ProxyFactory proxyFactory;
    private final List<TupleTransformer> tupleTransformers = new ArrayList<TupleTransformer>();
    private final List<TupleListTransformer> tupleListTransformers = new ArrayList<TupleListTransformer>();
    private int transformingElementCount = 0;
    private int transformingElementListCount = 0;

    private ViewTypeObjectBuilderTemplate(String aliasPrefix, String mappingPrefix, String idPrefix, int[] idPositions, Metamodel metamodel, EntityViewManager evm, ViewType<T> viewType, MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory) {
        if (mappingConstructor == null) {
            if(viewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + viewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (viewType.getConstructors().size() == 1) {
                mappingConstructor = (MappingConstructor<T>) viewType.getConstructors().toArray()[0];
            }
        }
        
        this.aliasPrefix = aliasPrefix;
        this.mappingPrefix = mappingPrefix;
        this.idPrefix = idPrefix;
        this.idPositions = idPositions;
        this.metamodel = metamodel;
        this.evm = evm;
        this.viewType = viewType;
        this.proxyFactory = proxyFactory;
        
        Class<?> proxyClass = proxyFactory.getProxy(viewType);
        Constructor<?>[] constructors = proxyClass.getDeclaredConstructors();
        Set<MethodAttribute<? super T, ?>> attributeSet = viewType.getAttributes();
        MethodAttribute<?, ?>[] attributes = attributeSet.toArray(new MethodAttribute<?, ?>[attributeSet.size()]);
        ParameterAttribute<?, ?>[] parameterAttributes;
        boolean[] featuresFound = new boolean[3];
        
        if (mappingConstructor == null) {
            parameterAttributes = new ParameterAttribute<?, ?>[0];
        } else {
            List<ParameterAttribute<? super T, ?>> parameterAttributeList = mappingConstructor.getParameterAttributes();
            parameterAttributes = parameterAttributeList.toArray(new ParameterAttribute<?, ?>[parameterAttributeList.size()]);
        }
        
        int length = 1 + attributes.length + parameterAttributes.length;
        Constructor<? extends T> javaConstructor = null;
        List<Object[]> mappingList = new ArrayList<Object[]>(length);
        List<String> parameterMappingList = new ArrayList<String>(length);
        
        // First we add the id attribute
        EntityType<?> entityType = metamodel.entity(viewType.getEntityClass());
        String idAttributeName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        mappingList.add(0, new Object[] { getMapping(idPrefix, idAttributeName), getAlias("_" + aliasPrefix, idAttributeName) });
        parameterMappingList.add(0, null);
        
        OUTER: for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                continue;
            }
            // parameterTypes[0] is the id
            for (int i = 0; i < attributes.length; i++) {
                MethodAttribute<?, ?> attribute = attributes[i];
                if (attribute.getJavaType() != parameterTypes[i + 1]) {
                    continue OUTER;
                } else {                    
                    applyMapping(attribute, mappingList, parameterMappingList, featuresFound);
                }
            }
            for (int i = 0; i < parameterAttributes.length; i++) {
                ParameterAttribute<?, ?> attribute = parameterAttributes[i];
                
                if (attribute.getJavaType() != parameterTypes[i + attributes.length + 1]) {
                    continue OUTER;
                } else {
                    applyMapping(attribute, mappingList, parameterMappingList, featuresFound);
                }
            }
            
            javaConstructor = (Constructor<? extends T>) constructor;
            break;
        }
        
        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClass.getName());
        }
        
        this.hasParameters = featuresFound[1];
        this.transformersHaveParameters = featuresFound[0];
        this.hasCollections = featuresFound[2];
        this.effectiveTupleSize = length;
        this.proxyConstructor = javaConstructor;        
        this.mappings = mappingList.toArray(new Object[mappingList.size()][]);
        this.parameterMappings = parameterMappingList.toArray(new String[parameterMappingList.size()]);
    }

    private void applyMapping(Attribute<?, ?> attribute, List<Object[]> mappingList, List<String> parameterMappingList, boolean[] featuresFound) {
        if (attribute.isSubquery()) {
            applySubqueryMapping((SubqueryAttribute<? super T, ?>) attribute, mappingList, parameterMappingList);
        } else {
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
            if (attribute.isCollection()) {
                featuresFound[2] = true;
                boolean listKey = attribute instanceof ListAttribute<?, ?>;
                boolean mapKey = attribute instanceof MapAttribute<?, ?, ?>;
                int startIndex = mappingList.size() - (transformingElementCount + transformingElementListCount);
                
                if (listKey) {
                    applyCollectionKeyMapping("INDEX", mappingAttribute, attribute, mappingList);
                } else if (mapKey) {
                    applyCollectionKeyMapping("KEY", mappingAttribute, attribute, mappingList);
                }
                
                if (attribute.isSubview()) {
                    PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
                    int[] newIdPositions;
                    
                    if (listKey || mapKey) {
                        newIdPositions = new int[idPositions.length + 1];
                        System.arraycopy(idPositions, 0, newIdPositions, 0, idPositions.length);
                        newIdPositions[idPositions.length] = mappingList.size() - transformingElementCount;
                    } else {
                        newIdPositions = idPositions;
                    }
                    
                    applySubviewMapping(attribute, newIdPositions, pluralAttribute.getElementType(), mappingAttribute, mappingList, parameterMappingList);
                } else {
                    applyBasicMapping(mappingAttribute, attribute, mappingList, parameterMappingList);
                }
                
                if (listKey) {
                    transformingElementListCount += 1;
                    tupleListTransformers.add(new ListTupleListTransformer(idPositions, startIndex));
                } else if (mapKey) {
                    transformingElementListCount += 1;
                    tupleListTransformers.add(new MapTupleListTransformer(idPositions, startIndex));
                } else {
                    if (attribute instanceof SetAttribute<?, ?>) {
                        tupleListTransformers.add(new SetTupleListTransformer(idPositions, startIndex));
                    } else {
                        // Collection
                        throw new IllegalArgumentException("Collection types are not supported. Please use a Set or a List instead.");
                    }
                }
            } else if (((SingularAttribute) attribute).isQueryParameter()) {
                featuresFound[1] = true;
                applyQueryParameterMapping(mappingAttribute, mappingList, parameterMappingList);
            } else if (attribute.isSubview()) {
                applySubviewMapping(attribute, idPositions, attribute.getJavaType(), mappingAttribute, mappingList, parameterMappingList);
            } else {
                applyBasicMapping(mappingAttribute, attribute, mappingList, parameterMappingList);
            }
        }
    }

    private void applyCollectionKeyMapping(String keyFunction, MappingAttribute<? super T, ?> mappingAttribute, Attribute<?, ?> attribute, List<Object[]> mappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = keyFunction + "(" + getMapping(mappingPrefix, mappingAttribute) + ")";
        mapping[1] = getAlias(aliasPrefix, attribute) + "_KEY";
        mappingList.add(mapping);
    }

    private void applySubviewMapping(Attribute<?, ?> attribute, int[] idPositions, Class<?> subviewClass, MappingAttribute<? super T, ?> mappingAttribute, List<Object[]> mappingList, List<String> parameterMappingList) {
        ViewType<Object[]> subviewType = (ViewType<Object[]>) evm.getMetamodel().view(subviewClass);
        String subviewAliasPrefix = getAlias(aliasPrefix, attribute);
        String subviewMappingPrefix = getMapping(mappingPrefix, mappingAttribute);
        String subviewIdPrefix = getMapping(idPrefix, mappingAttribute);
        int[] subviewIdPositions = new int[idPositions.length + 1];
        System.arraycopy(idPositions, 0, subviewIdPositions, 0, idPositions.length);
        subviewIdPositions[idPositions.length] = mappingList.size();
        ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(subviewAliasPrefix, subviewMappingPrefix, subviewIdPrefix, subviewIdPositions, metamodel, evm, subviewType, null, proxyFactory);
        int startIndex = mappingList.size() - transformingElementCount;
        Collections.addAll(mappingList, template.mappings);
        parameterMappingList.add(null);
        tupleTransformers.addAll(template.tupleTransformers);
        tupleListTransformers.addAll(template.tupleListTransformers);
        transformingElementCount += template.transformingElementCount;
        transformingElementCount += (template.effectiveTupleSize - 1);
        transformingElementListCount += template.transformingElementListCount;
        
        tupleTransformers.add(new SubviewTupleTransformer(template, startIndex));
    }

    private void applyBasicMapping(MappingAttribute<? super T, ?> mappingAttribute, Attribute<?, ?> attribute, List<Object[]> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = getMapping(mappingPrefix, mappingAttribute);
        mapping[1] = getAlias(aliasPrefix, attribute);
        mappingList.add(mapping);
        parameterMappingList.add(null);
    }

    private void applyQueryParameterMapping(MappingAttribute<? super T, ?> mappingAttribute, List<Object[]> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = "NULLIF(1,1)";
        mappingList.add(mapping);
        parameterMappingList.add(mappingAttribute.getMapping());
    }

    private void applySubqueryMapping(SubqueryAttribute<?, ?> attribute, List<Object[]> mappingList, List<String> parameterMappingList) {
        Object[] mapping = new Object[2];
        mapping[0] = attribute.getSubqueryProvider();
        mapping[1] = getAlias(aliasPrefix, attribute);
        mappingList.add(mapping);
        parameterMappingList.add(null);
    }
    
    private static String getMapping(String prefix, String mapping) {
        if (prefix != null) {
            return prefix + "." + mapping;
        }
        
        return mapping;
    }

    private static <T> String getMapping(String prefix, MappingAttribute<?, ?> mappingAttribute) {
        return getMapping(prefix, mappingAttribute.getMapping());
    }
    
    private static String getAlias(String prefix, String attributeName) {
        return prefix + "_" + attributeName;
    }

    private static <T> String getAlias(String prefix, Attribute<?, ?> attribute) {
        if (attribute instanceof MethodAttribute<?, ?>) {
            return getAlias(prefix, ((MethodAttribute<?, ?>) attribute).getName());
        }
        
        return null;
    }
    
    public ObjectBuilder<T> createObjectBuilder(QueryBuilder<?, ?> queryBuilder) {
        return createObjectBuilder(queryBuilder, 0, false);
    }
    
    public ObjectBuilder<T> createObjectBuilder(QueryBuilder<?, ?> queryBuilder, int startIndex, boolean isSubview) {
        boolean hasOffset = startIndex != 0;
        ObjectBuilder<T> result;
        
        if (hasParameters) {
            if (hasOffset || isSubview) {
                result = new ParameterOffsetViewTypeObjectBuilder(this, queryBuilder, startIndex);
            } else {
                result = new ParameterViewTypeObjectBuilder<T>(this, queryBuilder);
            }
        } else {
            if (hasOffset || isSubview) {
                result = new SimpleOffsetViewTypeObjectBuilder(this, startIndex);
            } else {
                result = new SimpleViewTypeObjectBuilder<T>(this);
            }
        }
        
        if (tupleTransformers.size() > 0 || tupleListTransformers.size() > 0) {
            result = new ChainingObjectBuilder<T>(tupleTransformers, tupleListTransformers, result, queryBuilder, startIndex);
        }
        
        return result;
    }

    public Constructor<? extends T> getProxyConstructor() {
        return proxyConstructor;
    }

    public Object[][] getMappings() {
        return mappings;
    }

    public String[] getParameterMappings() {
        return parameterMappings;
    }

    public boolean hasParameters() {
        return hasParameters;
    }

    public int getEffectiveTupleSize() {
        return effectiveTupleSize;
    }
    
    public static class Key<T> {
        private final ViewType<T> viewType;
        private final MappingConstructor<T> constructor;

        public Key(ViewType<T> viewType, MappingConstructor<T> constructor) {
            this.viewType = viewType;
            this.constructor = constructor;
        }
        
        public ViewTypeObjectBuilderTemplate<T> createValue(Metamodel metamodel, EntityViewManager evm, ProxyFactory proxyFactory) {
            int[] idPositions = new int[] { 0 };
            return new ViewTypeObjectBuilderTemplate<T>(viewType.getName(), null, null, idPositions, metamodel, evm, viewType, constructor, proxyFactory);
        }

        @Override
        public int hashCode() {
            int hash = 3;
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
