/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class LateAdditionalObjectBuilder implements ObjectBuilder<Object[]> {

    private final ObjectBuilder<Object[]> objectBuilder;
    private final ObjectBuilder<Object[]> additionalBuilder;
    private final boolean apply;

    public LateAdditionalObjectBuilder(ObjectBuilder<?> objectBuilder, ObjectBuilder<?> additionalBuilder, boolean apply) {
        this.objectBuilder = (ObjectBuilder<Object[]>) objectBuilder;
        this.additionalBuilder = (ObjectBuilder<Object[]>) additionalBuilder;
        this.apply = apply;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        if (apply) {
            additionalBuilder.applySelects(queryBuilder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] build(Object[] tuple) {
        return tuple;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> buildList(List<Object[]> list) {
        List<Object[]> objects = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            objects.add(objectBuilder.build(list.get(i)));
        }

        objects = objectBuilder.buildList(objects);

        for (int j = 0; j < objects.size(); j++) {
            objects.set(j, additionalBuilder.build(objects.get(j)));
        }
        return additionalBuilder.buildList(objects);
    }
}
