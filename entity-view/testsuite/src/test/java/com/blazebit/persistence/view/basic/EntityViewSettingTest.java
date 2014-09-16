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
package com.blazebit.persistence.view.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.basic.model.DocumentWithEntityView;
import com.blazebit.persistence.view.basic.model.FilteredDocument;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewSettingTest extends AbstractEntityViewTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("MyTest");
            Document doc2 = new Document("YourTest");
            Document doc3 = new Document("NoContacts");

            Person o1 = new Person("pers1");
            Person o2 = new Person("pers2");
            Person o3 = new Person("pers3");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);

            doc1.setOwner(o1);
            doc2.setOwner(o2);
            doc3.setOwner(o2);

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
    public void testEntityViewSetting() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(FilteredDocument.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<FilteredDocument, PaginatedCriteriaBuilder<FilteredDocument>> setting = EntityViewSetting.create(
            FilteredDocument.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("name", "Test");
        setting.addAttributeFilter("contactCount", "1");
        setting.addAttributeSorter("name", Sorters.descending());
        setting.addAttributeSorter("id", Sorters.descending());
        setting.addOptionalParameter("index", 1);

        PaginatedCriteriaBuilder<FilteredDocument> paginatedCb = evm.applySetting(setting, cb);
        PagedList<FilteredDocument> result = paginatedCb.getResultList();

        assertEquals(1, result.size());
        assertEquals(2, result.totalSize());
        assertEquals("YourTest", result.get(0).getName());
        assertEquals("pers2", result.get(0).getContactName());
    }

    @Test
    public void testEntityViewSettingWithEntityAttribute() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentWithEntityView.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        // Base setting
        EntityViewSetting<DocumentWithEntityView, PaginatedCriteriaBuilder<DocumentWithEntityView>> setting = EntityViewSetting
            .create(DocumentWithEntityView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("owner.name", "pers2");

        // Currently we have no way to express what filter should be used when using entity attributes
        try {
            evm.applySetting(setting, cb);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // Ok
        }
    }
    
    // TODO: needs more tests
}
