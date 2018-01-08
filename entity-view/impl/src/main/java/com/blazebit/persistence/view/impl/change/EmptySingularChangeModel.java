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
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EmptySingularChangeModel<V> extends AbstractChangeModel<V, V> implements SingularChangeModel<V> {

    public EmptySingularChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType) {
        super(type, basicType);
    }

    @Override
    public V getInitialState() {
        return null;
    }

    @Override
    public V getCurrentState() {
        return null;
    }

    @Override
    public ChangeKind getKind() {
        return ChangeKind.NONE;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isDirty(String attributePath) {
        validateAttributePath(type, attributePath);
        return false;
    }

    @Override
    public boolean isChanged(String attributePath) {
        validateAttributePath(type, attributePath);
        return false;
    }

    @Override
    public <X> ChangeModel<X> get(String attributePath) {
        return getEmptyChangeModel(type, attributePath, attributePath.split("\\."), 0);
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        return getEmptyChangeModel(methodAttribute);
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
    public <X> List<? extends ChangeModel<X>> getAll(String attributePath) {
        validateAttributePath(type, attributePath);
        return Collections.emptyList();
    }

    @Override
    public List<ChangeModel<?>> getDirtyChanges() {
        return Collections.emptyList();
    }
}
