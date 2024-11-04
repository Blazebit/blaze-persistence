/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.resolver;

import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import com.blazebit.persistence.deltaspike.data.testsuite.qualifier.Restricted;
import com.blazebit.persistence.view.EntityViewManager;

import javax.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class RestrictedEntityViewManagerResolver implements EntityViewManagerResolver {

    @Inject
    @Restricted
    private EntityViewManager evm;

    @Override
    public EntityViewManager resolveEntityViewManager() {
        return evm;
    }
}
