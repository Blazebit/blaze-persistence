/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.treat.entity.IntValueEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddableSub2;
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
 * @since 1.6.0
 */
public class CreatableAbstractEntityTest extends AbstractEntityViewTest {

    @Test
    public void tesValidationAbstractEntityMapping() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SingleTableBaseAbstractEntityMappingValidationView.class);

        try {
            cfg.createEntityViewManager(cbf);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains("@CreatableEntityView but refers to an abstract entity")) {
                throw ex;
            }
        }
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IntIdEntity.class,
                IntValueEmbeddable.class,
                SingleTableBase.class,
                SingleTableSub1.class,
                SingleTableSub2.class,
                SingleTableEmbeddable.class,
                SingleTableEmbeddableSub1.class,
                SingleTableEmbeddableSub2.class
        };
    }
}