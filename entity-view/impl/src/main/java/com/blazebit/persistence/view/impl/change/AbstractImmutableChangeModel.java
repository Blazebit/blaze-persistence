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
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;

import java.util.Objects;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractImmutableChangeModel<C, E> extends AbstractChangeModel<C, E> {

    protected final C initial;
    protected final C current;

    public AbstractImmutableChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType, C initial, C current) {
        super(type, basicType);
        this.initial = initial;
        this.current = current;
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
    public ChangeKind getKind() {
        return Objects.equals(initial, current) ? ChangeKind.NONE : ChangeKind.UPDATED;
    }

    @Override
    public boolean isDirty() {
        return !Objects.equals(initial, current);
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        Object value = methodAttribute.getValue(current);
        return getImmutableChangeModel(methodAttribute, value, value);
    }

}
