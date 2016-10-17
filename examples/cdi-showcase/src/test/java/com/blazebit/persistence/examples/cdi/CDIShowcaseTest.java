/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.examples.cdi;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.*;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public class CDIShowcaseTest {

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
        // just run the Application's run method - if no exception occur, we assume that it works
        Application application = BeanProvider.getContextualReference(Application.class);
        application.run();
    }


}
