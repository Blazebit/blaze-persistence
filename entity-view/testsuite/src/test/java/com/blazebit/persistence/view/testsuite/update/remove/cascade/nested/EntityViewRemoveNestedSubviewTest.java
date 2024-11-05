/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.cascade.nested;

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
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableResponsiblePersonView;
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
@Category({ NoEclipselink.class})
public class EntityViewRemoveNestedSubviewTest extends AbstractEntityViewRemoveDocumentTest<UpdatableDocumentView> {

    public EntityViewRemoveNestedSubviewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
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
        final UpdatableDocumentView docView = getDoc1View();
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
        deletePersonOwned(builder, true);
        deletePersonOwned(builder, true);

        // document.responsiblePerson.friend
        builder.delete(Person.class)
                .update(Document.class)
        // document.responsiblePerson
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
        remove(UpdatableDocumentView.class, doc1.getId());

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            // cascade of document.versions
            builder.select(Version.class);
            // document.responsiblePerson.friend
            builder.select(Person.class);
        }

        deleteDocumentOwned(builder, false);
        deletePersonOwned(builder, false);
        deletePersonOwned(builder, true);

        // In the query strategy, we use a returning clause to avoid a select statement
        if (!isQueryStrategy() || !dbmsDialect.supportsReturningColumns()) {
            // document.responsiblePerson.id
            builder.select(Document.class);
            if (!isQueryStrategy() && !supportsProxyRemoveWithoutLoading() || isQueryStrategy() && !dbmsDialect.supportsReturningColumns()) {
                // responsiblePerson.friend
                builder.select(Person.class);
            }
        }

        if (isQueryStrategy()) {
            // Need to select the version id to be able to delete the localized set
            builder.select(Version.class);
        }

        // document.responsiblePerson
        builder.delete(Person.class)
                .update(Document.class)
                // document.responsiblePerson.friend
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
