/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.subselectsubset.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
@EntityView(NameObject.class)
public interface NameObjectView {
    
    public String getPrimaryName();

}
