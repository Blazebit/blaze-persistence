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
public class ViewSingularChangeModel<V> extends AbstractSingularChangeModel<V> {

    private final V initial;
    private final DirtyStateTrackable current;
    private final DirtyChecker<V> dirtyChecker;

    public ViewSingularChangeModel(ManagedViewTypeImplementor<V> type, V initial, DirtyStateTrackable current, DirtyChecker<V> dirtyChecker) {
        super(type, null);
        this.initial = initial;
        this.current = current;
        this.dirtyChecker = dirtyChecker;
    }

    @Override
    public V getInitialState() {
        return initial;
    }

    @Override
    public V getCurrentState() {
        return (V) current;
    }

    @Override
    public ChangeKind getKind() {
        if (current == null) {
            if (initial == null) {
                return ChangeKind.NONE;
            }
            return ChangeKind.UPDATED;
        } else if (initial == current) {
            if (current.$$_isDirty()) {
                if (dirtyChecker.getDirtyKind(initial, (V) current) != DirtyChecker.DirtyKind.NONE) {
                    return ChangeKind.MUTATED;
                } else {
                    return ChangeKind.NONE;
                }
            } else {
                return ChangeKind.NONE;
            }
        } else {
            if (initial == null) {
                return ChangeKind.UPDATED;
            }
            if (current.$$_isDirty() || dirtyChecker.getDirtyKind(initial, (V) current) != DirtyChecker.DirtyKind.NONE) {
                return ChangeKind.MUTATED;
            } else {
                return ChangeKind.NONE;
            }
        }
    }

    @Override
    public boolean isDirty() {
        if (current == null) {
            if (initial == null) {
                return false;
            }
            return true;
        } else if (initial == current) {
            if (!current.$$_isDirty()) {
                return false;
            } else if (dirtyChecker.getDirtyKind(initial, (V) current) != DirtyChecker.DirtyKind.NONE) {
                return true;
            } else {
                return false;
            }
        } else if (dirtyChecker.getDirtyKind(initial, (V) current) != DirtyChecker.DirtyKind.NONE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isDirty(String attributePath) {
        return isDirty(type, initial, current, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker, attributePath);
    }

    @Override
    public boolean isChanged(String attributePath) {
        return isChanged(type, initial, current, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker, attributePath);
    }

    @Override
    public List<ChangeModel<?>> getDirtyChanges() {
        return getDirtyChanges(type, current, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker);
    }

    @Override
    public <X> ChangeModel<X> get(String attributePath) {
        return get(type, current, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker, attributePath);
    }

    @Override
    public <X> List<? extends ChangeModel<X>> getAll(String attributePath) {
        return getAll(type, current, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker, attributePath);
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        return getChangeModel(current, methodAttribute, (DirtyChecker<? extends DirtyStateTrackable>) dirtyChecker);
    }
}
