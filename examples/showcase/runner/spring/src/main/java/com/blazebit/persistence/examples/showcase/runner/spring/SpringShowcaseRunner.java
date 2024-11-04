/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.spring;

import com.blazebit.persistence.examples.showcase.spi.Showcase;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Configuration
@ImportResource({
    "classpath:/com/blazebit/persistence/examples/showcase/runner/spring/application-config.xml",
    "classpath:/META-INF/spring-beans.xml"})
@ComponentScan
@EnableEntityViews("com.blazebit.persistence.examples")
@EnableBlazeRepositories(basePackages = "com.blazebit.persistence.examples",
    entityManagerFactoryRef = "myEmf")
public class SpringShowcaseRunner implements CommandLineRunner {

    @Inject
    private Showcase showcase;

    @Inject
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Inject
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        autowireCapableBeanFactory.autowireBean(showcase);
        showcase.run();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringShowcaseRunner.class).close();
    }
}
