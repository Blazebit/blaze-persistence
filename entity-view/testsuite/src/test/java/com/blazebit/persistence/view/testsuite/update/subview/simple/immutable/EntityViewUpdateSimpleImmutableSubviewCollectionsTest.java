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

package com.blazebit.persistence.view.testsuite.update.subview.simple.immutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.simple.immutable.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.immutable.model.UpdatableDocumentWithCollectionsView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleImmutableSubviewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateSimpleImmutableSubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "people" };
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        update(docView);

        // Then
        // Assert that the document and the people are loaded in full mode.
        // During dirty detection we should be able to figure out that nothing changed
        // So partial modes wouldn't load anything and both won't cause any updates
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                assertReplaceAnd(builder);
                builder.update(Document.class);
            } else {
                fullFetch(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();
        
        // When
        docView.getPeople().add(newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                assertReplaceAnd(builder);
            }
        } else {
            fullFetch(builder);
        }

        if (version || isFullMode() && isQueryStrategy()) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "people")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();

        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        docView.getPeople().add(newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                assertReplaceAnd(builder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                if (preferLoadingAndDiffingOverRecreate()) {
                    fullFetch(builder);
                } else {
                    assertReplaceAnd(builder);
                }
            }
        }

        if (version || isFullMode() && isQueryStrategy()) {
            builder.update(Document.class);
        }
        builder.assertInsert()
                    .forRelation(Document.class, "people")
                .validate();
        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    public static void assertSubviewEquals(Collection<Person> persons, Collection<PersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (PersonView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getName());
            }
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        builder.delete(Document.class, "people")
                .insert(Document.class, "people");
        if (doc1.getPeople().size() > 1) {
            builder.insert(Document.class, "people");
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return versionUpdate(assertReplaceAnd(builder));
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
