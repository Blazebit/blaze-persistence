/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate60;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate62;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.subview.model.SimpleDocumentView;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubViewFiltered;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubviewEntityViewSettingTest extends AbstractEntityViewTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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
            }
        });
    }

    @Test
    // Hibernate ORM 6.2 bug: https://hibernate.atlassian.net/browse/HHH-18272
    @Category({ NoEclipselink.class, NoHibernate60.class })
    // Eclipselink has a result set mapping bug in case of map keys
    public void testEntityViewSettingFilterSubview() {
        EntityViewManager evm = build(
                DocumentMasterView.class,
                SimpleDocumentView.class,
                PersonSubView.class,
                PersonSubViewFiltered.class
        );

        // Base setting
        EntityViewSetting<DocumentMasterView, PaginatedCriteriaBuilder<DocumentMasterView>> setting = EntityViewSetting.create(
            DocumentMasterView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("owner.name", "OWNER");
        setting.addAttributeSorter("owner.name", Sorters.descending());
        setting.addAttributeSorter("id", Sorters.descending());
        setting.addOptionalParameter("contactPersonNumber", 1);

        PaginatedCriteriaBuilder<DocumentMasterView> paginatedCb = evm.applySetting(setting, cb);
        PagedList<DocumentMasterView> result = paginatedCb.getResultList();

        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());

        assertEquals("HisTest", result.get(0).getName());
        assertEquals("DocumentOwnerSlave".toUpperCase(), result.get(0).getOwner().getName());
    }

    @Test
    // Hibernate ORM 6.2 bug: https://hibernate.atlassian.net/browse/HHH-18272
    @Category({ NoEclipselink.class, NoHibernate62.class })
    // Eclipselink does not support VALUE() dereferencing
    public void testEntityViewSettingFilterFilteredSubview() {
        EntityViewManager evm = build(
                DocumentMasterView.class,
                SimpleDocumentView.class,
                PersonSubView.class,
                PersonSubViewFiltered.class
        );

        // Base setting
        EntityViewSetting<DocumentMasterView, PaginatedCriteriaBuilder<DocumentMasterView>> setting = EntityViewSetting.create(
            DocumentMasterView.class, 0, 1);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeFilter("myContactPerson.name", "Owner");
        setting.addAttributeSorter("myContactPerson.name", Sorters.descending());
        setting.addAttributeSorter("id", Sorters.descending());
        setting.addOptionalParameter("contactPersonNumber", 1);

        PaginatedCriteriaBuilder<DocumentMasterView> paginatedCb = evm.applySetting(setting, cb);
        PagedList<DocumentMasterView> result = paginatedCb.getResultList();

        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());

        assertEquals("HisTest", result.get(0).getName());
        assertEquals("DocumentOwnerSlave".toUpperCase(), result.get(0).getOwner().getName());
    }
}
