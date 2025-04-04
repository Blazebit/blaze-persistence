/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.graph;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.PersonIdView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatableDocumentWithGraphView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatableFriendPersonView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatableNestedPersonView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatableOwnerPersonView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatablePersonView;
import com.blazebit.persistence.view.testsuite.update.subview.graph.model.UpdatableSimpleDocumentView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
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
public class EntityViewUpdateSubviewGraphTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithGraphView> {

    public EntityViewUpdateSubviewGraphTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithGraphView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(DocumentIdView.class);
        cfg.addEntityView(PersonIdView.class);
        cfg.addEntityView(UpdatableOwnerPersonView.class);
        cfg.addEntityView(UpdatableNestedPersonView.class);
        cfg.addEntityView(UpdatableFriendPersonView.class);
        cfg.addEntityView(UpdatablePersonView.class);
        cfg.addEntityView(UpdatableSimpleDocumentView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "people" };
    }

    @Override
    protected void prepareData(EntityManager em) {
        super.prepareData(em);

        Person newPers = new Person("secondPers");
        newPers.getNameObject().setPrimaryName("secondPers");
        newPers.getLocalized().put(1, "secondLocalized");
        em.persist(newPers);
        doc1.getPeople().add(newPers);
    }

    @Test
    public void testUpdateAddToCollectionAndSet() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatableNestedPersonView newPerson = getP2View(UpdatableNestedPersonView.class);
        UpdatableFriendPersonView newPartner = getPersonView(p4.getId(), UpdatableFriendPersonView.class);
        clearQueries();

        // When
        docView.getPeople().add(newPerson);
        docView.getPartners().add(newPartner);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "people")
                        .insert(Document.class, "people")
                        .insert(Document.class, "people");
                builder.delete(Person.class, "favoriteDocuments");
                builder.update(Person.class)
                        .update(Person.class)
                        .update(Person.class);
            }
            builder.update(Person.class);

            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Document.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Person.class);
            }
            if (version || isFullMode()) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                // Only need to load the "people" to add an element
                // Since "partners" is an inverse collection, we transform the add to an update
                builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Document.class, "people")
                        .fetching(Person.class)
                        .and();
            }

            if (!isFullMode() && version) {
                builder.update(Document.class);
            }

            builder.update(Person.class);
            builder.select(Person.class);

            if (isFullMode()) {
                builder.assertSelect()
                        .fetching(Person.class)
                        .fetching(Person.class) // friend
                        .fetching(Person.class) // friend.friend
                        .fetching(Document.class) // partnerDocument
                        .and();
                if (version) {
                    builder.update(Document.class);
                }
            }
        }

        builder.insert(Document.class, "people")
                .validate();

        assertNoUpdateAndReload(docView, true);
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    @Test
    public void testUpdateDifferentNestedGraphs() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        docView.getPeople().add(getP2View(UpdatableNestedPersonView.class));
        docView.getPeople().get(1).setPartnerDocument(getDocumentView(doc2.getId(), UpdatableSimpleDocumentView.class));
        update(docView);
        clearQueries();

        // When
        docView.getPeople().get(0).getFriend().setName("testFriend");
        docView.getPeople().get(1).getPartnerDocument().setName("testDoc");
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "people")
                        .insert(Document.class, "people")
                        .insert(Document.class, "people")
                        .insert(Document.class, "people");
                builder.delete(Person.class, "favoriteDocuments");
                builder.update(Person.class);
            }

            builder.update(Person.class);

            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Person.class);
            }
            builder.update(Document.class);
            if (isFullMode()) {
                builder.update(Person.class)
                        .update(Document.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Person.class);
            }
            if (version || isFullMode()) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                // Only need to load the "people" to add an element
                // Since "partners" is an inverse collection, we transform the add to an update
                builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Document.class, "people")
                        .fetching(Person.class)
                        .fetching(Person.class)
                        .fetching(Document.class)
                        .and();
            }

            if (version) {
                builder.update(Document.class);
            }

            builder.assertUpdate()
                    .forEntity(Person.class)
                    .and();
            builder.assertUpdate()
                    .forEntity(Document.class)
                    .and();
        }

        builder.validate();

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isFullMode()) {
            if (isQueryStrategy()) {
                afterBuilder.delete(Document.class, "people")
                        .insert(Document.class, "people")
                        .insert(Document.class, "people")
                        .insert(Document.class, "people");
                afterBuilder.delete(Person.class, "favoriteDocuments");
                afterBuilder.update(Person.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Document.class)
                        .update(Person.class)
                        .update(Document.class)
                        .update(Person.class)
                        .update(Person.class)
                        .update(Person.class);
                afterBuilder.update(Document.class);
            } else {
                fullFetch(afterBuilder);
                if (version) {
                    afterBuilder.update(Document.class);
                }
            }
        }
        afterBuilder.validate();
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    public void assertSubviewEquals(Collection<Person> persons, Collection<UpdatableNestedPersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (UpdatableNestedPersonView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    if (p.getFriend() == null) {
                        assertNull(pSub.getFriend());
                    } else {
                        assertNotNull(pSub.getFriend());
                        assertEquals(p.getFriend().getId(), pSub.getFriend().getId());
                        assertEquals(p.getFriend().getName(), pSub.getFriend().getName());

                        if (p.getFriend().getFriend() == null) {
                            assertNull(pSub.getFriend().getFriend());
                        } else {
                            assertNotNull(pSub.getFriend().getFriend());
                            assertEquals(p.getFriend().getFriend().getId(), pSub.getFriend().getFriend().getId());
                            assertEquals(p.getFriend().getFriend().getName(), pSub.getFriend().getFriend().getName());
                        }
                    }
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
                .fetching(Person.class) // owner
                .fetching(Person.class, "favoriteDocuments")
                .fetching(Document.class)
                .fetching(Person.class) // partner
                .fetching(Person.class) // partner.friend
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .fetching(Person.class) // friend
                .fetching(Person.class) // friend
                .fetching(Document.class) // partnerDocument
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "people")
                .insert(Document.class, "people")
                .insert(Document.class, "people")
                .insert(Document.class, "people")
                .delete(Person.class, "favoriteDocuments")
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Document.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class);
        builder.update(Document.class);

        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
