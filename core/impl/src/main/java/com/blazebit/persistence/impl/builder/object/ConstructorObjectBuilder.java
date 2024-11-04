/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.object;

import java.lang.reflect.Constructor;
import java.util.List;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ConstructorObjectBuilder<T> implements ObjectBuilder<T> {

    private final Constructor<T> constructor;

    public ConstructorObjectBuilder(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T build(Object[] tuple) {
        if (constructor.getParameterTypes().length != tuple.length) {
            throw new RuntimeException("Constructor expects " + constructor.getParameterTypes().length + " arguments but " + tuple.length
                + " arguments were queried");
        }
        try {
            return constructor.newInstance(tuple);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> buildList(List<T> list) {
        return list;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
    }

}
