/*
 * Copyright 2014 - 2019 Blazebit.
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
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.listener.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateListenerCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateListenerCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
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
        return new String[] { "people" };
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();

        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        update(docView);

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        docView.getPeople().add(newPerson);

        saveWith(docView, flushOperationBuilder -> {
            flushOperationBuilder.onPreUpdate(UpdatableDocumentWithCollectionsView.class, view -> {
                PluralChangeModel<UpdatableDocumentWithCollectionsView, UpdatablePersonView> changeModel = (PluralChangeModel<UpdatableDocumentWithCollectionsView, UpdatablePersonView>) (ChangeModel<?>) evm.getChangeModel(view).get("people");
                assertEquals(newPerson, changeModel.getAddedElements().get(0).getCurrentState());
            });
        });

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        docView.getPeople().add(newPerson);
        update(docView);

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.getPeople().add(newPerson);
        update(docView);

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testClearUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.getPeople().add(newPerson);
        List<UpdatablePersonView> copy = new ArrayList<>(docView.getPeople());
        docView.getPeople().clear();
        docView.getPeople().addAll(copy);
        update(docView);

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToCollectionCreate() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);

        // When
        newPerson.setName("newPerson");
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        docView.getPeople().add(newPerson);
        saveWith(docView, flusherBuilder -> {
            flusherBuilder.onPrePersist(UpdatablePersonView.class, Person.class,(view, entity) -> {
                entity.setAge(10L);
            });
        });

        // Then
        restartTransactionAndReload();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
        assertEquals("newPerson", doc1.getPeople().get(1).getName());
        assertEquals(10L, doc1.getPeople().get(1).getAge());
    }

    public void assertSubviewEquals(Collection<Person> persons, Collection<UpdatablePersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (UpdatablePersonView pSub : personSubviews) {
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
