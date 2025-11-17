/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.simple.creatable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.simple.creatable.model.PersonCreateView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.creatable.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.simple.creatable.model.UpdatableDocumentWithMapsView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public class EntityViewUpdateSimpleCreatableSubviewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateSimpleCreatableSubviewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonView.class);
        cfg.addEntityView(PersonCreateView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "contacts" };
    }

    @Test
    public void testUpdateWithPersonCreateView() {
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();

        // When
        PersonCreateView personCreateView = evm.create(PersonCreateView.class);
        personCreateView.setName("newPers");
        docView.addContact(2, personCreateView);
        update(docView);

        // Then
        // Assert that only the document is loaded, as we don't need to load the old person
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.delete(Document.class, "contacts")
                        .insert(Document.class, "contacts");
            }
        } else {
//            builder.select(Document.class);
            // Adding elements to a list requires full fetching
            fullFetch(builder);
        }

        builder.insert(Person.class);

        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        builder.insert(Document.class, "contacts")
                .validate();

        assertNoUpdateAndReload(docView, true);
        assertEquals(doc1.getContacts().get(2).getId(), personCreateView.getId());
        assertEquals("newPers", doc1.getContacts().get(2).getName());
        assertSubviewEquals(doc1.getContacts(), docView.getContacts());
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

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.delete(Document.class, "contacts")
                .insert(Document.class, "contacts")
                .insert(Document.class, "contacts");
        return versionUpdate(builder);
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
