/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.view.EntityViewManager;

/**
 * Resolve the EntityViewManager used for a specific repository.
 * Only necessary if there are multiple EntityViewManagers with different qualifiers.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityViewManagerResolver {

    /**
     * @return the resolved EntityViewManager
     */
    EntityViewManager resolveEntityViewManager();
}
