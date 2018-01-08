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
import java.util.Objects;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicSingularChangeModel<V> extends AbstractChangeModel<V, V> implements SingularChangeModel<V> {

    private final V initial;
    private final V current;
    private final boolean assumeDirty;
    private final DirtyChecker<V> dirtyChecker;

    public BasicSingularChangeModel(BasicTypeImpl<V> basicType, V initial, V current, DirtyChecker<V> dirtyChecker) {
        super(null, basicType);
        this.initial = initial;
        this.current = current;
        this.assumeDirty = basicType.getUserType().isMutable() && !basicType.getUserType().supportsDirtyChecking();
        this.dirtyChecker = dirtyChecker;
    }

    @Override
    public V getInitialState() {
        return initial;
    }

    @Override
    public V getCurrentState() {
        return current;
    }

    @Override
    public ChangeKind getKind() {
        if (initial != current) {
            if (dirtyChecker != null) {
                if (dirtyChecker.getDirtyKind(initial, current) != DirtyChecker.DirtyKind.NONE) {
                    return ChangeKind.UPDATED;
                }
                return ChangeKind.NONE;
            } else if (assumeDirty) {
                return ChangeKind.MUTATED;
            } else {
                return Objects.equals(initial, current) ? ChangeKind.NONE : ChangeKind.UPDATED;
            }
        } else {
            if (dirtyChecker != null) {
                if (dirtyChecker.getDirtyKind(initial, current) != DirtyChecker.DirtyKind.NONE) {
                    return ChangeKind.MUTATED;
                }
                return ChangeKind.NONE;
            } else if (assumeDirty) {
                return ChangeKind.MUTATED;
            } else {
                return Objects.equals(initial, current) ? ChangeKind.NONE : ChangeKind.UPDATED;
            }
        }
    }

    @Override
    public boolean isDirty() {
        return getKind() != ChangeKind.NONE;
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
