/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic;

import static org.junit.Assert.assertEquals;
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
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitiveDocumentMultisetView;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitivePersonView;
import com.blazebit.persistence.view.testsuite.basic.model.PrimitiveSimpleDocumentView;
import com.blazebit.persistence.view.testsuite.basic.model.SelectFetchingPrimitivePersonView;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PrimitiveViewMultisetTest extends AbstractEntityViewTest {

    private final Collection<String> fetches;

    public PrimitiveViewMultisetTest(Collection<String> fetches) {
        this.fetches = fetches;
    }

    private EntityViewManager evm;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            PrimitiveDocument.class,
            PrimitivePerson.class,
            PrimitiveVersion.class
        };
    }

    private PrimitiveDocument doc1;
    private PrimitiveDocument doc2;

    private PrimitivePerson p1;
    private PrimitivePerson p2;

    @Before
    public void initEvm() {
        evm = build(
            PrimitivePersonView.class,
            PrimitiveSimpleDocumentView.class,
            PrimitiveDocumentMultisetView.class,
            SelectFetchingPrimitivePersonView.class
        );
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new PrimitiveDocument("doc1");
                doc2 = new PrimitiveDocument("doc2");

                p1 = new PrimitivePerson("pers1");
                p2 = new PrimitivePerson("pers2");
                p1.setPartnerDocument(doc1);
                p2.setPartnerDocument(doc2);

                doc1.setOwner(p1);
                doc2.setOwner(p2);

                doc1.getContacts().put(1, p1);
                doc2.getContacts().put(1, p2);

                doc1.getPeople().add(p1);
                doc2.getPeople().add(p2);

                em.persist(p1);
                em.persist(p2);

                em.persist(doc1);
                em.persist(doc2);
            }
        });

        doc1 = em.find(PrimitiveDocument.class, doc1.getId());
        doc2 = em.find(PrimitiveDocument.class, doc2.getId());
    }

    @Parameterized.Parameters
    public static Iterable<Collection<String>> fetchLists() {
        return Arrays.asList(
            Arrays.asList("name", "partners.name"),
            Arrays.asList("name", "partners"),
            Arrays.asList("name", "people"),
            Arrays.asList("name", "people.name"),

            Arrays.asList("name", "partners.name", "people.name"),
            Arrays.asList("name", "partners.name", "people"),
            Arrays.asList("name", "partners", "people.name"),
            Arrays.asList("name", "partners", "people")
        );
    }

    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void entityViewMultisetSubviewFetching() {
        // Given
        EntityViewSetting<PrimitiveDocumentMultisetView, CriteriaBuilder<PrimitiveDocumentMultisetView>> setting = EntityViewSetting.create(PrimitiveDocumentMultisetView.class);
        fetches.forEach(setting::fetch);

        // When
        PrimitiveDocumentMultisetView view = evm.applySetting(setting, cbf.create(em, PrimitiveDocument.class).where("id").eq(doc1.getId())).getResultList().get(0);

        // Then
        assertEquals("doc1", view.getName());
        if (fetchesPartners(fetches)) {
            assertEquals(1, view.getPartners().size());
            assertEquals("pers1", view.getPartners().iterator().next().getName());
        } else {
            assertNull(view.getPartners());
        }
        if (fetchesPeople(fetches)) {
            assertEquals(1, view.getPeople().size());
            assertEquals("pers1", view.getPeople().iterator().next().getName());
        } else {
            assertNull(view.getPeople());
        }
        assertNull(view.getContacts());
    }

    private boolean fetchesPartners(Collection<String> fetches) {
        return fetches.stream().anyMatch(fetch -> fetch.startsWith("partners"));
    }

    private boolean fetchesPeople(Collection<String> fetches) {
        return fetches.stream().anyMatch(fetch -> fetch.startsWith("people"));
    }
}
