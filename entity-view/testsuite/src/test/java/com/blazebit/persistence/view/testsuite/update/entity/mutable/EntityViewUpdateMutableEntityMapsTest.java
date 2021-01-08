/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.entity.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityMapsTest;
import com.blazebit.persistence.view.testsuite.update.entity.mutable.model.UpdatableDocumentEntityWithMapsView;
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
public class EntityViewUpdateMutableEntityMapsTest extends AbstractEntityViewUpdateEntityMapsTest<UpdatableDocumentEntityWithMapsView> {

    public EntityViewUpdateMutableEntityMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithMapsView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = replaceCollection();

        // Then
        // Since entities are mutable, assert that the document and the people are loaded always loaded.
        // During dirty detection we should be able to figure out that the collection didn't change
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation, but only a single relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        builder.select(Person.class);
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.insert(Document.class, "contacts")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }

        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToNewCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation, but only a single relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        builder.select(Person.class);
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.insert(Document.class, "contacts")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        builder.select(Person.class);
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .insert(Document.class, "contacts")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addToNewCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        builder.select(Person.class);
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .insert(Document.class, "contacts")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }

        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
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
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p1.getName());
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
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }

        if (supportsNullCollectionElements()) {
            builder.assertInsert()
                .forRelation(Document.class, "contacts")
            .and();
        }

        builder.validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }

        afterBuilder.validate();

        if (supportsNullCollectionElements()) {
            assertEquals(doc1.getContacts(), docView.getContacts());
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
            builder.update(Document.class);
        }

        builder.delete(Document.class, "contacts")
                .validate();

        // Since the collection is empty we don't have to care for collection element changes
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                afterBuilder.delete(Document.class, "contacts");
            } else {
                fullFetch(afterBuilder);
            }
            if (version || isQueryStrategy() && isFullMode()) {
                afterBuilder.update(Document.class);
            }
        }

        afterBuilder.validate();
        assertNullMap(docView.getContacts());
        assertEquals(0, doc1.getContacts().size());
    }

    @Test
    public void testUpdateAddNewEntityToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithMapsView docView = addNewEntityToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally the person is persisted and a single relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            builder.select(Person.class);
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(builder);
        }
        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.insert(Person.class)
                .insert(Document.class, "contacts")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        // This time we even have to re-load owned associations because they aren't lazy and could be dirty
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isQueryStrategy()) {
            afterBuilder.select(Person.class);
            afterBuilder.select(Person.class);
            if (isFullMode()) {
                afterBuilder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
            fullFetch(afterBuilder);
        }
        afterBuilder.assertSelect()
                .fetching(Person.class, "favoriteDocuments")
                .fetching(Document.class)
            .and()
            .assertSelect()
                .fetching(Person.class, "localized")
            .and()
            // ownedDocuments2 for the merge call of Person
            .select(Document.class);
        if (version || isQueryStrategy() && isFullMode()) {
            afterBuilder.update(Document.class);
        }
        if (doesJpaMergeOfRecentlyPersistedEntityForceUpdate()) {
            afterBuilder.update(Person.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getContacts(), docView.getContacts());
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
