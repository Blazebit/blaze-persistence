/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.entity.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityMapsTest;
import com.blazebit.persistence.view.testsuite.update.entity.updatableonly.model.UpdatableDocumentEntityWithMapsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.persistence.PersistenceException;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public class EntityViewUpdateUpdatableOnlyEntityMapsTest extends AbstractEntityViewUpdateEntityMapsTest<UpdatableDocumentEntityWithMapsView> {

    public EntityViewUpdateUpdatableOnlyEntityMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithMapsView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "contacts" };
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = replaceCollection();

        // Then
        // Since entities are not mutable we can detect nothing changed
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
                versionUpdate(builder);
            } else {
                fullFetch(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done. In partial modes that don't do fetching, a collection recreation is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }

        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "contacts")
                .and();
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToNewCollection();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // When fetching like in full mode, we can do a proper diff and see that a single insert is enough
        // But partial strategies currently favor not fetching, but collection recreations instead
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                if (isFullMode()) {
                    fullFetch(builder);
                } else {
                    builder.select(Document.class)
                            .delete(Document.class, "contacts")
                            .insert(Document.class, "contacts");
                }
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.insert(Document.class, "contacts");
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done but no update for the person
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.insert(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToNewCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // When fetching like in full mode, we can do a proper diff and see that a single insert is enough
        // But partial strategies currently favor not fetching, but collection recreations instead
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                if (isFullMode()) {
                    fullFetch(builder);
                } else {
                    builder.select(Document.class)
                            .delete(Document.class, "contacts")
                            .insert(Document.class, "contacts");
                }
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.insert(Document.class, "contacts");
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateModifyEntityInCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = modifyEntityInCollection();

        // Then
        // Assert that the document and the people are loaded in full mode i.e. a full fetch
        // Since no collection was changed, no insters or updates are done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                if (isFullMode()) {
                    builder.delete(Document.class, "contacts")
                            .insert(Document.class, "contacts");
                    versionUpdate(builder);
                }
            } else {
                fullFetch(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers1", p1.getName());
    }

    @Test
    public void testUpdateAddNullToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addNullToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done for the null element if supported
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }

        if (supportsNullCollectionElements()) {
            builder.insert(Document.class, "contacts");
        }

        builder.validate();

        assertNoUpdateAndReload(docView);

        if (supportsNullCollectionElements()) {
            assertEquals(doc1.getContacts(), docView.getContacts());
        } else {
            assertEquals(doc1.getContacts().size() + 1, docView.getContacts().size());
        }
    }

    @Test
    public void testUpdateSetCollectionToNull() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = setCollectionToNull();

        // Then
        // Assert that only the document is loaded
        // Since only an existing person was update, only a single update is generated
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            versionUpdate(builder);
        }
        builder.delete(Document.class, "contacts")
                .validate();

        // Since the collection is empty we don't have to care for collection element changes
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                afterBuilder.delete(Document.class, "contacts");
                versionUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
            }
        }

        afterBuilder.validate();
        assertNullMap(docView.getContacts());
        assertEquals(0, doc1.getContacts().size());
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
            AssertStatementBuilder builder = assertUnorderedQuerySequence();

            if (isQueryStrategy()) {
                if (isFullMode()) {
                    builder.delete(Document.class, "contacts")
                            .insert(Document.class, "contacts");
                }
            } else {
                fullFetch(builder);
                if (!doesTransientCheckBeforeFlush() && version) {
                    versionUpdate(builder);
                }
            }
            builder.validate();
            clearPersistenceContextAndReload();
            assertEquals(1, doc1.getContacts().size());
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
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "contacts")
                .insert(Document.class, "contacts");
        if (doc1.getContacts().size() > 1) {
            builder.insert(Document.class, "contacts");
        }
        return versionUpdate(builder);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
