/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.PersonForEntityKeyMaps;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(PersonForEntityKeyMaps.class)
public interface SubviewPersonForEntityKeyMapsView {
    
    @IdMapping
    public Long getId();

    public String getName();

}
