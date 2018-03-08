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
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityTest;
import com.blazebit.persistence.view.testsuite.update.entity.creatable.model.UpdatableDocumentEntityView;
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
public class EntityViewUpdateCreatableEntityTest extends AbstractEntityViewUpdateEntityTest<UpdatableDocumentEntityView> {

    public EntityViewUpdateCreatableEntityTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        final UpdatableDocumentEntityView docView = simpleUpdate();

        // Then
        // Assert that not only the document is loaded
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
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

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
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

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p2.getId(), docView.getResponsiblePerson().getId());
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("pers2", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithModifyExisting();

        // Then
        // Since we update the old responsiblePerson, load it along with the document for updating it later
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullUpdate(builder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
                if (version) {
                    fullUpdate(builder);
                }
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("pers1", doc1.getResponsiblePerson().getName());
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

        assertNoUpdateAndReload(docView);
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
            if (isFullMode()) {
                fullUpdate(afterBuilder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(afterBuilder);
                if (version) {
                    fullUpdate(afterBuilder);
                }
            }
        }
        afterBuilder.validate();
        assertEquals(docView.getResponsiblePerson().getId(), doc1.getResponsiblePerson().getId());
        assertEquals("newPerson", doc1.getResponsiblePerson().getName());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
