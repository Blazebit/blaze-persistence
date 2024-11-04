/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.cascade.simple.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveFamily;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@EntityView(PrimitiveFamily.class)
public interface FamilyIdView {

    @IdMapping
    long getId();

}
