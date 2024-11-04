/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface ConstrainedSelectBuilder extends SelectBuilder<Object> {

    public FullQueryBuilder<?, ?> getQueryBuilder();
}
