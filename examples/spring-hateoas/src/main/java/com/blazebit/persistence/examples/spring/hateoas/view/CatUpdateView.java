/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.hateoas.view;

import com.blazebit.persistence.examples.spring.hateoas.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(Cat.class)
public interface CatUpdateView extends CatSimpleView {

    void setName(String name);

    Integer getAge();

    void setAge(Integer age);
}
