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
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.model.DocumentViewInterface;
import com.blazebit.persistence.view.model.DocumentViewAbstractClass;
import com.blazebit.persistence.view.model.PersonView1;
import org.junit.BeforeClass;

/**
 *
 * @author cpbec
 */
public class AbstractEntityViewPersistenceTest extends AbstractPersistenceTest {
    
    protected static EntityViewManager evm;
    
    @BeforeClass
    public static void initEvm() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentViewInterface.class);
        cfg.addEntityView(DocumentViewAbstractClass.class);
        cfg.addEntityView(PersonView1.class);
        evm = cfg.createEntityViewManager();
    }
}
