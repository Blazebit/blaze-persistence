/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodMapAttribute;
import com.blazebit.persistence.view.metamodel.MethodPluralAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractSingularChangeModel<V> extends AbstractChangeModel<V, V> implements SingularChangeModel<V> {

    private ChangeModel<?>[] attributeChangeModels;

    public AbstractSingularChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType) {
        super(type, basicType);
    }

    @Override
    public <X> SingularChangeModel<X> get(SingularAttribute<V, X> attribute) {
        return (SingularChangeModel<X>) get(getMutableAttribute(attribute));
    }

    @Override
    public <E, C extends Collection<E>> PluralChangeModel<C, E> get(PluralAttribute<V, C, E> attribute) {
        return (PluralChangeModel<C, E>) (ChangeModel<?>) get(getMutableAttribute(attribute));
    }

    @Override
    public <K, V1> MapChangeModel<K, V1> get(MapAttribute<V, K, V1> attribute) {
        return (MapChangeModel<K, V1>) this.<Map<K, V1>>get(getMutableAttribute(attribute));
    }

    @Override
    public <X> SingularChangeModel<X> get(MethodSingularAttribute<V, X> attribute) {
        return (SingularChangeModel<X>) get(getMutableAttribute(attribute));
    }

    @Override
    public <E, C extends Collection<E>> PluralChangeModel<C, E> get(MethodPluralAttribute<V, C, E> attribute) {
        return (PluralChangeModel<C, E>) (ChangeModel<?>) get(getMutableAttribute(attribute));
    }

    @Override
    public <K, V1> MapChangeModel<K, V1> get(MethodMapAttribute<V, K, V1> attribute) {
        return (MapChangeModel<K, V1>) this.<Map<K, V1>>get(getMutableAttribute(attribute));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <X> ChangeModel<X> getChangeModel(Object initialAttributeObject, Object attributeObject, AbstractMethodAttribute<?, ?> methodAttribute, DirtyChecker<Object> attributeDirtyChecker) {
        if (attributeChangeModels == null) {
            this.attributeChangeModels = new ChangeModel[type.getMutableAttributeCount()];
        }

        final int index = methodAttribute.getDirtyStateIndex();
        ChangeModel<?> changeModel = attributeChangeModels[index];
        if (changeModel != null && changeModel.getCurrentState() == attributeObject) {
            return (ChangeModel<X>) changeModel;
        }

        changeModel = super.getChangeModel(initialAttributeObject, attributeObject, methodAttribute, attributeDirtyChecker);
        this.attributeChangeModels[index] = changeModel;
        return (ChangeModel<X>) changeModel;
    }

}
