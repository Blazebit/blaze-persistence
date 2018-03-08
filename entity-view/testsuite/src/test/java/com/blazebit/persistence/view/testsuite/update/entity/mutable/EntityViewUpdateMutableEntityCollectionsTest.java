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

package com.blazebit.persistence.view.testsuite.update.entity.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.entity.mutable.model.UpdatableDocumentEntityWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Iterator;

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
public class EntityViewUpdateMutableEntityCollectionsTest extends AbstractEntityViewUpdateEntityCollectionsTest<UpdatableDocumentEntityWithCollectionsView> {

    public EntityViewUpdateMutableEntityCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithCollectionsView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = replaceCollection();

        // Then
        // Since entities are mutable, assert that the document and the people are loaded always loaded.
        // During dirty detection we should be able to figure out that the collection didn't change
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (version) {
            builder.update(Document.class);
        }

        builder.validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation, but only a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder)
                .select(Person.class);
        if (version) {
            builder.update(Document.class);
        }
        builder.assertInsert()
                .forRelation(Document.class, "people")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation, but only a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder)
                .select(Person.class);
        if (version) {
            builder.update(Document.class);
        }
        builder.assertInsert()
                .forRelation(Document.class, "people")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder)
                .select(Person.class);
        if (version) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .assertInsert()
                .forRelation(Document.class, "people")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder)
                .select(Person.class);
        if (version) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .assertInsert()
                .forRelation(Document.class, "people")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateModifyEntityInCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = modifyEntityInCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Since only an existing person was update, only a single update is generated
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (version) {
            builder.update(Document.class);
        }
        builder.update(Person.class)
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", p1.getName());
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
            builder.update(Document.class);
        }

        if (supportsNullCollectionElements()) {
            builder.assertInsert()
                .forRelation(Document.class, "people")
            .and();
        }

        builder.validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder);
        if (version) {
            afterBuilder.update(Document.class);
        }
        afterBuilder.validate();

        if (supportsNullCollectionElements()) {
            assertEquals(doc1.getPeople(), docView.getPeople());
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
            builder.update(Document.class);
        }

        builder.assertDelete()
                    .forRelation(Document.class, "people")
                .validate();

        // Since the collection is empty we don't have to care for collection element changes
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            if (version) {
                afterBuilder.update(Document.class);
            }
        }

        afterBuilder.validate();
        assertNullCollection(docView.getPeople());
        assertEquals(0, doc1.getPeople().size());
    }

    @Test
    public void testUpdateAddNewEntityToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addNewEntityToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because of the merge invocation
        // Finally the person is persisted and a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder)
                .insert(Person.class);
        if (version) {
            builder.update(Document.class);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        // This time we even have to re-load owned associations because they aren't lazy and could be dirty
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        fullFetch(afterBuilder)
                .assertSelect()
                    .fetching(Person.class, "favoriteDocuments")
                    .fetching(Document.class)
                .and()
                .assertSelect()
                    .fetching(Person.class, "localized")
                .and();
        if (version) {
            afterBuilder.update(Document.class);
        }
        if (doesJpaMergeOfRecentlyPersistedEntityForceUpdate()) {
            afterBuilder.update(Person.class);
        }
        afterBuilder.validate();
        assertEquals(doc1.getPeople(), docView.getPeople());
        Iterator<Person> iter = doc1.getPeople().iterator();
        Person nextPerson = iter.next();
        if (nextPerson.getId().equals(p1.getId())) {
            assertEquals("pers1", nextPerson.getName());
            assertEquals("newPerson", iter.next().getName());
        } else {
            assertEquals("newPerson", nextPerson.getName());
            assertEquals("pers1", iter.next().getName());
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
