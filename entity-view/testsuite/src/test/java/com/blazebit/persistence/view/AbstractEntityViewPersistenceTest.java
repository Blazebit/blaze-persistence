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

package com.blazebit.persistence.view;

import com.blazebit.persistence.AbstractPersistenceTest;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.model.DocumentView1;
import com.blazebit.persistence.view.model.DocumentView2;
import com.blazebit.persistence.view.model.PersonView1;
import org.junit.BeforeClass;

/**
 *
 * @author cpbec
 */
public class AbstractEntityViewPersistenceTest extends AbstractPersistenceTest {
    
    protected static EntityViewManagerFactory evmf;
    protected static EntityViewManager evm;
    
    @BeforeClass
    public static void initEvm() {
        EntityViewConfiguration cfg = new EntityViewConfiguration();
        cfg.addEntityView(DocumentView1.class);
        cfg.addEntityView(DocumentView2.class);
        cfg.addEntityView(PersonView1.class);
        evmf = cfg.createEntityViewManagerFactory();
        evm = evmf.createEntityViewManager(em);
    }
}
