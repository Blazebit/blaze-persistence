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
public class EmptyMapChangeModel<K, V> extends AbstractEmptyPluralChangeModel<Map<K, V>, V> implements MapChangeModel<K, V> {

    private final ManagedViewTypeImplementor<K> keyType;
    private final BasicTypeImpl<K> basicKeyType;

    public EmptyMapChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, ManagedViewTypeImplementor<K> keyType, BasicTypeImpl<K> basicKeyType) {
        super(type, basicType);
        this.keyType = keyType;
        this.basicKeyType = basicKeyType;
    }

    @Override
    public boolean isKeyDirty(String attributePath) {
        validateAttributePath(keyType, attributePath);
        return false;
    }

    @Override
    public boolean isKeyChanged(String attributePath) {
        validateAttributePath(keyType, attributePath);
        return false;
    }

    @Override
    public <X> List<? extends ChangeModel<X>> keyGet(String attributePath) {
        validateAttributePath(keyType, attributePath);
        return Collections.emptyList();
    }

    @Override
    public <X> List<SingularChangeModel<X>> keyGet(SingularAttribute<K, X> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <E, C extends Collection<E>> List<PluralChangeModel<C, E>> keyGet(PluralAttribute<K, C, E> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <K1, V1> List<MapChangeModel<K1, V1>> keyGet(MapAttribute<K, K1, V1> attribute) {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<K>> getKeyChanges() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<K>> getAddedKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<K>> getRemovedKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<K>> getMutatedKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<?>> getObjectChanges() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<?>> getAddedObjects() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<?>> getRemovedObjects() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<?>> getMutatedObjects() {
        return Collections.emptyList();
    }
}
