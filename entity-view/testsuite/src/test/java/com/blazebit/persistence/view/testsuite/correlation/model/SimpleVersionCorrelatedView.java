/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@EntityView(Version.class)
public interface SimpleVersionCorrelatedView {
    
    @IdMapping
    public Long getId();

}
