/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectContainerView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectView;
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
// NOTE: Hibernate 4 does not support a list of embeddables containing embeddables
@Category({NoHibernate42.class, NoHibernate43.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateNestedMutableFlatViewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateNestedMutableFlatViewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableNameObjectView.class);
        cfg.addEntityView(UpdatableNameObjectContainerView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "nameContainers" };
    }

    @Test
    public void testUpdateCollectionElement() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        
        // When

        docView.getNameContainers().get(0).getNameObject().setPrimaryName("newPers");
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
        if (isFullMode()) {
            builder.delete(Document.class, "nameContainers")
                    .insert(Document.class, "nameContainers");
        } else {
            builder.update(Document.class, "nameContainers");
        }
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newPers", doc1.getNameContainers().get(0).getNameObject().getPrimaryName());
        assertSubviewEquals(doc1.getNameContainers(), docView.getNameContainers());
    }

    public static void assertSubviewEquals(Collection<NameObjectContainer> persons, Collection<? extends UpdatableNameObjectContainerView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (NameObjectContainer p : persons) {
            boolean found = false;
            for (UpdatableNameObjectContainerView pSub : personSubviews) {
                if (p.getNameObject().getPrimaryName().equals(pSub.getNameObject().getPrimaryName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getNameObject().getPrimaryName());
            }
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "nameContainers")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "nameContainers")
                .insert(Document.class, "nameContainers");
        builder.update(Document.class);
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
