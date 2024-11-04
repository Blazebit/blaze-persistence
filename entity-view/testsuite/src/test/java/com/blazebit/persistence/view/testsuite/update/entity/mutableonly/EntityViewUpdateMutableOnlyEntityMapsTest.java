/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.entity.mutableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityMapsTest;
import com.blazebit.persistence.view.testsuite.update.entity.mutableonly.model.UpdatableDocumentEntityWithMapsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableOnlyEntityMapsTest extends AbstractEntityViewUpdateEntityMapsTest<UpdatableDocumentEntityWithMapsView> {

    public EntityViewUpdateMutableOnlyEntityMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithMapsView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When & Then
        try {
            replaceCollection();
            fail("Expected the setter of a mutable only field to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Updating the mutable-only attribute 'contacts'"));
        }
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When & Then
        try {
            addToCollection();
            fail("Expected mutating collection operations to fail!");
        } catch (UnsupportedOperationException ex) {
            assertTrue(ex.getMessage().contains("Collection is not updatable"));
        }
    }

    @Test
    public void testUpdateModifyEntityInCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = modifyEntityInCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Since only an existing person was update, only a single update is generated
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.update(Person.class)
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(afterBuilder);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p1.getName());
    }

    @Test
    public void testUpdateSetCollectionToNull() {
        // Given & When & Then
        try {
            setCollectionToNull();
            fail("Expected the setter of a mutable only field to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Updating the mutable-only attribute 'contacts'"));
        }
    }

    @Test
    public void testUpdateAddNewEntityToCollection() {
        // Given & When & Then
        try {
            addNewEntityToCollection();
            fail("Expected mutating collection operations to fail!");
        } catch (UnsupportedOperationException ex) {
            assertTrue(ex.getMessage().contains("Collection is not updatable"));
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "contacts")
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
