/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.TPCBase;
import com.blazebit.persistence.testsuite.entity.TPCSub1;
import com.blazebit.persistence.testsuite.entity.TPCSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * This test is for issue #336
 *
 * @author Christian beikov
 * @since 1.2.0
 */
public class TablePerClassTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            TPCBase.class,
            TPCSub1.class,
            TPCSub2.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                TPCSub1 entity1 = new TPCSub1(1L, "test1");
                TPCSub2 entity2 = new TPCSub2(2L, "test2");
                em.persist(entity1);
                em.persist(entity2);
            }
        });
    }

    @Test
    public void buildingEntityMetamodelForTablePerClassEntitiesWorks() throws Exception {
        CriteriaBuilder<TPCBase> cb = cbf.create(em, TPCBase.class);

        List<TPCBase> result = cb.getResultList();
        Assert.assertEquals(2, result.size());
    }

}
