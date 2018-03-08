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

package com.blazebit.persistence.view.testsuite.update.subview.nested.creatable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.nested.creatable.model.PersonCreateView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.creatable.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.creatable.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.subview.nested.creatable.model.UpdatableResponsiblePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
public class EntityViewUpdateNestedCreatableSubviewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateNestedCreatableSubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableResponsiblePersonView.class);
        cfg.addEntityView(PersonView.class);
        cfg.addEntityView(PersonCreateView.class);
    }

    @Test
    public void testUpdateWithPersonCreateView() {
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When
        PersonCreateView personCreateView = evm.create(PersonCreateView.class);
        personCreateView.setName("newPers");
        docView.getPeople().get(0).setFriend(personCreateView);
        update(docView);

        // Then
        // Assert that only the document is loaded, as we don't need to load the old person
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            }
            builder.insert(Person.class);
            builder.update(Person.class);
            if (version) {
                builder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
//                builder.select(Document.class);
                // Adding elements to a list requires full fetching
                fullFetch(builder);
            }


            builder.insert(Person.class);
            if (version) {
                builder.update(Document.class);
            }
            builder.update(Person.class);
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getPeople().get(0).getFriend().getId(), personCreateView.getId());
        assertEquals("newPers", doc1.getPeople().get(0).getFriend().getName());
        assertSubviewEquals(doc1.getPeople(), docView.getPeople());
    }

    public static void assertSubviewEquals(Collection<Person> persons, Collection<? extends UpdatableResponsiblePersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (UpdatableResponsiblePersonView pSub : personSubviews) {
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

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        fullFetch(builder)
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
