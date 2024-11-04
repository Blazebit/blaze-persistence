/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;

/**
 *
 * @author Christian
 * @since 1.2.0
 */
public interface ComparisonFilter {
    
    public <T> T applyRestriction(RestrictionBuilder<T> rb, Object value);

    public <T> SubqueryInitiator<T> applySubquery(RestrictionBuilder<T> rb);
}
