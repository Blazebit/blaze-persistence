/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.elementcollection;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForElementCollections;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.elementcollection.model.DocumentForElementCollectionsEmbeddableElementsView;
import com.blazebit.persistence.view.testsuite.update.elementcollection.model.DocumentForElementCollectionsFlatViewElementsView;
import com.blazebit.persistence.view.testsuite.update.elementcollection.model.DocumentForElementIdView;
import com.blazebit.persistence.view.testsuite.update.elementcollection.model.PersonForElementCollectionsView;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@RunWith(Parameterized.class)
// NOTE: No EclipseLink and Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class ElementCollectionUpdateReferenceTest extends AbstractEntityViewUpdateTest<DocumentForElementCollectionsEmbeddableElementsView> {

    private static final boolean REPLACE_WITH_REFERENCE_CONTENTS = true;

    private DocumentForElementCollections doc1;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            DocumentForElementCollections.class,
            PersonForElementCollections.class
        };
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(DocumentForElementCollectionsEmbeddableElementsView.class);
        cfg.addEntityView(DocumentForElementCollectionsFlatViewElementsView.class);
        cfg.addEntityView(PersonForElementCollectionsView.class);
        cfg.addEntityView(DocumentForElementIdView.class);
    }

    public ElementCollectionUpdateReferenceTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, DocumentForElementCollectionsEmbeddableElementsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void prepareData(EntityManager em) {
        doc1 = new DocumentForElementCollections("doc1");

        PersonForElementCollections o1 = new PersonForElementCollections("pers1");

        doc1.getPartners().add(o1);

        em.persist(doc1);
    }

    // Test for #1308
    @Test
    public void testUpdateReferenceEmbeddable() {
        DocumentForElementCollectionsEmbeddableElementsView doc1View = evm.getReference(DocumentForElementCollectionsEmbeddableElementsView.class, doc1.getId());
        doc1View.getPartners().add(new PersonForElementCollections("Test1"));

        update(doc1View);

        List<DocumentForElementCollections> entities = cbf.create(em, DocumentForElementCollections.class, "d")
                .where("id").eq(doc1.getId())
                .getResultList();

        // Doc1
        assertEquals(doc1.getName(), entities.get(0).getName());
        if (REPLACE_WITH_REFERENCE_CONTENTS) {
            assertEquals(1, entities.get(0).getPartners().size());
            assertEquals("Test1", entities.get(0).getPartners().iterator().next().getFullname());
        } else {
            assertEquals(2, entities.get(0).getPartners().size());
            List<PersonForElementCollections> partners = new ArrayList<>(entities.get(0).getPartners());
            partners.sort(Comparator.comparing(PersonForElementCollections::getFullname));
            assertEquals("Test1", partners.get(0).getFullname());
            assertEquals("pers1", partners.get(1).getFullname());
        }
    }

    // Test for #1308
    @Test
    public void testUpdateReferenceFlatView() {
        DocumentForElementCollectionsFlatViewElementsView doc1View = evm.getReference(DocumentForElementCollectionsFlatViewElementsView.class, doc1.getId());
        PersonForElementCollectionsView personForElementCollectionsView = evm.create(PersonForElementCollectionsView.class);
        personForElementCollectionsView.setFullname("Test1");
        doc1View.getPartners().add(personForElementCollectionsView);
        update(doc1View);

        List<DocumentForElementCollections> entities = cbf.create(em, DocumentForElementCollections.class, "d")
                .where("id").eq(doc1.getId())
                .getResultList();

        // Doc1
        assertEquals(doc1.getName(), entities.get(0).getName());
        if (REPLACE_WITH_REFERENCE_CONTENTS) {
            assertEquals(1, entities.get(0).getPartners().size());
            assertEquals("Test1", entities.get(0).getPartners().iterator().next().getFullname());
        } else {
            assertEquals(2, entities.get(0).getPartners().size());
            List<PersonForElementCollections> partners = new ArrayList<>(entities.get(0).getPartners());
            partners.sort(Comparator.comparing(PersonForElementCollections::getFullname));
            assertEquals("Test1", partners.get(0).getFullname());
            assertEquals("pers1", partners.get(1).getFullname());
        }
    }

    @Override
    protected void reload() {

    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder;
    }
}
