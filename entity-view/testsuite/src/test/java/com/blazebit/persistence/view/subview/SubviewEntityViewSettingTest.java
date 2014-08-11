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
package com.blazebit.persistence.view.subview;

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
import com.blazebit.persistence.view.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.subview.model.PersonSubView;
import com.blazebit.persistence.view.subview.model.PersonSubViewFiltered;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SubviewEntityViewSettingTest extends AbstractEntityViewTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("MyTest");
            Document doc2 = new Document("YourTest");
            Document doc3 = new Document("HisTest");

            Person o1 = new Person("DocumentViewer");
            Person o2 = new Person("DocumentOwnerMaster");
            Person o3 = new Person("DocumentOwnerSlave");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);

            doc1.setOwner(o1);
            doc2.setOwner(o2);
            doc3.setOwner(o3);

            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            doc3.getContacts().put(1, o3);

            doc1.getContacts2().put(2, o1);
            doc2.getContacts2().put(2, o2);
            doc3.getContacts2().put(2, o3);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEntityViewSettingFilterSubview() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<DocumentMasterView, PaginatedCriteriaBuilder<DocumentMasterView>> setting = EntityViewSetting.create(
            DocumentMasterView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.from(em, Document.class);
        setting.addAttributeFilter("owner.name", "Owner");
        setting.addAttributeSorter("owner.name", Sorters.descending());
        setting.addOptionalParameter("contactPersonNumber", 1);

        PaginatedCriteriaBuilder<DocumentMasterView> paginatedCb = setting.apply(evm, cb);
        PagedList<DocumentMasterView> result = paginatedCb.getResultList();

        assertEquals(1, result.size());
        assertEquals(2, result.totalSize());

        assertEquals("HisTest", result.get(0).getName());
        assertEquals("DocumentOwnerSlave", result.get(0).getOwner().getName());
    }

    @Test
    public void testEntityViewSettingFilterFilteredSubview() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<DocumentMasterView, PaginatedCriteriaBuilder<DocumentMasterView>> setting = EntityViewSetting.create(
            DocumentMasterView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.from(em, Document.class);
        setting.addAttributeFilter("myContactPerson.name", "Owner");
        setting.addAttributeSorter("myContactPerson.name", Sorters.descending());
        setting.addOptionalParameter("contactPersonNumber", 1);

        PaginatedCriteriaBuilder<DocumentMasterView> paginatedCb = setting.apply(evm, cb);
        PagedList<DocumentMasterView> result = paginatedCb.getResultList();

        assertEquals(1, result.size());
        assertEquals(2, result.totalSize());

        assertEquals("HisTest", result.get(0).getName());
        assertEquals("DocumentOwnerSlave", result.get(0).getOwner().getName());
    }
}
