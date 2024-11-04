/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc;

import com.blazebit.persistence.spring.data.testsuite.webmvc.config.CustomLocalContainerEntityManagerFactoryBean;
import com.blazebit.persistence.spring.data.testsuite.webmvc.config.SystemPropertyBasedActiveProfilesResolver;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.MutablePersistenceUnitInfo;
import org.junit.After;
import org.junit.Before;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@ActiveProfiles(resolver = SystemPropertyBasedActiveProfilesResolver.class)
public abstract class AbstractSpringTest extends AbstractPersistenceTest {

    private TestContextManager testContextManager;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                Document.class,
                Person.class
        };
    }

    @Override
    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        // No-op
    }

    @Before
    public void setUpContext() throws Exception {
        // We have to close the EM and EMF constructed by the abstraction
        cleanDatabase();
        this.em.getTransaction().rollback();
        this.em.close();
        emf.close();
        emf = null;
        this.em = null;
        this.cbf = null;
        this.jpaProvider = null;
        this.dbmsDialect = null;

        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        CustomLocalContainerEntityManagerFactoryBean.properties = createProperties("none");
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

}
