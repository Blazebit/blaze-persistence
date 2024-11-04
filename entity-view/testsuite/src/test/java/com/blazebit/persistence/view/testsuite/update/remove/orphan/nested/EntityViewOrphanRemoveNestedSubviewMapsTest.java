/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.orphan.nested;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.AbstractEntityViewOrphanRemoveDocumentTest;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonCreateView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableResponsiblePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewOrphanRemoveNestedSubviewMapsTest extends AbstractEntityViewOrphanRemoveDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewOrphanRemoveNestedSubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableResponsiblePersonView.class);
        cfg.addEntityView(FriendPersonView.class);
        cfg.addEntityView(FriendPersonCreateView.class);
    }

    @Test
    public void testSetNull() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().get(2).setFriend(null);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        clearPersistenceContextAndReload();
        assertNull(p6.getFriend());
        assertNull(p9);
    }

    @Test
    public void testSetOther() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        FriendPersonView p5View = getPersonView(p5.getId(), FriendPersonView.class);
        clearQueries();

        // When
        docView.getContacts().get(2).setFriend(p5View);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        clearPersistenceContextAndReload();
        assertEquals(p5.getId(), p6.getFriend().getId());
        assertNull(p9);
    }

    @Test
    public void testSetNew() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        FriendPersonCreateView newPersonView = evm.create(FriendPersonCreateView.class);
        newPersonView.setName("new");
        clearQueries();

        // When
        docView.getContacts().get(2).setFriend(newPersonView);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();
        builder.insert(Person.class);
        builder.validate();

        clearPersistenceContextAndReload();
        assertEquals("new", p6.getFriend().getName());
        assertNull(p9);
    }

    @Test
    public void testRemoveCascade() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().remove(2);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
                // Re-insert since we will delete all first
                builder.insert(Document.class, "contacts");
            }
        } else {
            builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "contacts")
                .fetching(Person.class)
                .and();
            if (!supportsProxyRemoveWithoutLoading()) {
                builder.select(Person.class);
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        deletePersonOwned(builder, true);
        deletePersonOwned(builder, true);
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.delete(Document.class, "contacts");
        builder.validate();

        clearPersistenceContextAndReload();
        assertNull(p6);
        assertNull(p9);
        assertEquals(1, doc1.getContacts().size());
    }

    public AssertStatementBuilder assertUpdateAndRemove() {
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
                // Re-insert since we will delete all first
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            builder.assertSelect()
                    .fetching(Document.class)
                    .fetching(Document.class, "contacts")
                    .fetching(Person.class)
                    .and();
            if (!supportsProxyRemoveWithoutLoading()) {
                builder.select(Person.class);
            }
        }

        // Since we switch to entity flushing because of the collection, we avoid the document flush even in full mode
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }

        deletePersonOwned(builder, true);

        // document.responsiblePerson.friend
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.update(Person.class);
        return builder;
    }

}
