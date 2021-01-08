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

package com.blazebit.persistence.view.testsuite.update.listener;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.listener.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatablePersonView;
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
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateListenerMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateListenerMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonView.class);
        cfg.addEntityView(UpdatablePersonView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "contacts" };
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When

        docView.getContacts().put(2, newPerson);
        saveWith(docView, flushOperationBuilder -> {
            flushOperationBuilder.onPreUpdate(UpdatableDocumentWithMapsView.class, view -> {
                MapChangeModel<UpdatableDocumentWithMapsView, UpdatablePersonView> changeModel = (MapChangeModel<UpdatableDocumentWithMapsView, UpdatablePersonView>) (ChangeModel<?>) evm.getChangeModel(view).get("contacts");
                assertEquals(newPerson, changeModel.getAddedElements().get(0).getCurrentState());
            });
        });

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testClearUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.getContacts().put(2, newPerson);
        HashMap<Integer, UpdatablePersonView> copy = new HashMap<>(docView.getContacts());
        docView.getContacts().clear();
        docView.getContacts().putAll(copy);
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToCollectionCreate() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        saveWith(docView, flusherBuilder -> {
            flusherBuilder.onPrePersist(UpdatablePersonView.class, Person.class,(view, entity) -> {
                entity.setAge(10L);
            });
        });

        // Then
        clearPersistenceContextAndReload();
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
        assertEquals("newPerson", doc1.getContacts().get(2).getName());
        assertEquals(10L, doc1.getContacts().get(2).getAge());
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
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder;
    }
}
