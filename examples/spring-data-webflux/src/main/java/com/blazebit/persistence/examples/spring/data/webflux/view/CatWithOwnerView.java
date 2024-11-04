/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webflux.view;

import com.blazebit.persistence.examples.spring.data.webflux.model.Cat;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {

    PersonSimpleView getOwner();

}
