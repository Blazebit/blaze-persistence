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

package com.blazebit.persistence.view.testsuite.update.subview.simple.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.simple.updatableonly.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.updatableonly.model.UpdatableDocumentWithMapsView;
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
public class EntityViewUpdateSimpleUpdatableOnlySubviewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateSimpleUpdatableOnlySubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonView.class);
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
            if (version) {
                builder.update(Document.class);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();
        
        // When
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // Assert that the document and the people are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

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
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                assertReplaceAnd(builder);
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
    public void testUpdateAddToCollectionAndModifySubview() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                assertReplaceAnd(builder);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "contacts")
                .validate();
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts().size(), docView.getContacts().size());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModifySubview() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        PersonView newPerson = getP2View(PersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the people are also loaded
        // Since we load the people in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                assertReplaceAnd(builder);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "contacts")
                .validate();
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts().size(), docView.getContacts().size());
        assertEquals("pers2", p2.getName());
    }

    @Test
    public void testUpdateModifySubviewInCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().get(1).setName("newPerson");
        update(docView);

        // Then
        // Nothing is loaded since nothing that should be cascaded changed
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            if (version) {
                builder.update(Document.class);
            }
        }
        builder.validate();
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getContacts().size(), docView.getContacts().size());
        assertEquals("pers1", p1.getName());
    }

    public static void assertSubviewEquals(Map<Integer, Person> persons, Map<Integer, PersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<Integer, Person> entry : persons.entrySet()) {
            Person p = entry.getValue();
            boolean found = false;
            PersonView pSub = personSubviews.get(entry.getKey());
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

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "contacts")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "contacts")
                .and();
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
                .fetching(Document.class, "contacts")
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
