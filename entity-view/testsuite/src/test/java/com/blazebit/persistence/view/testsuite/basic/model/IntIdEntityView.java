/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(IntIdEntity.class)
public interface IntIdEntityView extends IdHolderView<Integer> {

}
