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
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.SubqueryAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    private final boolean transformersHaveParameters;
    
    private final List<TupleTransformer> tupleTransformers = new ArrayList<TupleTransformer>();
    private final List<TupleListTransformer> tupleListTransformers = new ArrayList<TupleListTransformer>();

    private ViewTypeObjectBuilderTemplate(String aliasPrefix, String mappingPrefix, EntityViewManager evm, ViewType<T> viewType, MappingConstructor<T> mappingConstructor, ProxyFactory proxyFactory) {
        if (mappingConstructor == null) {
            if(viewType.getConstructors().size() > 1) {
                throw new IllegalArgumentException("The given view type '" + viewType.getJavaType().getName() + "' has multiple constructors but the given constructor was null.");
            } else if (viewType.getConstructors().size() == 1) {
                mappingConstructor = (MappingConstructor<T>) viewType.getConstructors().toArray()[0];
            }
        }
        
        Class<?> proxyClass = proxyFactory.getProxy(viewType);
        Constructor<?>[] constructors = proxyClass.getDeclaredConstructors();
        Set<MethodAttribute<? super T, ?>> attributeSet = viewType.getAttributes();
        MethodAttribute<?, ?>[] attributes = attributeSet.toArray(new MethodAttribute<?, ?>[attributeSet.size()]);
        ParameterAttribute<?, ?>[] parameterAttributes;
        boolean parameterFound = false;
        boolean transformersParameterFound = false;
        
        if (mappingConstructor == null) {
            parameterAttributes = new ParameterAttribute<?, ?>[0];
        } else {
            List<ParameterAttribute<? super T, ?>> parameterAttributeList = mappingConstructor.getParameterAttributes();
            parameterAttributes = parameterAttributeList.toArray(new ParameterAttribute<?, ?>[parameterAttributeList.size()]);
        }
        
        int length = attributes.length + parameterAttributes.length;
        Constructor<? extends T> javaConstructor = null;
        List<Object[]> mappingList = new ArrayList<Object[]>(length);
        List<String> parameterMappingList = new ArrayList<String>(length);
        
        OUTER: for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (length != parameterTypes.length) {
                continue;
            }
            for (int i = 0; i < attributes.length; i++) {
                MethodAttribute<?, ?> attribute = attributes[i];
                if (attribute.getJavaType() != parameterTypes[i]) {
                    continue OUTER;
                } else {
                    transformersParameterFound = applyMapping(attribute, aliasPrefix, mappingPrefix, evm, proxyFactory, viewType, mappingList, parameterMappingList, transformersParameterFound);
                }
            }
            for (int i = 0; i < parameterAttributes.length; i++) {
                ParameterAttribute<?, ?> attribute = parameterAttributes[i];
                
                if (attribute.getJavaType() != parameterTypes[i + attributes.length]) {
                    continue OUTER;
                } else {
                    parameterFound = true;
                    transformersParameterFound = applyMapping(attribute, aliasPrefix, mappingPrefix, evm, proxyFactory, viewType, mappingList, parameterMappingList, transformersParameterFound);
                }
            }
            
            javaConstructor = (Constructor<? extends T>) constructor;
            break;
        }
        
        if (javaConstructor == null) {
            throw new IllegalArgumentException("The given mapping constructor '" + mappingConstructor + "' does not map to a constructor of the proxy class: " + proxyClass.getName());
        }
        
        this.proxyConstructor = javaConstructor;
        this.mappings = mappingList.toArray(new Object[mappingList.size()][]);
        this.parameterMappings = parameterMappingList.toArray(new String[parameterMappingList.size()]);
        this.effectiveTupleSize = length;
        this.hasParameters = parameterFound;
        this.transformersHaveParameters = transformersParameterFound;
    }

    private boolean applyMapping(Attribute<?, ?> attribute, String aliasPrefix, String mappingPrefix, EntityViewManager evm, ProxyFactory proxyFactory, ViewType<T> viewType, List<Object[]> mappingList, List<String> parameterMappingList, boolean transformersParameterFound) {
        if (attribute.isSubquery()) {
            Object[] mapping = new Object[2];
            mapping[0] = ((SubqueryAttribute<? super T, ?>) attribute).getSubqueryProvider();
            mapping[1] = getAlias(aliasPrefix, attribute);
            mappingList.add(mapping);
            parameterMappingList.add(null);
        } else {
            MappingAttribute<? super T, ?> mappingAttribute = (MappingAttribute<? super T, ?>) attribute;
            if (attribute.isCollection()) {
                if (attribute instanceof ListAttribute<?, ?> || attribute instanceof MapAttribute<?, ?, ?>) {
                    Object[] mapping = new Object[2];
                    mapping[0] = "KEY(" + getMapping(mappingPrefix, mappingAttribute) + ")";
                    mapping[1] = getAlias(aliasPrefix, attribute) + "_KEY";
                    mappingList.add(mapping);
                    parameterMappingList.add(null);
                }
                Object[] mapping = new Object[2];
                mapping[0] = getMapping(mappingPrefix, mappingAttribute);
                mapping[1] = getAlias(aliasPrefix, attribute);
                mappingList.add(mapping);
                parameterMappingList.add(null);
            } else if (((SingularAttribute) attribute).isQueryParameter()) {
                Object[] mapping = new Object[2];
                mapping[0] = "NULLIF(1,1)";
                mappingList.add(mapping);
                parameterMappingList.add(getMapping(mappingPrefix, mappingAttribute));
            } else if (attribute.isSubview()) {
                ViewType<Object[]> subviewType = (ViewType<Object[]>) evm.getMetamodel().view(attribute.getJavaType());
                String subviewAliasPrefix = getAlias(aliasPrefix, attribute);
                String subviewMappingPrefix = getMapping(mappingPrefix, mappingAttribute);
                ViewTypeObjectBuilderTemplate<Object[]> template = new ViewTypeObjectBuilderTemplate<Object[]>(subviewAliasPrefix, subviewMappingPrefix, evm, subviewType, null, proxyFactory);
                int startIndex = mappingList.size();
                transformersParameterFound = transformersParameterFound || template.hasParameters;
                Collections.addAll(mappingList, template.mappings);
                Collections.addAll(parameterMappingList, template.parameterMappings);
                tupleTransformers.addAll(template.tupleTransformers);
                tupleListTransformers.addAll(template.tupleListTransformers);
                
                tupleTransformers.add(new SubviewTupleTransformer(template, startIndex));
            } else {
                Object[] mapping = new Object[2];
                mapping[0] = getMapping(mappingPrefix, mappingAttribute);
                mapping[1] = getAlias(aliasPrefix, attribute);
                mappingList.add(mapping);
                parameterMappingList.add(null);
            }
        }
        
        return transformersParameterFound;
    }

    private static <T> String getMapping(String prefix, MappingAttribute<?, ?> mappingAttribute) {
        if (prefix != null) {
            return prefix + "." + mappingAttribute.getMapping();
        }
        
        return mappingAttribute.getMapping();
    }

    private static <T> String getAlias(String prefix, Attribute<?, ?> attribute) {
        if (attribute instanceof MethodAttribute<?, ?>) {
            return prefix + "_" + ((MethodAttribute<?, ?>) attribute).getName();
        }
        
        return null;
    }
    
    public ObjectBuilder<T> createObjectBuilder(QueryBuilder<?, ?> queryBuilder) {
        return createObjectBuilder(queryBuilder, 0);
    }
    
    public ObjectBuilder<T> createObjectBuilder(QueryBuilder<?, ?> queryBuilder, int startIndex) {
        boolean hasOffset = startIndex != 0;
        ObjectBuilder<T> result;
        
        if (hasParameters) {
            if (hasOffset) {
                result = new ParameterOffsetViewTypeObjectBuilder(this, queryBuilder, startIndex);
            } else {
                result = new ParameterViewTypeObjectBuilder<T>(this, queryBuilder);
            }
        } else {
            if (hasOffset) {
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
    
    private static class SubviewTupleTransformer extends TupleTransformer {
        
        private final ViewTypeObjectBuilderTemplate<Object[]> template;
        private ObjectBuilder<Object[]> objectBuilder;

        public SubviewTupleTransformer(ViewTypeObjectBuilderTemplate<Object[]> template, int startIndex) {
            super(startIndex);
            this.template = template;
        }

        @Override
        public TupleTransformer init(QueryBuilder<?, ?> queryBuilder) {
            this.objectBuilder = template.createObjectBuilder(queryBuilder, startIndex);
            return this;
        }

        @Override
        public Object[] transform(Object[] tuple) {
            return objectBuilder.build(tuple, null);
        }
        
    }
    
    public static class Key<T> {
        private final ViewType<T> viewType;
        private final MappingConstructor<T> constructor;

        public Key(ViewType<T> viewType, MappingConstructor<T> constructor) {
            this.viewType = viewType;
            this.constructor = constructor;
        }
        
        public ViewTypeObjectBuilderTemplate<T> createValue(EntityViewManager evm, ProxyFactory proxyFactory) {
            return new ViewTypeObjectBuilderTemplate<T>(viewType.getName(), null, evm, viewType, constructor, proxyFactory);
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
