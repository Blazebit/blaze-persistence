/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.testsuite.update.entity.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.entity.updatableonly.model.UpdatableDocumentEntityWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.PersistenceException;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateUpdatableOnlyEntityCollectionsTest extends AbstractEntityViewUpdateEntityCollectionsTest<UpdatableDocumentEntityWithCollectionsView> {

    public EntityViewUpdateUpdatableOnlyEntityCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithCollectionsView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = replaceCollection();

        // Then
        // Since entities are not mutable we can detect nothing changed
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            if (version) {
                versionUpdate(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done. In partial modes that don't do fetching, a collection recreation is done
        AssertStatementBuilder builder = assertQuerySequence();

        if (preferLoadingAndDiffingOverRecreate()) {
            fullFetch(builder);
        } else {
            fullFetch(builder);
        }

        if (version) {
            versionUpdate(builder);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .and();
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollection();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // When fetching like in full mode, we can do a proper diff and see that a single insert is enough
        // But partial strategies currently favor not fetching, but collection recreations instead
        AssertStatementBuilder builder = assertQuerySequence();

        if (preferLoadingAndDiffingOverRecreate()) {
            fullFetch(builder);
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class)
                        .assertDelete()
                            .forRelation(Document.class, "people")
                        .and()
                        .assertInsert()
                            .forRelation(Document.class, "people")
                        .and();
            }
        }

        if (version) {
            versionUpdate(builder);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .and();
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done but no update for the person
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (version) {
            versionUpdate(builder);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // When fetching like in full mode, we can do a proper diff and see that a single insert is enough
        // But partial strategies currently favor not fetching, but collection recreations instead
        AssertStatementBuilder builder = assertQuerySequence();

        if (preferLoadingAndDiffingOverRecreate()) {
            fullFetch(builder);
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class)
                        .assertDelete()
                            .forRelation(Document.class, "people")
                        .and()
                        .assertInsert()
                            .forRelation(Document.class, "people")
                        .and();
            }
        }

        if (version) {
            versionUpdate(builder);
        }
        builder.assertInsert()
                .forRelation(Document.class, "people")
                .and();
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateModifyEntityInCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = modifyEntityInCollection();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // Since no collection was changed, no insters or updates are done
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            if (version) {
                versionUpdate(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers1", p1.getName());
    }

    @Test
    public void testUpdateAddNullToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addNullToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done for the null element if supported
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (version) {
            versionUpdate(builder);
        }

        if (supportsNullCollectionElements()) {
            builder.assertInsert()
                .forRelation(Document.class, "people")
            .and();
        }

        builder.validate();

        assertNoUpdateAndReload(docView);

        if (supportsNullCollectionElements()) {
            assertEquals(doc1.getPeople(), docView.getPeople());
        } else {
            assertEquals(doc1.getPeople().size() + 1, docView.getPeople().size());
        }
    }

    @Test
    public void testUpdateSetCollectionToNull() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = setCollectionToNull();

        // Then
        // Assert that only the document is loaded
        // Since only an existing person was update, only a single update is generated
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            builder.select(Document.class);
        }
        if (version) {
            versionUpdate(builder);
        }

        builder.assertDelete()
                    .forRelation(Document.class, "people")
                .validate();

        // Since the collection is empty we don't have to care for collection element changes
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            if (version) {
                versionUpdate(afterBuilder);
            }
        }

        afterBuilder.validate();
        assertNullCollection(docView.getPeople());
        assertEquals(0, doc1.getPeople().size());
    }

    @Test
    public void testUpdateAddNewEntityToCollection() {
        try {
            // Given & When
            addNewEntityToCollection();
            fail("Expected a transient reference error for the new person!");
        } catch (PersistenceException | IllegalStateException ex) {
            // Then
            assertTrue(ex.getMessage().contains("transient"));
            AssertStatementBuilder builder = assertQuerySequence();

            fullFetch(builder);

            if (version) {
                versionUpdate(builder);
            }
            builder.validate();
            restartTransactionAndReload();
            assertEquals(1, doc1.getPeople().size());
        }
    }

    @Override
    protected boolean isQueryStrategy() {
        // Collection changes always need to be applied on the entity model, can't do that via a query
        return false;
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
