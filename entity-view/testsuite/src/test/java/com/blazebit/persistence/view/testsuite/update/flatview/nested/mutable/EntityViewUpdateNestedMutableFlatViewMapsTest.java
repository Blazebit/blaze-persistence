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
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectContainerView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

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
public class EntityViewUpdateNestedMutableFlatViewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateNestedMutableFlatViewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
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
        return new String[] { "nameContainerMap" };
    }

    @Test
    public void testUpdateCollectionElement() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();
        
        // When

        docView.getNameContainerMap().get("doc1").getNameObject().setPrimaryName("newPers");
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally the person is updated because the primary name changed
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        fullFetch(builder);
        if (version) {
            builder.update(Document.class);
        }

        if (supportsIndexedInplaceUpdate() && (!isQueryStrategy() || isQueryStrategy() && !isFullMode())) {
            builder.update(Document.class, "nameContainerMap");
        } else {
            builder.delete(Document.class, "nameContainerMap")
                    .insert(Document.class, "nameContainerMap");

        }

        assertNoUpdateAndReload(docView);
        assertEquals("newPers", doc1.getNameContainerMap().get("doc1").getNameObject().getPrimaryName());
        assertSubviewEquals(doc1.getNameContainerMap(), docView.getNameContainerMap());
    }

    public static void assertSubviewEquals(Map<String, NameObjectContainer> persons, Map<String, ? extends UpdatableNameObjectContainerView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<String, NameObjectContainer> entry : persons.entrySet()) {
            NameObjectContainer p = entry.getValue();
            boolean found = false;
            UpdatableNameObjectContainerView pSub = personSubviews.get(entry.getKey());
            if (pSub != null) {
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
                .fetching(Document.class, "nameContainerMap")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "nameContainerMap")
                .insert(Document.class, "nameContainerMap");
        builder.update(Document.class);
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
