/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@UpdatableEntityView
@EntityView(Cat.class)
public interface CatUpdateView extends CatSimpleView {

    void setName(String name);

    Integer getAge();

    void setAge(Integer age);
}
