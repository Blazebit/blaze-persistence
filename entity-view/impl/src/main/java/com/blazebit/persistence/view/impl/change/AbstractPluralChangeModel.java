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
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPluralChangeModel<C, V, D extends DirtyChecker<C>> extends AbstractChangeModel<C, V> implements PluralChangeModel<C, V> {

    protected final C initial;
    protected final C current;
    protected final D pluralDirtyChecker;
    private Map<Object, AbstractChangeModel<?, ?>> changeModels;

    public AbstractPluralChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, C initial, C current, D pluralDirtyChecker) {
        super(type, basicType);
        this.initial = initial;
        this.current = current;
        this.pluralDirtyChecker = pluralDirtyChecker;
    }

    @Override
    public ChangeKind getKind() {
        if (current == null) {
            if (initial == null) {
                return ChangeKind.NONE;
            }
            return ChangeKind.UPDATED;
        } else if (initial == current) {
            if (pluralDirtyChecker.getDirtyKind(initial, current) != DirtyChecker.DirtyKind.NONE) {
                return ChangeKind.MUTATED;
            } else {
                return ChangeKind.NONE;
            }
        } else if (pluralDirtyChecker.getDirtyKind(initial, current) != DirtyChecker.DirtyKind.NONE) {
            return ChangeKind.MUTATED;
        } else {
            return ChangeKind.NONE;
        }
    }

    @Override
    public boolean isDirty() {
        return getKind() != ChangeKind.NONE;
    }

    @Override
    public C getInitialState() {
        return initial;
    }

    @Override
    public C getCurrentState() {
        return current;
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        return null;
    }

    protected abstract <X> List<? extends ChangeModel<X>> getAll(AbstractMethodAttribute<?, ?> methodAttribute);

    @Override
    public <X> List<SingularChangeModel<X>> get(SingularAttribute<V, X> attribute) {
        return (List<SingularChangeModel<X>>) (List<?>) getAll(getMutableAttribute(attribute));
    }

    @Override
    public <E, C extends Collection<E>> List<PluralChangeModel<C, E>> get(PluralAttribute<V, C, E> attribute) {
        return (List<PluralChangeModel<C, E>>) (List<?>) getAll(getMutableAttribute(attribute));
    }

    @Override
    public <K, E> List<MapChangeModel<K, E>> get(MapAttribute<V, K, E> attribute) {
        return (List<MapChangeModel<K, E>>) (List<?>) this.getAll(getMutableAttribute(attribute));
    }

    @SuppressWarnings("unchecked")
    protected final <X> AbstractChangeModel<X, X> getObjectChangeModel(Type<X> elementType, X initial, X element, DirtyChecker<X> dirtyChecker) {
        if (changeModels == null) {
            this.changeModels = new IdentityHashMap<>();
        }

        AbstractChangeModel<X, X> changeModel = (AbstractChangeModel<X, X>) changeModels.get(element);
        if (changeModel != null && changeModel.getInitialState() == initial && changeModel.getCurrentState() == element) {
            return changeModel;
        }

        if (elementType instanceof ManagedViewType<?>) {
            if (element instanceof DirtyStateTrackable) {
                changeModel = (AbstractSingularChangeModel<X>) new ViewSingularChangeModel<>((ManagedViewTypeImplementor<Object>) elementType, initial, (DirtyStateTrackable) element, (DirtyChecker<Object>) dirtyChecker);
            } else {
                changeModel = new ImmutableSingularChangeModel<>((ManagedViewTypeImplementor<X>) elementType, null, initial, element);
            }
        } else {
            changeModel = new BasicSingularChangeModel<>((BasicTypeImpl<X>) elementType, initial, element, dirtyChecker);
        }
        this.changeModels.put(element, changeModel);
        return changeModel;
    }

}
