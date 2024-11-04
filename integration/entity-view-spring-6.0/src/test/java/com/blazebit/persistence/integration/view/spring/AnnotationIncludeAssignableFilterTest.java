/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.spring;

import com.blazebit.persistence.integration.view.spring.views.SomeSuperInterface;
import com.blazebit.persistence.integration.view.spring.views.sub1.TestView1;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jakarta.inject.Inject;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnotationIncludeAssignableFilterTest.TestConfig.class)
public class AnnotationIncludeAssignableFilterTest {
    @Inject
    private EntityViewConfiguration entityViewConfiguration;

    @Test
    public void testInjection() {
        Set<Class<?>> entityViews = entityViewConfiguration.getEntityViews();
        Assert.assertEquals(1, entityViews.size());
        Assert.assertTrue(entityViewConfiguration.getEntityViews().contains(TestView1.class));
    }

    @Configuration
    @EnableEntityViews(includeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SomeSuperInterface.class)
    })
    static class TestConfig {
    }
}
