/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewChangeModel<V extends DirtyStateTrackable> extends AbstractSingularChangeModel<V> {

    private final V object;
    private final DirtyChecker<V> dirtyChecker;

    public ViewChangeModel(ManagedViewTypeImplementor<V> type, V object, DirtyChecker<V> dirtyChecker) {
        super(type, null);
        this.object = object;
        this.dirtyChecker = dirtyChecker;
    }

    @Override
    public V getInitialState() {
        if (object.$$_isNew()) {
            return null;
        } else {
            return object;
        }
    }

    @Override
    public V getCurrentState() {
        return object;
    }

    @Override
    public ChangeKind getKind() {
        if (object.$$_isDirty()) {
            if (dirtyChecker.getDirtyKind(object, object) != DirtyChecker.DirtyKind.NONE) {
                return ChangeKind.MUTATED;
            } else {
                return ChangeKind.NONE;
            }
        } else {
            return ChangeKind.NONE;
        }
    }

    @Override
    public boolean isDirty() {
        if (object.$$_isDirty()) {
            if (dirtyChecker.getDirtyKind(object, object) != DirtyChecker.DirtyKind.NONE) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isDirty(String attributePath) {
        return isDirty(type, object, object, dirtyChecker, attributePath);
    }

    @Override
    public boolean isChanged(String attributePath) {
        return isChanged(type, object, object, dirtyChecker, attributePath);
    }

    @Override
    public List<ChangeModel<?>> getDirtyChanges() {
        return getDirtyChanges(type, object, dirtyChecker);
    }

    @Override
    public <X> ChangeModel<X> get(String attributePath) {
        return get(type, object, dirtyChecker, attributePath);
    }

    @Override
    public <X> List<? extends ChangeModel<X>> getAll(String attributePath) {
        return getAll(type, object, dirtyChecker, attributePath);
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        return getChangeModel(object, methodAttribute, dirtyChecker);
    }
}
