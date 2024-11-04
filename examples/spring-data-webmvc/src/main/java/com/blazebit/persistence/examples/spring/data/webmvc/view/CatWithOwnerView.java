/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webmvc.view;

import com.blazebit.persistence.examples.spring.data.webmvc.model.Cat;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {

    PersonSimpleView getOwner();

}
