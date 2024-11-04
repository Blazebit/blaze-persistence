/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.FlatViewType;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Objects;
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
            if (!Modifier.isPublic(javaMethod.getModifiers()) && !Objects.equals(getJavaType().getPackage(), javaMethod.getDeclaringClass().getPackage())) {
                supportsInterfaceEquals = false;
                LOG.warning("The method for the " + attribute.getLocation() + " is non-public and declared in a different package " + javaMethod.getDeclaringClass().getPackage() + " than the view type " + getJavaType().getName() +
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
