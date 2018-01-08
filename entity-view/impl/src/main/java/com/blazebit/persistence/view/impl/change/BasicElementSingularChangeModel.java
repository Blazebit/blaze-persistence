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
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicElementSingularChangeModel<V> extends AbstractChangeModel<V, V> implements SingularChangeModel<V> {

    private final V current;
    private final DirtyChecker<V> dirtyChecker;

    public BasicElementSingularChangeModel(BasicTypeImpl<V> basicType, V current, DirtyChecker<V> dirtyChecker) {
        super(null, basicType);
        this.current = current;
        this.dirtyChecker = dirtyChecker;
    }

    @Override
    public V getInitialState() {
        return null;
    }

    @Override
    public V getCurrentState() {
        return current;
    }

    @Override
    public ChangeKind getKind() {
        if (dirtyChecker != null) {
            if (dirtyChecker.getDirtyKind(null, current) != DirtyChecker.DirtyKind.NONE) {
                return ChangeKind.MUTATED;
            }
            return ChangeKind.NONE;
        } else {
            return ChangeKind.NONE;
        }
    }

    @Override
    public boolean isDirty() {
        if (dirtyChecker != null) {
            if (dirtyChecker.getDirtyKind(null, current) != DirtyChecker.DirtyKind.NONE) {
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
        throw illegalDereference();
    }

    @Override
    public boolean isChanged(String attributePath) {
        throw illegalDereference();
    }

    @Override
    public List<ChangeModel<?>> getDirtyChanges() {
        return Collections.emptyList();
    }

    @Override
    public <X> ChangeModel<X> get(String attributePath) {
        throw illegalDereference();
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        throw illegalDereference();
    }

    @Override
    public <X> List<? extends ChangeModel<X>> getAll(String attributePath) {
        throw illegalDereference();
    }

    @Override
    public <X> SingularChangeModel<X> get(SingularAttribute<V, X> attribute) {
        throw illegalDereference();
    }

    @Override
    public <E, C extends Collection<E>> PluralChangeModel<C, E> get(PluralAttribute<V, C, E> attribute) {
        throw illegalDereference();
    }

    @Override
    public <K, V1> MapChangeModel<K, V1> get(MapAttribute<V, K, V1> attribute) {
        throw illegalDereference();
    }

    private IllegalStateException illegalDereference() {
        return new IllegalStateException("Illegal dereference of basic type attribute!");
    }
}
