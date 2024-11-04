/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.flat.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@EntityView(NameObject.class)
public interface NameObjectView {

    String getPrimaryName();

    String getSecondaryName();
}
