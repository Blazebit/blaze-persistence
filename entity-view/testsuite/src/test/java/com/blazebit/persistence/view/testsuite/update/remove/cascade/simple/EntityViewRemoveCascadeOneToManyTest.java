/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.remove.cascade.simple;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.entity.PrimitiveVersion;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.simple.model.PersonIdView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewRemoveCascadeOneToManyTest extends AbstractEntityViewUpdateTest<PersonIdView> {

    private PrimitivePerson person;

    public EntityViewRemoveCascadeOneToManyTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, PersonIdView.class);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                PrimitivePerson.class,
                PrimitiveDocument.class,
                PrimitiveVersion.class
        };
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void prepareData(EntityManager em) {
        person = new PrimitivePerson("pers1");
        em.persist(person);

        PrimitiveDocument doc1 = new PrimitiveDocument("doc1");
        doc1.setOwner(person);
        em.persist(doc1);

        PrimitiveVersion doc1V1 = new PrimitiveVersion();
        doc1V1.setVersionId(1L);
        doc1V1.setDocument(doc1);
        em.persist(doc1V1);
    }

    @Test
    public void testRemoveById() {
        // Given
        clearQueries();

        // When
        remove(PersonIdView.class, person.getId());

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // In the query strategy, we query by owner id
        if (isQueryStrategy()) {
            builder.select(PrimitiveDocument.class)
                .select(PrimitiveVersion.class);
        } else {
            builder.select(PrimitivePerson.class)
                .assertSelect()
                .fetching(PrimitiveDocument.class)
                .fetching(PrimitiveVersion.class)
                .and();
        }

        builder.delete(PrimitiveDocument.class, "contacts")
            .delete(PrimitiveDocument.class, "people")
            .delete(PrimitiveDocument.class, "peopleCollectionBag")
            .delete(PrimitiveDocument.class, "peopleListBag")
            .delete(PrimitiveVersion.class)
            .delete(PrimitiveDocument.class)
            .delete(PrimitivePerson.class)
            .validate();

        clearPersistenceContextAndReload();
        Assert.assertNull(person);
    }

    @Override
    protected void reload() {
        person = em.find(PrimitivePerson.class, person.getId());
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
