/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class BlazeRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableBlazeRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new BlazeRepositoryConfigExtension();
    }
}
