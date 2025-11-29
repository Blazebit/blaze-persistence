/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.array;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.array.model.DocumentCollectionsContainerView;
import com.blazebit.persistence.view.testsuite.array.model.DocumentCollectionsLimitView;
import com.blazebit.persistence.view.testsuite.array.model.SubviewDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollectionsContainer;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;
import jakarta.persistence.EntityManager;

import static com.blazebit.persistence.view.testsuite.collections.subview.SubviewAssert.assertSubviewEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ArrayFilteredCollectionsTest extends AbstractEntityViewTest {

    private DocumentForCollectionsContainer docContainer1;
    private DocumentForCollections doc1;
    private DocumentForCollections doc2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollectionsContainer.class,
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                docContainer1 = new DocumentForCollectionsContainer("docs");
                doc1 = new DocumentForCollections("doc1");
                doc2 = new DocumentForCollections("doc2");

                PersonForCollections o1 = new PersonForCollections("pers1");
                PersonForCollections o2 = new PersonForCollections("pers2");
                PersonForCollections o3 = new PersonForCollections("pers3");
                PersonForCollections o4 = new PersonForCollections("pers4");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc1);
                o4.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);
                doc1.getContacts().put(2, o3);
                doc2.getContacts().put(2, o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                doc1.getPartners().add(o1);
                doc1.getPartners().add(o3);
                doc2.getPartners().add(o2);
                doc2.getPartners().add(o4);

                doc1.getPersonList().add(o1);
                doc1.getPersonList().add(o2);
                doc2.getPersonList().add(o3);
                doc2.getPersonList().add(o4);

                em.persist(doc1);
                em.persist(doc2);

                docContainer1.getDocuments().add(doc1);
                docContainer1.getDocuments().add(doc2);
                em.persist(docContainer1);
            }
        });
    }

    @Before
    public void setUp() {
        docContainer1 = cbf.create(em, DocumentForCollectionsContainer.class).where("containerName").eq("docs").getSingleResult();
        doc1 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc2").getSingleResult();
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    @Test
    @Category({ NoDB2.class, NoEclipselink.class })
    public void testArrayExpression() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        EntityViewManager evm = build(cfg, SubviewDocumentCollectionsView.class, SubviewPersonForCollectionsView.class);

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<SubviewDocumentCollectionsView> cb = evm.applySetting(EntityViewSetting.create(SubviewDocumentCollectionsView.class), criteria);
        List<SubviewDocumentCollectionsView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertSubviewEquals(doc1.getContacts(), results.get(0).getContacts());
        assertSubviewEquals(doc1.getPartners(), results.get(0).getPartners());
        assertSubviewEquals(doc1.getPersonList(), results.get(0).getPersonList());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertSubviewEquals(doc2.getContacts(), results.get(1).getContacts());
        assertSubviewEquals(doc2.getPartners(), results.get(1).getPartners());
        assertSubviewEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }

    // Test for #2057
    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    @Test
    @Category({  NoDB2.class, NoEclipselink.class })
    public void testArrayExpressionNested() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        EntityViewManager evm = build(cfg, DocumentCollectionsContainerView.class, DocumentCollectionsLimitView.class, SubviewPersonForCollectionsView.class);
        // Assert that the attribute isn't named "name", as that is vital to trigger the bug
        assertNotNull(emf.getMetamodel().entity(DocumentForCollectionsContainer.class).getAttribute("containerName"));

        CriteriaBuilder<DocumentForCollectionsContainer> criteria = cbf.create(em, DocumentForCollectionsContainer.class, "d");
        CriteriaBuilder<DocumentCollectionsContainerView> cb = evm.applySetting(EntityViewSetting.create(DocumentCollectionsContainerView.class), criteria);
        List<DocumentCollectionsContainerView> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals(docContainer1.getContainerName(), results.get(0).getContainerName());

        // Doc1
        assertEquals(doc1.getName(), results.get(0).getFirstDocument().getName());
        assertSubviewEquals(doc1.getContacts(), results.get(0).getFirstDocument().getContacts());
        assertSubviewEquals(doc1.getPartners(), results.get(0).getFirstDocument().getPartners());
        assertSubviewEquals(doc1.getPersonList(), results.get(0).getFirstDocument().getPersonList());
    }
}
