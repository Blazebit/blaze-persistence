/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.cascade.nested;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertSelectStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.AbstractEntityViewRemoveDocumentTest;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.FriendPersonView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableResponsiblePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public class EntityViewRemoveNestedSubviewMapsTest extends AbstractEntityViewRemoveDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewRemoveNestedSubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
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
    }

    @Test
    public void testSimpleRemove() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        remove(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            if (!supportsProxyRemoveWithoutLoading()) {
                builder.select(Person.class)
                    .select(Person.class);
            }
            builder.select(Document.class)
                    .select(Version.class);
        }

        if (isQueryStrategy()) {
            // Need to select the version id to be able to delete the localized set
            builder.select(Version.class);
        }

        deleteDocumentOwned(builder);
        deletePersonOwned(builder);
        deletePersonOwned(builder);

        // document.contacts.friend
        builder.delete(Person.class)
                .update(Document.class)
                // document.contacts
                .delete(Person.class)
                .update(Document.class)
                // document.versions
                .delete(Version.class)
                .delete(Version.class, "localized")
                .delete(Document.class)
                .validate();

        clearPersistenceContextAndReload();
        assertNull(doc1);
        assertNull(p1);
        assertNull(p3);
    }

    @Test
    public void testRemoveById() {
        // Given
        clearQueries();

        // When
        remove(UpdatableDocumentWithMapsView.class, doc1.getId());

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            // cascade of document.versions
            builder.select(Version.class);
            // Hibernate flushes the changes done to the document because Person#1 was deleted => responsiblePerson set to NULL
            builder.update(Document.class);

            // Select for deletion because of possible cycle
            // document.contacts.id
            builder.select(Document.class);
        }

        deleteDocumentOwned(builder, true);
        deletePersonOwned(builder, false);
        deletePersonOwned(builder, true);

        // document.contacts
        if (isQueryStrategy()) {
            // Contacts ids are returned instead of queried if possible
            if (dbmsDialect.supportsReturningColumns()) {
                // But unfortunately current Hibernate versions also try to do the delete implicitly
                builder.assertDelete().forRelation(Document.class, "contacts").and();
            } else {
                // The JPQL that joins people unfortunately joins all tables, though the collection table alone would suffice
                AssertSelectStatementBuilder statementBuilder = builder.assertSelect()
                    .forEntity(Document.class)
                    .forRelation(Document.class, "contacts");
                if (!supportsLazyCollectionElementJoin()) {
                    // As of Hibernate 6, we don't always need to join the element table
                    statementBuilder.fetching(Person.class);
                }
                statementBuilder.and();
            }
        } else {
            builder.assertSelect()
                    .fetching(Document.class, "contacts")
                    .fetching(Person.class)
                    .and();
        }

        // If possible, the deletion of document.contacts returns document.contacts.friend
        if (!isQueryStrategy() && !supportsProxyRemoveWithoutLoading() || isQueryStrategy() && !dbmsDialect.supportsReturningColumns()) {
            builder.select(Person.class);
        }

        if (isQueryStrategy()) {
            // Need to select the version id to be able to delete the localized set
            builder.select(Version.class);
        }

        builder.delete(Person.class)
                .update(Document.class)
                // document.contacts.friend
                .delete(Person.class)
                .update(Document.class)
                // document.versions
                .delete(Version.class)
                .delete(Version.class, "localized")
                .delete(Document.class)
                .validate();

        clearPersistenceContextAndReload();
        assertNull(doc1);
        assertNull(p1);
        assertNull(p3);
    }
}
