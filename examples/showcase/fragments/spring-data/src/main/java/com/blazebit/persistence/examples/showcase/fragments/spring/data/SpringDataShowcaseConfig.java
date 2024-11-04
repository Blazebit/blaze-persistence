/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.spring.data;

import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import org.springframework.context.annotation.Configuration;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Configuration
@EnableBlazeRepositories(
        basePackages = "com.blazebit.persistence.examples.showcase.fragments.spring.data.repository",
        entityManagerFactoryRef = "myEmf")
public class SpringDataShowcaseConfig {
}
