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

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;

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
