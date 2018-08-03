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

package com.blazebit.persistence.view.testsuite.pagination;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.pagination.model.DocumentViewInterface;

/**
 *
 * @author Moritz Becker
 * @since 1.1.0
 */
public class BasicViewPaginationTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentViewInterface.class);
        evm = cfg.createEntityViewManager(cbf);
    }
    
    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                Person o1 = new Person("pers1");

                doc1.setAge(10);
                doc1.setOwner(o1);

                doc1.getContacts().put(1, o1);
                doc1.getContacts2().put(2, o1);

                em.persist(o1);
                em.persist(doc1);
                em.persist(new Document("doc2", o1));
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
    }
    
    private Document doc1;
    
    @Test
    public void testPaginationWithNegativeFirstResult() {
        EntityViewSetting<DocumentViewInterface, PaginatedCriteriaBuilder<DocumentViewInterface>> settings = EntityViewSetting.create(DocumentViewInterface.class, doc1.getId(), 1);
        List<DocumentViewInterface> page = evm.applySetting(settings, cbf.create(em, Document.class).orderByAsc("id")).getResultList();
        assertEquals(1, page.size());
    }
}
