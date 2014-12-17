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
package com.blazebit.persistence.view.subquery;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.subquery.model.DocumentWithExpressionSubqueryView;
import com.blazebit.persistence.view.subquery.model.DocumentWithSubquery;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MappingSubqueryTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("doc1");
            doc2 = new Document("doc2");

            Person o1 = new Person("pers1");
            Person o2 = new Person("pers2");
            Person o3 = new Person("pers3");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            o3.getLocalized().put(1, "localized3");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);
            o3.setPartnerDocument(doc2);

            doc1.setAge(10);
            doc1.setOwner(o1);
            doc2.setAge(20);
            doc2.setOwner(o2);

            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);

            doc1.getContacts2().put(2, o1);
            doc2.getContacts2().put(2, o2);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);

            em.flush();
            tx.commit();
            em.clear();

            doc1 = em.find(Document.class, doc1.getId());
            doc2 = em.find(Document.class, doc2.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSubquery() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentWithSubquery.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).orderByAsc("id");
        List<DocumentWithSubquery> list = evm.applySetting(EntityViewSetting.create(DocumentWithSubquery.class), cb).getResultList();

        assertEquals(2, list.size());
        assertEquals("doc1", list.get(0).getName());
        assertEquals(Long.valueOf(1), list.get(0).getContactCount());
        assertEquals("doc2", list.get(1).getName());
        assertEquals(Long.valueOf(2), list.get(1).getContactCount());
    }

    @Test
    public void testSubqueryWithExpression() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentWithExpressionSubqueryView.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<DocumentWithExpressionSubqueryView, PaginatedCriteriaBuilder<DocumentWithExpressionSubqueryView>> setting = EntityViewSetting
            .create(DocumentWithExpressionSubqueryView.class, 0, 2);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeSorter("contactCount", Sorters.descending());
        setting.addAttributeSorter("id", Sorters.descending());

        PaginatedCriteriaBuilder<DocumentWithExpressionSubqueryView> paginatedCb = evm.applySetting(setting, cb);
        PagedList<DocumentWithExpressionSubqueryView> result = paginatedCb.getResultList();

        assertEquals(2, result.size());
        assertEquals(2, result.getTotalSize());

        assertEquals(doc2.getName(), result.get(0).getName());
        assertEquals(Long.valueOf(22), result.get(0).getContactCount());

        assertEquals(doc1.getName(), result.get(1).getName());
        assertEquals(Long.valueOf(11), result.get(1).getContactCount());
    }

    @Test
    public void testSubqueryEntityViewSettings() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentWithSubquery.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).orderByDesc("id");
        EntityViewSetting<DocumentWithSubquery, PaginatedCriteriaBuilder<DocumentWithSubquery>> setting = EntityViewSetting
            .create(DocumentWithSubquery.class, 0, 1);
        setting.addAttributeFilter("contactCount", "0");
        PagedList<DocumentWithSubquery> list = evm.applySetting(setting, cb).getResultList();

        assertEquals(1, list.size());
        assertEquals(2, list.getTotalSize());
        assertEquals("doc2", list.get(0).getName());
        assertEquals(Long.valueOf(2), list.get(0).getContactCount());
    }
}
