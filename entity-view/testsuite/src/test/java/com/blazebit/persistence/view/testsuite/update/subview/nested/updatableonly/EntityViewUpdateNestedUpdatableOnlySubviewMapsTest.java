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

package com.blazebit.persistence.view.testsuite.update.subview.nested.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.nested.updatableonly.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.updatableonly.model.UpdatableFriendPersonView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.updatableonly.model.UpdatableResponsiblePersonView;
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
public class EntityViewUpdateNestedUpdatableOnlySubviewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateNestedUpdatableOnlySubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableResponsiblePersonView.class);
        cfg.addEntityView(UpdatableFriendPersonView.class);
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatableResponsiblePersonView newPerson = getP2View(UpdatableResponsiblePersonView.class);
        clearQueries();
        
        // When
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                // In full mode we need to select the added element for cascading the update
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
        UpdatableResponsiblePersonView newPerson = getP2View(UpdatableResponsiblePersonView.class);
        clearQueries();

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                // In full mode we need to select the added element for cascading the update
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
        UpdatableResponsiblePersonView newPerson = getP2View(UpdatableResponsiblePersonView.class);
        clearQueries();

        // When
        newPerson.getFriend().setName("newPerson");
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                // In full mode we need to select the added element for cascading the update
                builder.select(Person.class);
            }
        }
        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                    .forRelation(Document.class, "contacts")
                .and()
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers4", p4.getName());
        docView.getContacts().get(2).getFriend().setName("pers4");
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatableResponsiblePersonView newPerson = getP2View(UpdatableResponsiblePersonView.class);
        clearQueries();

        // When
        newPerson.getFriend().setName("newPerson");
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
        } else {
            if (isFullMode()) {
                // In full mode we need to select the added element for cascading the update
                builder.select(Person.class);
            }
        }
        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                    .forRelation(Document.class, "contacts")
                .and()
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("pers4", p4.getName());
        docView.getContacts().get(2).getFriend().setName("pers4");
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateModifyCollectionElement() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatableFriendPersonView newFriend = getPersonView(p4.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        docView.getContacts().get(1).setFriend(newFriend);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            }
            builder.update(Person.class);
            if (version) {
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

        assertNoCollectionUpdateAndReload(docView);
        assertEquals(p4.getId(), p1.getFriend().getId());
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateModifyCollectionElementCopy() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatableFriendPersonView newFriend = getPersonView(p3.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        newFriend.setName("newFriend");
        docView.getContacts().get(1).setFriend(newFriend);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder).update(Person.class);
                if (version) {
                    versionUpdate(builder);
                }
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
                if (version) {
                    versionUpdate(builder);
                }
            }
        }

        builder.validate();

        assertNoCollectionUpdateAndReload(docView);
        assertEquals(p3.getId(), p1.getFriend().getId());
        assertEquals("pers3", p3.getName());
        docView.getContacts().get(1).getFriend().setName("pers3");
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateModifyCollectionElementAndModify() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        UpdatableFriendPersonView newFriend = getPersonView(p4.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        newFriend.setName("newFriend");
        docView.getContacts().get(1).setFriend(newFriend);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            }
            builder.update(Person.class);
            if (version) {
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

        assertNoCollectionUpdateAndReload(docView);
        assertEquals(p4.getId(), p1.getFriend().getId());
        assertEquals("pers4", p4.getName());
        docView.getContacts().get(1).getFriend().setName("pers4");
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    @Test
    public void testUpdateModifyCollectionElementSetToNull() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().get(1).setFriend(null);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            }
            builder.update(Person.class);
            if (version) {
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

        assertNoCollectionUpdateAndReload(docView);
        assertNull(p1.getFriend());
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
    }

    public static void assertSubviewEquals(Map<Integer, Person> persons, Map<Integer, ? extends UpdatableResponsiblePersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<Integer, Person> entry : persons.entrySet()) {
            Person p = entry.getValue();
            boolean found = false;
            UpdatableResponsiblePersonView pSub = personSubviews.get(entry.getKey());
            if (pSub != null) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    if (p.getFriend() == null) {
                        assertNull(pSub.getFriend());
                    } else {
                        assertNotNull(pSub.getFriend());
                        assertEquals(p.getFriend().getId(), pSub.getFriend().getId());
                        assertEquals(p.getFriend().getName(), pSub.getFriend().getName());
                    }
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getName());
            }
        }
    }

    private void assertNoCollectionUpdateAndReload(UpdatableDocumentWithMapsView docView) {
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(afterBuilder).update(Person.class);
                if (version) {
                    versionUpdate(afterBuilder);
                }
            }
        } else {
            if (isFullMode()) {
                fullFetch(afterBuilder);
                if (version) {
                    versionUpdate(afterBuilder);
                }
            }
        }

        afterBuilder.validate();
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
            builder.update(Document.class);
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
