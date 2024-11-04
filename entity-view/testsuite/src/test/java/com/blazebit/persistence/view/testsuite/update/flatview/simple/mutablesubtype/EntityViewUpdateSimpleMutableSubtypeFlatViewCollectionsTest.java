/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.ReadonlyNameObjectView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.UpdatableNameObjectView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleMutableSubtypeFlatViewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateSimpleMutableSubtypeFlatViewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableNameObjectView.class);
        cfg.addEntityView(ReadonlyNameObjectView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "names" };
    }

    @Test
    public void testUpdateCollectionElement() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        
        // When

        UpdatableNameObjectView updatableNameObjectView = evm.convert(docView.getNames().get(0), UpdatableNameObjectView.class);
        updatableNameObjectView.setPrimaryName("newPers");
        docView.getNames().set(0, updatableNameObjectView);
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally the person is updated because the primary name changed
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        if (version || isFullMode() && isQueryStrategy()) {
            builder.update(Document.class);
        }
        if (isFullMode() || !isQueryStrategy() && !supportsIndexedInplaceUpdate()) {
            builder.delete(Document.class, "names")
                    .insert(Document.class, "names");
        } else {
            builder.update(Document.class, "names");
        }
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newPers", doc1.getNames().get(0).getPrimaryName());
        assertSubviewEquals(doc1.getNames(), docView.getNames());
    }

    @Test
    public void testAddCollectionElement() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When

        UpdatableNameObjectView updatableNameObjectView = evm.create(UpdatableNameObjectView.class);
        updatableNameObjectView.setPrimaryName("newPers");
        docView.getNames().add(updatableNameObjectView);
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally the person is added because the primary name changed
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        if (version || isFullMode() && isQueryStrategy()) {
            builder.update(Document.class);
        }
        if (isFullMode() || !isQueryStrategy() && !supportsIndexedInplaceUpdate()) {
            builder.delete(Document.class, "names")
                    .insert(Document.class, "names");
        }
        builder.insert(Document.class, "names");
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newPers", doc1.getNames().get(1).getPrimaryName());
        assertSubviewEquals(doc1.getNames(), docView.getNames());
    }

    public static void assertSubviewEquals(Collection<NameObject> persons, Collection<? extends ReadonlyNameObjectView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (NameObject p : persons) {
            boolean found = false;
            for (ReadonlyNameObjectView pSub : personSubviews) {
                if (p.getPrimaryName().equals(pSub.getPrimaryName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a UpdatableNameObjectView with the name: " + p.getPrimaryName());
            }
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "names")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "names")
                .insert(Document.class, "names");
        if ( doc1.getNames().size() > 1 ) {
            builder.insert(Document.class, "names");
        }
        builder.update(Document.class);
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
