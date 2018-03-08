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
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityTest;
import com.blazebit.persistence.view.testsuite.update.entity.mutable.model.UpdatableDocumentEntityView;
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
public class EntityViewUpdateMutableEntityTest extends AbstractEntityViewUpdateEntityTest<UpdatableDocumentEntityView> {

    public EntityViewUpdateMutableEntityTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        final UpdatableDocumentEntityView docView = simpleUpdate();

        // Then
        // Assert that not only the document is loaded, but also always the responsiblePerson
        // This might be unexpected for partial strategies
        // but since we don't know if entities are dirty, we need to be conservative and load the object
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            builder.assertSelect()
                    .fetching(Person.class)
                    .and();
        } else {
            fullFetch(builder);
        }

        builder.update(Document.class);
        builder.validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.assertSelect()
                        .fetching(Person.class)
                    .and();

            if (isFullMode() || version) {
                afterBuilder.update(Document.class);
            }
        } else {
            afterBuilder.assertSelect()
                    .fetching(Document.class, Person.class)
                    .and();
            if (version) {
                afterBuilder.update(Document.class);
            }
        }

        afterBuilder.validate();
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testUpdateWithEntity() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithEntity();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        // Unfortunately, the new responsiblePerson has to be loaded by the JPA provider since it has to be merged
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.select(Person.class)
                .update(Document.class)
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.assertSelect()
                    .fetching(Person.class)
                    .and();
            if (isFullMode() || version) {
                afterBuilder.update(Document.class);
            }
        } else {
            afterBuilder.assertSelect()
                    .fetching(Document.class, Person.class)
                    .and();
            if (version) {
                afterBuilder.update(Document.class);
            }
        }
        afterBuilder.validate();
        assertEquals(p2.getId(), docView.getResponsiblePerson().getId());
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
    }

    @Test
    public void testUpdateWithModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithModifyEntity();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        // Unfortunately, the new responsiblePerson has to be loaded by the JPA provider since it has to be merged
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.select(Person.class)
                .update(Document.class)
                .update(Person.class)
                .validate();


        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.assertSelect()
                    .fetching(Person.class)
                    .and();
            if (isFullMode() || version) {
                afterBuilder.update(Document.class);
            }
        } else {
            // Unfortunately, even after an update, we have to reload the entity to merge again
            afterBuilder.assertSelect()
                    .fetching(Document.class, Person.class)
                    .and();
            if (version) {
                afterBuilder.update(Document.class);
            }
        }

        afterBuilder.validate();
        assertEquals(p2.getId(), docView.getResponsiblePerson().getId());
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("newOwner", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithModifyExisting();

        // Then
        // Since we update the old responsiblePerson, load it along with the document for updating it later
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            builder.assertSelect()
                    .fetching(Person.class)
                    .and();
        } else {
            fullFetch(builder);
        }

        if (isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        } else if (version) {
            builder.update(Document.class);
        }

        builder.update(Person.class);

        builder.validate();

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.assertSelect()
                    .fetching(Person.class)
                    .and();
            if (isFullMode() || version) {
                afterBuilder.update(Document.class);
            }
        } else {
            // Unfortunately, even after an update, we have to reload the entity to merge again
            afterBuilder.assertSelect()
                    .fetching(Document.class, Person.class)
                    .and();
            if (version) {
                afterBuilder.update(Document.class);
            }
        }

        afterBuilder.validate();
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("newOwner", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateToNull() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateToNull();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        // Since the new responsiblePerson is null, we don't need to do anything further
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class)
                .validate();

        // Updating to null thankfully does not require reloading the relation
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                afterBuilder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(afterBuilder);
                if (version) {
                    afterBuilder.update(Document.class);
                }
            }
        }
        afterBuilder.validate();
        assertNull(doc1.getResponsiblePerson());
    }

    @Test
    public void testUpdateToNewPerson() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateToNewPerson();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        // The new responsiblePerson will be persisted
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.insert(Person.class)
                .update(Document.class)
                .validate();

        // We always have to do a full fetch because the entity might be dirty
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            afterBuilder.assertSelect()
                    .fetching(Person.class)
                    .and();
        } else {
            fullFetch(afterBuilder);
        }
        // Unfortunately, Hibernate has to fetch all non-inverse collections and merge changes
        afterBuilder.assertSelect()
                    .fetching(Person.class, "favoriteDocuments")
                    .fetching(Document.class)
                .and()
                .assertSelect()
                    .fetching(Person.class, "localized")
                .and();

        if (isQueryStrategy()) {
            if (doesJpaMergeOfRecentlyPersistedEntityForceUpdate()) {
                afterBuilder.update(Person.class);
            }
            if (isFullMode() || version) {
                afterBuilder.update(Document.class);
            }
        } else if (version) {
            afterBuilder.update(Document.class);
            if (doesJpaMergeOfRecentlyPersistedEntityForceUpdate()) {
                afterBuilder.update(Person.class);
            }
        } else if (doesJpaMergeOfRecentlyPersistedEntityForceUpdate()) {
            afterBuilder.update(Person.class);
        }

        afterBuilder.validate();
        assertEquals("newPerson", doc1.getResponsiblePerson().getName());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.assertUpdate()
                .forEntity(Document.class)
                .and()
                .assertUpdate()
                .forEntity(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
