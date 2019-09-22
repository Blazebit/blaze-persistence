/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.view.metamodel.FlatViewType;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlatViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements FlatViewTypeImplementor<X> {

    private static final Logger LOG = Logger.getLogger(FlatViewTypeImpl.class.getName());

    private final boolean supportsInterfaceEquals;

    @SuppressWarnings("unchecked")
    public FlatViewTypeImpl(ViewMapping viewMapping, ManagedType<?> managedType, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        super(viewMapping, managedType, context, embeddableMapping);
        boolean supportsInterfaceEquals = true;
        for (AbstractMethodAttribute<?, ?> attribute : (Collection<AbstractMethodAttribute<?, ?>>) (Collection<?>) getAttributes()) {
            Method javaMethod = attribute.getJavaMethod();
            if (!Modifier.isPublic(javaMethod.getModifiers()) && !getJavaType().getPackage().getName().equals(javaMethod.getDeclaringClass().getPackage().getName())) {
                supportsInterfaceEquals = false;
                LOG.warning("The method for the " + attribute.getLocation() + " is non-public and declared in a different package " + javaMethod.getDeclaringClass().getPackage().getName() + " than the view type " + getJavaType().getName() +
                        " which makes it impossible to allow checking for equality with user provided implementations of the view type. If you don't need that, you can ignore this warning.");
            }
        }

        this.supportsInterfaceEquals = supportsInterfaceEquals;
        context.finishViewType(this);
    }

    @Override
    public FlatViewTypeImplementor<X> getRealType() {
        return this;
    }

    @Override
    protected boolean hasId() {
        return false;
    }

    @Override
    public boolean supportsInterfaceEquals() {
        return supportsInterfaceEquals;
    }

    @Override
    public MappingType getMappingType() {
        return MappingType.FLAT_VIEW;
    }

    @Override
    public int hashCode() {
        return getJavaType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FlatViewType<?> && getJavaType().equals(((FlatViewType<?>) obj).getJavaType());
    }
}
