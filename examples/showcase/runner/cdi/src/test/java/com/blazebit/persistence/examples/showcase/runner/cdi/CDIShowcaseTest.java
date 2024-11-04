/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.runner.cdi;

import com.blazebit.persistence.examples.showcase.spi.Showcase;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.enterprise.context.ApplicationScoped;
import java.util.ServiceLoader;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
public class CDIShowcaseTest {

    final Showcase showcase;

    public CDIShowcaseTest(Showcase showcase) {
        this.showcase = showcase;
    }

    @Parameterized.Parameters
    public static Iterable<Showcase> data() {
        return ServiceLoader.load(Showcase.class);
    }

    @BeforeClass
    public static void startContainer() {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
    }

    @AfterClass
    public static void stopContainer() {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.shutdown();
    }

    @Before
    public void startContexts() {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(ApplicationScoped.class);
    }

    @After
    public void stopContexts() {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.stopContext(ApplicationScoped.class);
    }

    @Test
    public void simpleApplicationTest() {
        BeanProvider.injectFields(showcase).run();
    }


}
