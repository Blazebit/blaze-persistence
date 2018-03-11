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

package com.blazebit.persistence.view.testsuite.basic;

import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonDuplicateCollectionUsageValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonInvalidMappingValidationView;
import com.blazebit.persistence.view.testsuite.basic.model.PersonValidationView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ValidationTest extends AbstractEntityViewTest {

    @Test
    public void testValidation() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentValidationView.class);
        cfg.addEntityView(PersonValidationView.class);
        cfg.createEntityViewManager(cbf);
    }

    @Test
    public void testValidationDuplicateCollection() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PersonDuplicateCollectionUsageValidationView.class);
        
        try {
            cfg.createEntityViewManager(cbf);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains("'ownedDocuments'")) {
                throw ex;
            }
        }
    }

    @Test
    public void testValidationInvalidMapping() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PersonInvalidMappingValidationView.class);
        
        try {
            cfg.createEntityViewManager(cbf);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains(PersonInvalidMappingValidationView.class.getSimpleName() + ".getName") || !ex.getCause().getMessage().contains("'defaultContact'")) {
                throw ex;
            }
        }
    }
}
