/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.views;

import com.blazebit.persistence.integration.graphql.entities.Cat;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Cat.class)
public interface CatView extends AnimalView {
}
