/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
