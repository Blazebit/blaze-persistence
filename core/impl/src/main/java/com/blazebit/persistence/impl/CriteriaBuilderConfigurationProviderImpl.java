/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import java.util.logging.Logger;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.CriteriaBuilderConfigurationProvider;
import com.blazebit.persistence.spi.PackageOpener;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CriteriaBuilderConfigurationProviderImpl implements CriteriaBuilderConfigurationProvider {

    private static final Logger LOG = Logger.getLogger(CriteriaBuilderConfigurationProviderImpl.class.getName());
    private static volatile boolean versionLogged = false;

    @Override
    public CriteriaBuilderConfiguration createConfiguration() {
        return createConfiguration(PackageOpener.NOOP);
    }

    @Override
    public CriteriaBuilderConfiguration createConfiguration(PackageOpener packageOpener) {
        if (!versionLogged) {
            LOG.info("Blaze-Persistence version: " + Version.printVersion());
            versionLogged = true;
        }

        return new CriteriaBuilderConfigurationImpl(packageOpener);
    }

}
