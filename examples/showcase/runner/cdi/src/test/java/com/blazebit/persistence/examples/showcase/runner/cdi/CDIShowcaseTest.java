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
