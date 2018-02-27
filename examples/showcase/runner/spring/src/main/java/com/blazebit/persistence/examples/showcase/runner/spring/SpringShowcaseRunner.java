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

package com.blazebit.persistence.examples.showcase.runner.spring;

import com.blazebit.persistence.examples.showcase.spi.Showcase;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
@EnableJpaRepositories(basePackages = "com.blazebit.persistence.examples",
        entityManagerFactoryRef = "myEmf",
        repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class)
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
