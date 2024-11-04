/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.cte.view;

import com.blazebit.persistence.examples.showcase.base.model.Cat;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(Cat.class)
public abstract class BasicCatView implements IdHolderView<Integer> {

    public abstract String getName();

    @Override
    public String toString() {
        return "Cat{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
