/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.entity.PrimitiveVersion;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitiveDocumentMultisetView;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitiveDocumentView;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitivePersonView;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitiveSimpleDocumentView;
import com.blazebit.persistence.view.testsuite.basic.model.SelectFetchingPrimitivePersonView;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrimitiveViewTest extends AbstractEntityViewTest {

    private EntityViewManager evm;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                PrimitiveDocument.class,
                PrimitivePerson.class,
                PrimitiveVersion.class
        };
    }

    @Before
    public void initEvm() {
        evm = build(
                PrimitiveSimpleDocumentView.class,
                PrimitiveDocumentView.class,
                PrimitivePersonView.class,
                PrimitiveDocumentMultisetView.class,
                SelectFetchingPrimitivePersonView.class
        );
    }

    private PrimitiveDocument doc1;
    private PrimitiveDocument doc2;

    private PrimitivePerson o1;
    private PrimitivePerson o2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new PrimitiveDocument("doc1");
                doc2 = new PrimitiveDocument("doc2");

                o1 = new PrimitivePerson("pers1");
                o2 = new PrimitivePerson("pers2");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);

                doc1.getPeople().add(o1);
                doc2.getPeople().add(o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);
            }
        });

        doc1 = em.find(PrimitiveDocument.class, doc1.getId());
        doc2 = em.find(PrimitiveDocument.class, doc2.getId());
    }

    @Test
    @Category({ NoEclipselink.class })
    // Eclipselink has a result set mapping bug in case of map keys
    public void testSimple() {
        CriteriaBuilder<PrimitiveDocument> criteria = cbf.create(em, PrimitiveDocument.class, "d")
            .orderByAsc("id");
        EntityViewSetting<PrimitiveDocumentView, CriteriaBuilder<PrimitiveDocumentView>> setting;
        setting = EntityViewSetting.create(PrimitiveDocumentView.class);
        List<PrimitiveDocumentView> results = evm.applySetting(setting, criteria).getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        assertFalse(results.get(0).isDeleted());
        assertEquals(o1.getId(), results.get(0).getOwner().getId().longValue());
        assertEquals(o1.getName(), results.get(0).getOwner().getName());
        // Doc2
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertFalse(results.get(1).isDeleted());
        assertEquals(o2.getId(), results.get(1).getOwner().getId().longValue());
        assertEquals(o2.getName(), results.get(1).getOwner().getName());

        results.get(0).setId(123L);
        results.get(0).setName("Abc");
    }

    @Test
    // Test for issue #375
    public void primitiveBooleanAttributeMetamodelMappingIsCorrect() {
        ViewType<PrimitiveSimpleDocumentView> view = evm.getMetamodel().view(PrimitiveSimpleDocumentView.class);
        assertNotNull(view.getAttribute("deleted"));
        assertEquals(boolean.class, view.getAttribute("deleted").getJavaType());
    }

    @Test
    public void testEntityViewSubviewFetches() {
        EntityViewManager evm = build(
                PrimitiveSimpleDocumentView.class,
                PrimitiveDocumentView.class,
                PrimitivePersonView.class
        );

        EntityViewSetting<PrimitiveDocumentView, CriteriaBuilder<PrimitiveDocumentView>> setting = EntityViewSetting.create(PrimitiveDocumentView.class);
        setting.fetch("name");
        setting.fetch("owner.name");
        setting.fetch("correlatedOwner.name");

        PrimitiveDocumentView view = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class).where("id").eq(doc1.getId())).getResultList().get(0);
        assertEquals("doc1", view.getName());
        assertEquals("pers1", view.getOwner().getName());
        assertEquals("pers1", view.getCorrelatedOwner().getName());
        assertNull(view.getContacts());
    }

    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void entityViewMultisetWithNestedSelectFetching() {
        // Given
        EntityViewSetting<PrimitiveDocumentMultisetView, CriteriaBuilder<PrimitiveDocumentMultisetView>> setting = EntityViewSetting.create(PrimitiveDocumentMultisetView.class);
        setting.fetch("people.ownedDocumentsSelectFetched");

        // When
        PrimitiveDocumentMultisetView view = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class).where("id").eq(doc1.getId())).getResultList().get(0);

        // Then
        assertNull(view.getPartners());
        assertNull(view.getName());
        assertEquals(0, view.getDocId());
        assertEquals(1, view.getPeople().size());
        SelectFetchingPrimitivePersonView personView = view.getPeople().get(0);
        assertEquals(1, personView.getOwnedDocumentsSelectFetched().size());
        assertNull(personView.getName());
    }

    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void entityViewMultisetWithNestedJoinFetching() {
        // Given
        EntityViewSetting<PrimitiveDocumentMultisetView, CriteriaBuilder<PrimitiveDocumentMultisetView>> setting = EntityViewSetting.create(PrimitiveDocumentMultisetView.class);
        setting.fetch("people.ownedDocumentsJoinFetched");

        // When
        PrimitiveDocumentMultisetView view = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class).where("id").eq(doc1.getId())).getResultList().get(0);

        // Then
        assertNull(view.getPartners());
        assertNull(view.getName());
        assertEquals(0, view.getDocId());
        assertEquals(1, view.getPeople().size());
        SelectFetchingPrimitivePersonView personView = view.getPeople().get(0);
        assertEquals(1, personView.getOwnedDocumentsJoinFetched().size());
        assertNull(personView.getName());
    }
}
