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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.TPCBase;
import com.blazebit.persistence.testsuite.entity.TPCSub1;
import com.blazebit.persistence.testsuite.entity.TPCSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian beikov
 * @since 1.2.0
 */
public class SelectPolymorphicTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IdHolderCTE.class,
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

    // NOTE: MySQL has no CTE support
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testSelectTablePerClassWithCte() throws Exception {
        CriteriaBuilder<TPCBase> cb = cbf.create(em, TPCBase.class, "t")
                .with(IdHolderCTE.class)
                    .from(TPCBase.class, "t")
                    .bind("id").select("t.id")
                .end()
                .where("id").in()
                    .from(IdHolderCTE.class, "cte")
                    .select("cte.id")
                .end();

        String expected = "WITH IdHolderCTE(id) AS(\n" +
                "SELECT t.id FROM TPCBase t\n" +
                ")\n" +
                "SELECT t FROM TPCBase t WHERE t.id IN (" +
                    "SELECT cte.id FROM IdHolderCTE cte" +
                ")";

        assertEquals(expected, cb.getQueryString());

        List<TPCBase> result = cb.getResultList();
        Assert.assertEquals(2, result.size());
    }

}
