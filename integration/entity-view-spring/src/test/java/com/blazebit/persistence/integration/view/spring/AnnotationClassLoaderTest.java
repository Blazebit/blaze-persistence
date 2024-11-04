/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring;

import com.blazebit.persistence.integration.view.spring.views.sub1.TestView1;
import com.blazebit.persistence.integration.view.spring.views.sub2.TestView2;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnotationClassLoaderTest.TestConfig.class
)
public class AnnotationClassLoaderTest {

    static final AtomicInteger trackedClassLoaderViews = new AtomicInteger();
    static final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();

    static {
        Thread.currentThread().setContextClassLoader(new TrackingClassLoader());
    }

    @AfterClass
    public static void resetClassLoader() {
        Thread.currentThread().setContextClassLoader(previousClassLoader);
    }

    @Inject
    private EntityViewConfiguration entityViewConfiguration;

    @Test
    public void testInjectionWithCustomLoader() {
        Set<Class<?>> entityViews = entityViewConfiguration.getEntityViews();
        Assert.assertEquals(2, entityViews.size());
        Assert.assertEquals(2, trackedClassLoaderViews.get());
        Assert.assertTrue(entityViewConfiguration.getEntityViews().contains(TestView1.class));
        Assert.assertTrue(entityViewConfiguration.getEntityViews().contains(TestView2.class));
    }

    @Configuration
    @EnableEntityViews
    static class TestConfig {
    }

    private static class TrackingClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (Objects.equals(TestView1.class.getName(), name) || Objects.equals(TestView2.class.getName(), name)) {
                trackedClassLoaderViews.incrementAndGet();
            }
            return super.loadClass(name);
        }
    }
}
