/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub2;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.SingleTableBaseAbstractEntityMappingValidationView;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Philipp Eder
 * @since 1.4.1
 */
public class AbstractMappingTest extends AbstractEntityViewTest {

    @Test
    public void tesValidationAbstractEntityMapping() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SingleTableBaseAbstractEntityMappingValidationView.class);

        try {
            cfg.createEntityViewManager(cbf);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains("may not map abstract Entities.")) {
                throw ex;
            }
        }
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IntIdEntity.class,
                SingleTableBase.class,
                SingleTableSub1.class,
                SingleTableSub2.class
        };
    }
}
