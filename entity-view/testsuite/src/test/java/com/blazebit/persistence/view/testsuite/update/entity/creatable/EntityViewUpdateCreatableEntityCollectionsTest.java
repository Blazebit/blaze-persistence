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

package com.blazebit.persistence.view.testsuite.update.entity.creatable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.entity.creatable.model.UpdatableDocumentEntityWithCollectionsView;
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
public class EntityViewUpdateCreatableEntityCollectionsTest extends AbstractEntityViewUpdateEntityCollectionsTest<UpdatableDocumentEntityWithCollectionsView> {

    public EntityViewUpdateCreatableEntityCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityWithCollectionsView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = replaceCollection();

        // Then
        // We only fetch the document and the collection in full mode
        // During dirty detection we can figure out that effectively nothing changed
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            if (version) {
                versionUpdate(builder);
            }
        }

        builder.validate();

        // No need for an update since it isn't dirty
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // The added person is not loaded, only a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }
        builder.assertInsert()
                .forRelation(Document.class, "people")
            .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // The added person is not loaded, only a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "people")
            .validate();

        // No need for an update since it isn't dirty
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // The added person is not loaded, only a single relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = addToNewCollectionAndModifyEntity();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally a single relation insert is done but without an update
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "people")
            .validate();

        // No need for an update since it isn't dirty
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateModifyEntityInCollection() {
        // Given & When
        final UpdatableDocumentEntityWithCollectionsView docView = modifyEntityInCollection();

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Since only an existing person was update, only a single update is generated
        validateNoChange(docView);
        assertEquals(doc1.getPeople(), docView.getPeople());
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
            builder.update(Document.class);
        }

        if (supportsNullCollectionElements()) {
            builder.assertInsert()
                .forRelation(Document.class, "people")
            .and();
        }

        builder.validate();

        // Since we don't need to merge elements, no need reload the collection
        assertNoUpdateAndReload(docView);

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

        // Since we don't need to merge elements, no need reload the collection
        assertNoUpdateAndReload(docView);
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

        // Since we don't need to merge elements, no need reload the collection
        assertNoUpdateAndReload(docView);
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
