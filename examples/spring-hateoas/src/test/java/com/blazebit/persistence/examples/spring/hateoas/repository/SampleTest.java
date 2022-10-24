/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.examples.spring.hateoas.repository;

import com.blazebit.persistence.examples.spring.hateoas.model.Cat;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SampleTest.TestConfig.class, HateoasAwareSpringDataWebConfiguration.class})
public class SampleTest extends AbstractSampleTest {

    @Autowired
    private CatRepository catRepository;

    @Test
    public void sampleTest() {
        final Page<Cat> page = catRepository.findAll(null, null);
        Assert.assertEquals(6, page.getNumberOfElements());
    }

    @Configuration
    @ComponentScan("com.blazebit.persistence.examples.spring.hateoas")
    @ImportResource({"/META-INF/test-config.xml"})
    @EnableEntityViews(basePackages = { "com.blazebit.persistence.examples.spring.hateoas.view"})
    @EnableBlazeRepositories(
            basePackages = "com.blazebit.persistence.examples.spring.hateoas.repository")
    static class TestConfig {
    }
}
