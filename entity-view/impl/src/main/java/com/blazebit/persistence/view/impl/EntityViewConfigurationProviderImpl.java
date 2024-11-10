/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewConfigurationProvider;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewConfigurationProviderImpl implements EntityViewConfigurationProvider {

    @Override
    public EntityViewConfiguration createConfiguration() {
        return new EntityViewConfigurationImpl();
    }

}
