/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.spring;

import com.blazebit.persistence.examples.showcase.spi.Showcase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.inject.Inject;
import java.util.ServiceLoader;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
@ContextConfiguration(classes = SpringShowcaseRunner.class)
public class SpringShowcaseTest {

    private TestContextManager testContextManager;
    private final Showcase showcase;

    public SpringShowcaseTest(Showcase showcase) {
        this.showcase = showcase;
    }

    @Parameterized.Parameters
    public static Iterable<Showcase> data() {
        return ServiceLoader.load(Showcase.class);
    }

    @Before
    public void setUpContext() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
        testContextManager.registerTestExecutionListeners(new DirtiesContextTestExecutionListener());

    }

    @After
    public void tearDownContext() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
        testContextManager.getTestContext().setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);
    }

    @Inject
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Test
    public void simpleApplicationTest() {
        autowireCapableBeanFactory.autowireBean(showcase);
        showcase.run();
    }

}
