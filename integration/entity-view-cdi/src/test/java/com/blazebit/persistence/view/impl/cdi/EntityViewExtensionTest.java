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

package com.blazebit.persistence.view.impl.cdi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.Test;

import com.blazebit.persistence.view.spi.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewExtensionTest {

    @Inject
    private EntityViewConfiguration config;

    @Test
    public void testInjection() throws Exception {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();
        container.getContextControl().startContexts();
        BeanProvider.injectFields(this);

        assertNotNull(config);
        assertFalse(config.getEntityViews().isEmpty());
        assertTrue(config.getEntityViews().contains(TestView.class));

        container.shutdown();
    }
}
