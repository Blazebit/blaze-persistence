/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.blazebit.persistence.spring.data.webmvc.impl.BlazePersistenceWebConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Configuration
public class BlazePersistenceAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

    @Override
    public boolean[] match(String[] classNames, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] shouldImport = new boolean[classNames.length];
        String excludeName = BlazePersistenceWebConfiguration.class.getName();
        for (int i = 0; i < classNames.length; i++) {
            shouldImport[i] = !excludeName.equals(classNames[i]);
        }
        return shouldImport;
    }
}
