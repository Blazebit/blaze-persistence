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
public class ClassObjectBuilder<T> implements ObjectBuilder<T> {

    private final Class<T> clazz;
    private Constructor<T> constructor;

    public ClassObjectBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    private Constructor<T> getConstructor(Object[] tuple) {
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<T> matchingConstr = null;
        for (Constructor<?> constr : constructors) {
            Class<?>[] paramTypes = constr.getParameterTypes();
            if (paramTypes.length == tuple.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (tuple[i] != null && !paramTypes[i].isAssignableFrom(tuple[i].getClass())) {
                        match = false;
                        break;
                    }
                }
                if (match == true) {
                    if (matchingConstr != null) {
                        throw new RuntimeException("Multiple constructors matching");
                    }
                    return (Constructor<T>) constr;
                }

            }
        }

        throw new RuntimeException("No matching constructor");
    }

    @Override
    public T build(Object[] tuple) {
        if (constructor == null) {
            constructor = getConstructor(tuple);
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
