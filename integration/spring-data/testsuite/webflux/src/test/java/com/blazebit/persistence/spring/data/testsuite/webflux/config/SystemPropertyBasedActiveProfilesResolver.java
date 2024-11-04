/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.config;

import org.springframework.test.context.support.DefaultActiveProfilesResolver;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SystemPropertyBasedActiveProfilesResolver extends DefaultActiveProfilesResolver {

    private static final String ACTIVE_PROFILES_PROPERTY = "activeProfiles";
    private static final String DEFAULT_ACTIVE_PROFILES = "hibernate";

    @Override
    public String[] resolve(Class<?> testClass) {
        return System.getProperty(ACTIVE_PROFILES_PROPERTY, DEFAULT_ACTIVE_PROFILES).split(",");
    }
}
