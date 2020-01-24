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
 * @author Philipp Eder <p.eder@curecomp.com>
 * @since 24.01.2020
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
