/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.hateoas.view;

import com.blazebit.persistence.examples.spring.hateoas.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingParameter;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {

    PersonSimpleView getOwner();

    @MappingParameter("test")
    String getTest();

}
