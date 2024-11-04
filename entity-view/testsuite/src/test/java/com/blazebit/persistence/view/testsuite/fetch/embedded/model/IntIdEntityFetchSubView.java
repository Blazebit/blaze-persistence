/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(IntIdEntity.class)
public interface IntIdEntityFetchSubView extends IntIdEntitySimpleSubView {
    String getName();
}
