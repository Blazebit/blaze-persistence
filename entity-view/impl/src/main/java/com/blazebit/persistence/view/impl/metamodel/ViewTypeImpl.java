/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.LockMode;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewFilterMapping;

import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewTypeImpl<X> extends ManagedViewTypeImpl<X> implements ViewTypeImplementor<X> {

    private static final Logger LOG = Logger.getLogger(ViewTypeImpl.class.getName());

    private final String lockOwner;
    private final MethodAttribute<? super X, ?> idAttribute;
    private final MethodAttribute<? super X, ?> versionAttribute;
    private final Map<String, ViewFilterMapping> viewFilters;
    private final boolean supportsInterfaceEquals;
    private final boolean supportsUserTypeEquals;

    public ViewTypeImpl(ViewMapping viewMapping, ManagedType<?> managedType, MetamodelBuildingContext context) {
        super(viewMapping, managedType, context, null);

        if (viewMapping.getViewFilterProviders() == null) {
            this.viewFilters = Collections.emptyMap();
        } else {
            Map<String, ViewFilterMapping> viewFilters = new HashMap<String, ViewFilterMapping>();
            for (Map.Entry<String, Class<? extends ViewFilterProvider>> entry : viewMapping.getViewFilterProviders().entrySet()) {
                viewFilters.put(entry.getKey(), new ViewFilterMappingImpl(this, entry.getKey(), entry.getValue()));
            }
            this.viewFilters = Collections.unmodifiableMap(viewFilters);
        }
        this.idAttribute = viewMapping.getIdAttribute().getMethodAttribute(this, -1, -1, context, null);

        if (getLockMode() != LockMode.NONE) {
            if (viewMapping.getVersionAttribute() != null) {
                this.versionAttribute = viewMapping.getVersionAttribute().getMethodAttribute(this, -1, -1, context, null);
            } else {
                this.versionAttribute = null;
            }
            // TODO: validate lock owner path is valid and target has a version if optimistic
            // Also verify that lock owner isn't set when we have a version attribute?
            this.lockOwner = viewMapping.getLockOwner();
        } else {
            this.versionAttribute = null;
            this.lockOwner = null;
            if (viewMapping.getVersionAttribute() != null) {
                context.addError("Invalid version attribute mapping defined for managed view type '" + getJavaType().getName() + "'!");
            }
            if (viewMapping.getLockOwner() != null) {
                context.addError("Invalid lock owner mapping defined for managed view type '" + getJavaType().getName() + "'!");
            }
        }
        boolean supportsInterfaceEquals = true;
        boolean supportsUserTypeEquals = true;
        Method javaMethod = idAttribute.getJavaMethod();
        if (!Modifier.isPublic(javaMethod.getModifiers()) && !Objects.equals(getJavaType().getPackage(), javaMethod.getDeclaringClass().getPackage())) {
            supportsInterfaceEquals = false;
            supportsUserTypeEquals = false;
            LOG.warning("The method for the " + ((AbstractMethodAttribute<?, ?>) idAttribute).getLocation() + " is non-public and declared in a different package " + javaMethod.getDeclaringClass().getPackage() + " than the view type " + getJavaType().getName() +
                    " which makes it impossible to allow checking for equality with user provided implementations of the view type. If you don't need that, you can ignore this warning.");
            // We also disallow interface equality when the view is defined for an abstract entity type
        } else if (getJpaManagedType().getPersistenceType() != javax.persistence.metamodel.Type.PersistenceType.ENTITY || java.lang.reflect.Modifier.isAbstract(getJpaManagedType().getJavaType().getModifiers())) {
            supportsUserTypeEquals = false;
            LOG.warning("The view class " + getJavaType().getName() + " is defined for an abstract or non-entity type which is why id-based equality can't be checked on a user provided instance. If you don't need that, you can ignore this warning.");
        }

        this.supportsInterfaceEquals = supportsInterfaceEquals;
        this.supportsUserTypeEquals = supportsUserTypeEquals;
        context.finishViewType(this);
    }

    @Override
    public ViewTypeImplementor<X> getRealType() {
        return this;
    }

    @Override
    protected boolean hasId() {
        return true;
    }

    @Override
    public MappingType getMappingType() {
        return MappingType.VIEW;
    }

    @Override
    public boolean supportsInterfaceEquals() {
        return supportsInterfaceEquals;
    }

    @Override
    public boolean supportsUserTypeEquals() {
        return supportsUserTypeEquals;
    }

    @Override
    public MethodAttribute<? super X, ?> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public MethodAttribute<? super X, ?> getVersionAttribute() {
        return versionAttribute;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public ViewFilterMapping getViewFilter(String filterName) {
        return viewFilters.get(filterName);
    }

    @Override
    public Set<ViewFilterMapping> getViewFilters() {
        return new SetView<ViewFilterMapping>(viewFilters.values());
    }

}
