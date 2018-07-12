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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractParameterAttribute<X, Y> extends AbstractAttribute<X, Y> implements ParameterAttribute<X, Y> {

    private final int index;
    private final MappingConstructor<X> declaringConstructor;

    public AbstractParameterAttribute(MappingConstructorImpl<X> constructor, ParameterAttributeMapping mapping, MetamodelBuildingContext context) {
        super(constructor.getDeclaringType(), mapping, context);
        this.index = mapping.getIndex();
        this.declaringConstructor = constructor;

        if (this.mapping != null && this.mapping.isEmpty()) {
            context.addError("Illegal empty mapping for the " + mapping.getErrorLocation());
        }
    }

    @Override
    protected Class<?>[] getTypeArguments() {
        Class<?> clazz = getDeclaringType().getJavaType();
        Constructor<?> constructor = getDeclaringConstructor().getJavaConstructor();
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        return ReflectionUtils.resolveTypeArguments(clazz, genericParameterTypes[getIndex()]);
    }

    public static Annotation getMapping(Constructor<?> constructor, int index, MetamodelBootContext context) {
        Annotation[] annotations = constructor.getParameterAnnotations()[index];

        for (Annotation a : annotations) {
            if (MappingParameter.class.isInstance(a)
                    || Mapping.class.isInstance(a)
                    || MappingSubquery.class.isInstance(a)
                    || MappingCorrelated.class.isInstance(a)
                    || MappingCorrelatedSimple.class.isInstance(a)) {
                return a;
            }
        }
        
        context.addError("No MappingParameter annotation given for the " + ParameterAttributeMapping.getLocation(constructor, index));
        return null;
    }

    @Override
    public String getLocation() {
        return ParameterAttributeMapping.getLocation(declaringConstructor.getJavaConstructor(), index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean isUpdatable() {
        return false;
    }

    @Override
    public boolean needsDirtyTracker() {
        return false;
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.PARAMETER;
    }

    @Override
    public Set<Class<?>> getAllowedSubtypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOptimizeCollectionActionsEnabled() {
        return false;
    }

    @Override
    public MappingConstructor<X> getDeclaringConstructor() {
        return declaringConstructor;
    }

}
