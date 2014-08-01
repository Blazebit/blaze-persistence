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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.Test;
import static  org.junit.Assert.*;

/**
 *
 * @author Christian
 */
public class EntityViewExtensionTest {
    
    @PersistenceUnit
    private EntityManagerFactory emf;
    @Inject
    private EntityViewManager evm;
    @Inject
    private CriteriaBuilderFactory cbf;
    
    @Test
    public void testInjection() throws Exception {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();
        container.getContextControl().startContexts();
        BeanProvider.injectFields(this);
            
        assertNotNull(evm);
        assertNotNull(TestEnricher.config);
        assertFalse(evm.getMetamodel().getViews().isEmpty());
        assertNotNull(evm.getMetamodel().view(TestView.class));
        // TODO: Issue #29
//        CriteriaBuilder<TestView> cb = cbf.from(emf.createEntityManager(), TestEntity.class, "t").select(TestView.class);
//        assertEquals("SELECT t.id FROM TestEntity t", cb.getQueryString());
        
        container.shutdown();
    }
}
