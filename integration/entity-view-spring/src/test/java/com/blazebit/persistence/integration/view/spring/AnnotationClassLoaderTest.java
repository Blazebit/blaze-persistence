/*
 * Copyright 2014 - 2021 Blazebit.
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
package com.blazebit.persistence.integration.view.spring;

import com.blazebit.persistence.integration.view.spring.views.sub1.TestView1;
import com.blazebit.persistence.integration.view.spring.views.sub2.TestView2;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.lang.reflect.Method;
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
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ClassLoader.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (method.getName().contains("loadClass") && args.length == 1) {
                    if (Objects.equals(TestView1.class.getName(), args[0]) || Objects.equals(TestView2.class.getName(), args[0])) {
                        trackedClassLoaderViews.incrementAndGet();
                    }
                }
                return proxy.invokeSuper(object, args);
            }
        });
        ClassLoader proxy = (ClassLoader) enhancer.create();
        Thread.currentThread().setContextClassLoader(proxy);
    }

    @AfterClass
    public static void verify() {
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
}
