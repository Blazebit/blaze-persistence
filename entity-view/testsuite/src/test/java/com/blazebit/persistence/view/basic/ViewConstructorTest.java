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
package com.blazebit.persistence.view.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.basic.model.DocumentViewAbstractClass;
import com.blazebit.persistence.view.basic.model.DocumentViewInterface;
import com.blazebit.persistence.view.basic.model.DocumentViewWithMissingMappingParameter;
import com.blazebit.persistence.view.basic.model.PersonView;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import java.util.List;
import javax.persistence.EntityTransaction;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class ViewConstructorTest extends AbstractEntityViewTest {

    @Test
    public void testAbstractClass() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentViewWithMissingMappingParameter.class);
        
        try {
            cfg.createEntityViewManager();
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
}
