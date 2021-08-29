/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.CustomRootPersonView;
import com.blazebit.persistence.view.testsuite.basic.model.DocumentWithEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.FilteredDocument;
import com.blazebit.persistence.view.testsuite.basic.model.PersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewSettingTest extends AbstractEntityViewTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("MyTest");
                Document doc2 = new Document("YourTest");
                Document doc3 = new Document("NoContacts");

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");
                Person o4 = new Person("pers4");
                Person o5 = new Person("pers5");
                Person o6 = new Person("pers6");
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
                doc1.getContacts().put(2, o4);
                doc2.getContacts().put(2, o5);
                doc3.getContacts().put(2, o6);

                doc1.getContacts2().put(2, o1);
                doc2.getContacts2().put(2, o2);
                doc3.getContacts2().put(2, o3);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);
                em.persist(o5);
                em.persist(o6);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Test
    public void testEntityViewSetting() {
        EntityViewManager evm = build(FilteredDocument.class);

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
        assertEquals(2, result.getTotalSize());
        assertEquals("YourTest", result.get(0).getName());
        assertEquals("pers2", result.get(0).getContactName());
    }

    @Test
    public void testEntityViewSettingWithEntityAttribute() {
        EntityViewManager evm = build(DocumentWithEntityView.class, PersonView.class);

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
    
    @Test
    public void testEntityViewSettingNotExistingFilterAttribute() {
        EntityViewManager evm = build(DocumentWithEntityView.class, PersonView.class);

        // Base setting
        EntityViewSetting<DocumentWithEntityView, PaginatedCriteriaBuilder<DocumentWithEntityView>> setting = EntityViewSetting
            .create(DocumentWithEntityView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("asd", "test");

        // Currently we have no way to express what filter should be used when using entity attributes
        try {
            evm.applySetting(setting, cb);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // Ok
        }
    }

    @Test
    public void testEntityViewSettingCustomRoot() {
        EntityViewManager evm = build(CustomRootPersonView.class);

        // Base setting
        EntityViewSetting<CustomRootPersonView, CriteriaBuilder<CustomRootPersonView>> setting = EntityViewSetting.create(CustomRootPersonView.class);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("name", "pers1");
        CriteriaBuilder<CustomRootPersonView> criteriaBuilder = evm.applySetting(setting, cb, "owner");
        assertEquals("SELECT " + singleValuedAssociationIdPath("owner_1.id", "owner_1") + " AS CustomRootPersonView_id, owner_1.name AS CustomRootPersonView_name " +
                        "FROM Document document JOIN document.owner owner_1 " +
                        "WHERE owner_1.name <> :param_0", criteriaBuilder.getQueryString());
        List<CustomRootPersonView> result = criteriaBuilder.getResultList();

        assertEquals(2, result.size());
        assertEquals("pers2", result.get(0).getName());
        assertEquals("pers2", result.get(1).getName());
    }

    @Test
    public void testEntityViewFetches() {
        EntityViewManager evm = build(DocumentWithEntityView.class, PersonView.class);

        EntityViewSetting<DocumentWithEntityView, CriteriaBuilder<DocumentWithEntityView>> setting = EntityViewSetting.create(DocumentWithEntityView.class);
        setting.fetch("id");
        setting.fetch("name");

        DocumentWithEntityView view = evm.applySetting(setting, cbf.create(em, Document.class).where("name").eq("MyTest")).getSingleResult();
        assertEquals("MyTest", view.getName());
        assertNotNull(view.getId());
        assertNull(view.getOwner());
    }

    @Test
    @Category({ NoMySQLOld.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // We need a left entity join for this so Hibernate < 5.1 can't be used
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testEntityViewFetchesWithFilterAndSorter() {
        EntityViewManager evm = build(DocumentWithEntityView.class, PersonView.class);

        EntityViewSetting<DocumentWithEntityView, CriteriaBuilder<DocumentWithEntityView>> setting = EntityViewSetting.create(DocumentWithEntityView.class);
        setting.fetch("id");
        setting.addAttributeSorter("name", Sorters.ascending());
        setting.addAttributeFilter("owner", false);
        setting.addAttributeSorter("firstContact.name", Sorters.ascending());
        setting.addAttributeSorter("contactCount", Sorters.ascending());

        List<DocumentWithEntityView> list = evm.applySetting(setting, cbf.create(em, Document.class)).getResultList();
        assertEquals(3, list.size());
        DocumentWithEntityView view = list.get(0);
        assertNotNull(view.getId());
        assertNull(view.getName());
        assertNull(view.getOwner());
        // We need to fetch it to provide sorting
        assertNotNull(view.getFirstContact());
        Document document = cbf.create(em, Document.class).where("name").eq("MyTest").getSingleResult();
        assertEquals(document.getId(), view.getId());
    }
}
