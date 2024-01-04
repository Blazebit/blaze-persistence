/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.pagination.model.DocumentAggregationView;
import com.blazebit.persistence.view.testsuite.pagination.model.DocumentViewInterface;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
// NOTE: DataNucleus apparently only allows `MAX(fieldReference)` but we use `MAX(1)` for type resolving internally
@Category({ NoDatanucleus.class })
public class AggregateViewPaginationTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(DocumentAggregationView.class);
    }
    
    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                Person o1 = new Person("pers1", 10);

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
    public void test() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class)
                .where("owner.age").ge(10L)
                .groupBy("intIdEntity.id", "owner.id");
        EntityViewSetting<DocumentAggregationView, PaginatedCriteriaBuilder<DocumentAggregationView>> settings = EntityViewSetting.create(DocumentAggregationView.class, doc1.getId(), 1);
        settings.addAttributeSorter("id", Sorters.ascending());
        List<DocumentAggregationView> page = evm.applySetting(settings, cb).getResultList();
        assertEquals(1, page.size());
    }
}
