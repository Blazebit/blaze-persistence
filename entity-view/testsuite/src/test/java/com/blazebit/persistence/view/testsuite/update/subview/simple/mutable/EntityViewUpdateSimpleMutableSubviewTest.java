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

package com.blazebit.persistence.view.testsuite.update.subview.simple.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.simple.mutable.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.mutable.model.UpdatablePersonView;
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
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleMutableSubviewTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentView> {

    public EntityViewUpdateSimpleMutableSubviewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatablePersonView.class);
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
        // Since only the document changed we don't need to load the owner
        // Just assert the update is properly done
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
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
        UpdatablePersonView newOwner = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        docView.setResponsiblePerson(newOwner);
        update(docView);

        // Then
        // Since the owner changed we don't need to load the old owner
        // The new owner also doesn't have to be loaded, except in full mode
        AssertStatementBuilder builder = assertQuerySequence();;

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder)
                        .select(Person.class);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p2.getId(), doc1.getResponsiblePerson().getId());
    }

    @Test
    public void testUpdateWithModifySubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatablePersonView newOwner = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        newOwner.setName("newPerson");
        docView.setResponsiblePerson(newOwner);
        update(docView);

        // Then
        // Since the owner changed we don't need to load the old owner
        // Now the new owner has to be loaded as it is updated as well
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            builder.update(Person.class)
                    .update(Document.class);
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
            builder.select(Person.class)
                    .update(Document.class)
                    .update(Person.class);
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p2.getId(), doc1.getResponsiblePerson().getId());
        assertEquals("newPerson", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.getResponsiblePerson().setName("newPerson");
        update(docView);

        // Then
        // Since we update the old responsiblePerson, load it along with the document for updating it later
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
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
        assertEquals("newPerson", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateToNull() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.setResponsiblePerson(null);
        update(docView);

        // Then
        // Assert that only the document is loaded and finally also updated
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

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                afterBuilder.update(Document.class);
            } else {
                fullFetch(afterBuilder);
                if (version) {
                    afterBuilder.update(Document.class);
                }
            }
        }
        afterBuilder.validate();
        Assert.assertNull(doc1.getResponsiblePerson());
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
        return builder.update(Person.class)
                .update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
