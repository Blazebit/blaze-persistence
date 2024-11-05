/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.nested.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.nested.mutable.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.mutable.model.UpdatableFriendPersonView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.mutable.model.UpdatableResponsiblePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public class EntityViewUpdateNestedMutableSubviewTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentView> {

    public EntityViewUpdateNestedMutableSubviewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableResponsiblePersonView.class);
        cfg.addEntityView(UpdatableFriendPersonView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setName("newDoc");
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testUpdateWithSubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatableFriendPersonView newFriend = getPersonView(p4.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        docView.getResponsiblePerson().setFriend(newFriend);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }
            builder.update(Person.class);
            if (isFullMode() || version) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder)
                        // The new person p4 is also loaded for applying changes
                        // This is due to the use of the full flush mode
                        .select(Person.class);
            } else {
                builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Person.class)
                        .and();
            }
            if (version) {
                builder.update(Document.class);
            }
            builder.update(Person.class);
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p4.getId(), doc1.getResponsiblePerson().getFriend().getId());
    }

    @Test
    public void testUpdateWithModifySubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatableFriendPersonView newFriend = getPersonView(p4.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        newFriend.setName("newFriend");
        docView.getResponsiblePerson().setFriend(newFriend);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.update(Person.class)
                    .update(Person.class);

            if (isFullMode() || version) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Person.class)
                        .and();
            }

            builder.select(Person.class);

            if (version) {
                builder.update(Document.class);
            }

            builder.update(Person.class)
                    .update(Person.class);
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p4.getId(), doc1.getResponsiblePerson().getFriend().getId());
        assertEquals("newFriend", p4.getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.getResponsiblePerson().getFriend().setName("newFriend");
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }

            builder.update(Person.class);

            if (isFullMode() || version) {
                builder.update(Document.class);
            }
        } else {
            fullFetch(builder);

            if (version) {
                builder.update(Document.class);
            }
            builder.update(Person.class);
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newFriend", p3.getName());
    }

    @Test
    public void testUpdateToNull() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.getResponsiblePerson().setFriend(null);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.update(Person.class);

            if (isFullMode() || version) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Person.class)
                        .and();
            }

            if (version) {
                builder.update(Document.class);
            }

            builder.update(Person.class);
        }

        builder.validate();

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                afterBuilder.update(Person.class)
                        .update(Document.class);
            } else {
                fullFetch(afterBuilder);
            }
        }
        afterBuilder.validate();
        Assert.assertNull(p1.getFriend());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(Person.class)
                .update(Person.class)
                .update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
