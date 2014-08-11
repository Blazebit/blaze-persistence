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

package com.blazebit.persistence.view.impl.cdi;

import com.blazebit.persistence.view.EntityViewManager;
import javax.inject.Inject;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.Test;
import static  org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewExtensionTest {
    @Inject
    private EntityViewManager evm;
    
    @Test
    public void testInjection() throws Exception {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();
        container.getContextControl().startContexts();
        BeanProvider.injectFields(this);
            
        assertNotNull(evm);
        assertFalse(evm.getMetamodel().getViews().isEmpty());
        assertNotNull(evm.getMetamodel().view(TestView.class));
        
        container.shutdown();
    }
}
