/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.testsuite.entity.TablePerClassBase;
import com.blazebit.persistence.testsuite.entity.TablePerClassSub1;
import com.blazebit.persistence.testsuite.entity.TablePerClassSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Christian beikov
 * @since 1.2.0
 */
public class Issue336Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            TablePerClassBase.class,
            TablePerClassSub1.class,
            TablePerClassSub2.class
        };
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                TablePerClassSub1 entity1 = new TablePerClassSub1(1L, "test1");
                TablePerClassSub2 entity2 = new TablePerClassSub2(2L, "test2");
                em.persist(entity1);
                em.persist(entity2);
            }
        });
    }

    @Test
    public void testBuild() throws Exception {
        CriteriaBuilder<TablePerClassBase> cb = cbf.create(em, TablePerClassBase.class);

        List<TablePerClassBase> result = cb.getResultList();
        Assert.assertEquals(2, result.size());
    }

}
