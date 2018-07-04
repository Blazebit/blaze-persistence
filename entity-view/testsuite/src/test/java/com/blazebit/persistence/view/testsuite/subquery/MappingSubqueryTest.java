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

package com.blazebit.persistence.view.testsuite.subquery;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.subquery.model.DocumentWithDeepSubqueryEmbeddingView;
import com.blazebit.persistence.view.testsuite.subquery.model.DocumentWithSubqueryEmbeddingView;
import com.blazebit.persistence.view.testsuite.subquery.model.DocumentWithSubqueryViewRoot;
import com.blazebit.persistence.view.testsuite.subquery.model.PersonWithSubqueryEmbeddingViewSubview;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.Sorters;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subquery.model.DocumentWithExpressionSubqueryView;
import com.blazebit.persistence.view.testsuite.subquery.model.DocumentWithSubquery;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MappingSubqueryTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                doc2 = new Document("doc2");

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");
                o1.getLocalized().put(1, "localized1");
                o2.getLocalized().put(1, "localized2");
                o3.getLocalized().put(1, "localized3");

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

                // Flush doc1 before so we get the ids we would expect
                em.persist(doc1);
                em.flush();

                em.persist(doc2);
                em.flush();

                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
    }

    @Test
    public void testSubquery() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithSubquery.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).orderByAsc("id");
        EntityViewSetting<DocumentWithSubquery, CriteriaBuilder<DocumentWithSubquery>> setting;
        setting = EntityViewSetting.create(DocumentWithSubquery.class);
        setting.addOptionalParameter("optionalParameter", 1);
        List<DocumentWithSubquery> list = evm.applySetting(setting, cb).getResultList();

        assertEquals(2, list.size());
        assertEquals("doc1", list.get(0).getName());
        assertEquals(Long.valueOf(1), list.get(0).getContactCount());
        assertEquals("doc2", list.get(1).getName());
        assertEquals(Long.valueOf(2), list.get(1).getContactCount());
    }

    @Test
    @Category(NoDB2.class)
    public void testSubqueryWithExpression() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithExpressionSubqueryView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        // Base setting
        EntityViewSetting<DocumentWithExpressionSubqueryView, PaginatedCriteriaBuilder<DocumentWithExpressionSubqueryView>> setting = EntityViewSetting
                .create(DocumentWithExpressionSubqueryView.class, 0, 2);

        // Query
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        setting.addAttributeSorter("contactCount", Sorters.descending());
        setting.addAttributeSorter("id", Sorters.descending());

        PaginatedCriteriaBuilder<DocumentWithExpressionSubqueryView> paginatedCb = evm.applySetting(setting, cb);
        // TODO: Since case when statements in order bys use the resolved expression and because hibernate does not resolve
        // the select alias in nested expressions, we can't run this on DB2
        // The fix is to use the select alias
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
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithSubquery.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).orderByDesc("id");
        EntityViewSetting<DocumentWithSubquery, PaginatedCriteriaBuilder<DocumentWithSubquery>> setting = EntityViewSetting
                .create(DocumentWithSubquery.class, 0, 1);
        setting.addOptionalParameter("optionalParameter", 1);
        setting.addAttributeFilter("contactCount", "0");
        PagedList<DocumentWithSubquery> list = evm.applySetting(setting, cb).getResultList();

        assertEquals(1, list.size());
        assertEquals(2, list.getTotalSize());
        assertEquals("doc2", list.get(0).getName());
        assertEquals(Long.valueOf(2), list.get(0).getContactCount());
    }

    @Test
    public void testSubqueryViewRootEntityViewSettings() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithSubqueryViewRoot.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Person> cb = cbf.create(em, Person.class);
        EntityViewSetting<DocumentWithSubqueryViewRoot, CriteriaBuilder<DocumentWithSubqueryViewRoot>> setting = EntityViewSetting
                .create(DocumentWithSubqueryViewRoot.class);
        setting.addOptionalParameter("optionalParameter", 1);
        setting.addAttributeSorter("name", Sorters.ascending());
        List<DocumentWithSubqueryViewRoot> list = evm.applySetting(setting, cb, "partnerDocument").getResultList();

        assertEquals("SELECT " +
                singleValuedAssociationIdPath("partnerDocument_1.id", "partnerDocument_1") + " AS DocumentWithSubqueryViewRoot_id, " +
                "(SELECT COUNT(person_1.id) " +
                "FROM Person person_1" +
                singleValuedAssociationIdJoin("person_1.partnerDocument", "partnerDocument_2", true) +
                " WHERE " + singleValuedAssociationIdPath("person_1.partnerDocument.id", "partnerDocument_2") + " = " + singleValuedAssociationIdPath("partnerDocument_1.id", "partnerDocument_1") +
                ") AS DocumentWithSubqueryViewRoot_contactCount, " +
                "partnerDocument_1.name AS DocumentWithSubqueryViewRoot_name " +
                "FROM Person person " +
                "LEFT JOIN person.partnerDocument partnerDocument_1 " +
                "ORDER BY " + renderNullPrecedence("DocumentWithSubqueryViewRoot_name", "partnerDocument_1.name", "ASC", "LAST"), cb.getQueryString());
        assertEquals(3, list.size());
        assertEquals("doc1", list.get(0).getName());
        assertEquals(Long.valueOf(1), list.get(0).getContactCount());
        assertEquals("doc2", list.get(1).getName());
        assertEquals(Long.valueOf(2), list.get(1).getContactCount());
        assertEquals("doc2", list.get(2).getName());
        assertEquals(Long.valueOf(2), list.get(2).getContactCount());
    }

    @Test
    public void testSubqueryEmbeddingViewEntityViewSettings() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithSubqueryEmbeddingView.class);
        cfg.addEntityView(PersonWithSubqueryEmbeddingViewSubview.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Person> cb = cbf.create(em, Person.class);
        EntityViewSetting<PersonWithSubqueryEmbeddingViewSubview, CriteriaBuilder<PersonWithSubqueryEmbeddingViewSubview>> setting = EntityViewSetting
                .create(PersonWithSubqueryEmbeddingViewSubview.class);
        setting.addOptionalParameter("optionalParameter", 1);
        setting.addAttributeSorter("pDocument.name", Sorters.ascending());
        List<PersonWithSubqueryEmbeddingViewSubview> list = evm.applySetting(setting, cb).getResultList();

        assertEquals("SELECT " +
                "person.id AS PersonWithSubqueryEmbeddingViewSubview_id, " +
                "person.name AS PersonWithSubqueryEmbeddingViewSubview_name, " +
                singleValuedAssociationIdPath("person.partnerDocument.id", "partnerDocument_1") + " AS PersonWithSubqueryEmbeddingViewSubview_pDocument_id, " +
                "(SELECT COUNT(person_1.id) " +
                "FROM Person person_1" +
                singleValuedAssociationIdJoin("person_1.partnerDocument", "partnerDocument_2", true) +
                " WHERE " + singleValuedAssociationIdPath("person_1.partnerDocument.id", "partnerDocument_2") + " = " + singleValuedAssociationIdPath("person.partnerDocument.id", "partnerDocument_1") +
                ") AS PersonWithSubqueryEmbeddingViewSubview_pDocument_contactCount, " +
                "partnerDocument_1.name AS PersonWithSubqueryEmbeddingViewSubview_pDocument_name " +
                "FROM Person person " +
                "LEFT JOIN person.partnerDocument partnerDocument_1 " +
                "ORDER BY " + renderNullPrecedence("PersonWithSubqueryEmbeddingViewSubview_pDocument_name", "partnerDocument_1.name", "ASC", "LAST"), cb.getQueryString());
        assertEquals(3, list.size());
        assertEquals("doc1", list.get(0).getPDocument().getName());
        assertEquals(Long.valueOf(1), list.get(0).getPDocument().getContactCount());
        assertEquals("doc2", list.get(1).getPDocument().getName());
        assertEquals(Long.valueOf(2), list.get(1).getPDocument().getContactCount());
        assertEquals("doc2", list.get(2).getPDocument().getName());
        assertEquals(Long.valueOf(2), list.get(2).getPDocument().getContactCount());
    }

    @Test
    public void testDeepSubqueryEmbeddingViewEntityViewSettings() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithDeepSubqueryEmbeddingView.class);
        cfg.addEntityView(DocumentWithSubqueryEmbeddingView.class);
        cfg.addEntityView(PersonWithSubqueryEmbeddingViewSubview.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        EntityViewSetting<DocumentWithDeepSubqueryEmbeddingView, CriteriaBuilder<DocumentWithDeepSubqueryEmbeddingView>> setting = EntityViewSetting
                .create(DocumentWithDeepSubqueryEmbeddingView.class);
        setting.addOptionalParameter("optionalParameter", 1);
        setting.addAttributeSorter("owner.pDocument.name", Sorters.ascending());
        List<DocumentWithDeepSubqueryEmbeddingView> list = evm.applySetting(setting, cb).getResultList();

        assertEquals("SELECT " +
                "document.id AS DocumentWithDeepSubqueryEmbeddingView_id, " +
                singleValuedAssociationIdPath("document.owner.id", "owner_1") + " AS DocumentWithDeepSubqueryEmbeddingView_owner_id, " +
                "owner_1.name AS DocumentWithDeepSubqueryEmbeddingView_owner_name, " +
                singleValuedAssociationIdPath("owner_1.partnerDocument.id", "partnerDocument_1") + " AS DocumentWithDeepSubqueryEmbeddingView_owner_pDocument_id, " +
                "(SELECT COUNT(person.id) " +
                "FROM Person person" +
                singleValuedAssociationIdJoin("person.partnerDocument", "partnerDocument_2", true) +
                " WHERE " + singleValuedAssociationIdPath("person.partnerDocument.id", "partnerDocument_2") + " = " + singleValuedAssociationIdPath("owner_1.partnerDocument.id", "partnerDocument_1") +
                ") AS DocumentWithDeepSubqueryEmbeddingView_owner_pDocument_contactCount, " +
                "partnerDocument_1.name AS DocumentWithDeepSubqueryEmbeddingView_owner_pDocument_name " +
                "FROM Document document " +
                "JOIN document.owner owner_1 " +
                "LEFT JOIN owner_1.partnerDocument partnerDocument_1 " +
                "ORDER BY " + renderNullPrecedence("DocumentWithDeepSubqueryEmbeddingView_owner_pDocument_name", "partnerDocument_1.name", "ASC", "LAST"), cb.getQueryString());
    }

}