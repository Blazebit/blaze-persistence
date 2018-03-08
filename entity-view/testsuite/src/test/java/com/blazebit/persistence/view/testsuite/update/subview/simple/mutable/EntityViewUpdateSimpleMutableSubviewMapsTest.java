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
import com.blazebit.persistence.view.testsuite.update.subview.simple.mutable.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.mutable.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleMutableSubviewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateSimpleMutableSubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
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
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        update(docView);

        // Then
        // Assert that the document and the people are loaded in full mode.
        // During dirty detection we should be able to figure out that nothing changed
        // So partial modes wouldn't load anything and both won't cause any updates
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            if (isQueryStrategy()) {
                builder.update(Person.class);
            }
            if (version) {
                builder.update(Document.class);
            }
        }

        builder.validate();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();
        
        // When

        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded, but only a relation insert is done
        // The full mode also has to load the person that is added and apply the changes
        // But since nothing is changed, no update is subsequently generated
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                builder.select(Person.class);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        // The full mode also has to load the person that is added and apply the changes
        // But since nothing is changed, no update is subsequently generated
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                builder.select(Person.class);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                    .forRelation(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because it is dirty
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }

            builder.update(Person.class);

            if (version) {
                builder.update(Document.class);
            }
        } else {
            builder.select(Person.class);
            if (version) {
                builder.update(Document.class);
            }

            builder.update(Person.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // In addition, the new person is loaded because it is dirty
        // Finally a single relation insert is done and an update to the person is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }

            builder.update(Person.class);

            if (version) {
                builder.update(Document.class);
            }
        } else {
            builder.select(Person.class);
            if (version) {
                builder.update(Document.class);
            }

            builder.update(Person.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
    }

    public static void assertSubviewEquals(Map<Integer, Person> persons, Map<Integer, ? extends UpdatablePersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<Integer, Person> entry : persons.entrySet()) {
            Person p = entry.getValue();
            boolean found = false;
            UpdatablePersonView pSub = personSubviews.get(entry.getKey());
            if (pSub != null) {
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
        fullFetch(builder)
                .update(Person.class)
                .update(Person.class);
        if (version) {
            versionUpdate(builder);
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
