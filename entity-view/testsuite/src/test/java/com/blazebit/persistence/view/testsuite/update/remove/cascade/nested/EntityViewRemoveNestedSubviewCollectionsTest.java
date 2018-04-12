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

package com.blazebit.persistence.view.testsuite.update.remove.cascade.nested;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.AbstractEntityViewRemoveDocumentTest;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.FriendPersonView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model.UpdatableResponsiblePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewRemoveNestedSubviewCollectionsTest extends AbstractEntityViewRemoveDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewRemoveNestedSubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableResponsiblePersonView.class);
        cfg.addEntityView(FriendPersonView.class);
    }

    @Test
    public void testSimpleRemove() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When
        remove(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence().unordered();

        if (!isQueryStrategy()) {
            // Hibernate loads the entities before deleting?
            builder.select(Person.class)
                    .select(Person.class)
                    .select(Document.class)
                    .select(Version.class);
        }

        deleteDocumentOwned(builder);
        deletePersonOwned(builder);
        deletePersonOwned(builder);

        // document.people.friend
        builder.delete(Person.class)
                // document.people
                .delete(Person.class)
                // document.versions
                .delete(Version.class)
                .delete(Document.class)
                .validate();

        restartTransactionAndReload();
        assertNull(doc1);
        assertNull(p1);
        assertNull(p3);
    }

    @Test
    public void testRemoveById() {
        // Given
        clearQueries();

        // When
        remove(UpdatableDocumentWithCollectionsView.class, doc1.getId());

        // Then
        AssertStatementBuilder builder = assertQuerySequence().unordered();

        if (!isQueryStrategy()) {
            // Hibernate loads the entities before deleting?
            builder.select(Version.class);
            // Hibernate flushes the changes done to the document because Person#1 was deleted => responsiblePerson set to NULL
            builder.update(Document.class);
            // Select for deletion because of possible cycle
            // document.people.id
            builder.select(Document.class);
        }

        deleteDocumentOwned(builder, true);
        deletePersonOwned(builder, false);
        deletePersonOwned(builder, true);

        // document.people
        if (isQueryStrategy()) {
            // People ids are returned instead of queried if possible
            if (dbmsDialect.supportsReturningColumns()) {
                // But unfortunately current Hibernate versions also try to do the delete implicitly
                builder.assertDelete().forRelation(Document.class, "people").and();
            } else {
                // The JPQL that joins people unfortunately joins all tables, though the collection table alone would suffice
                builder.assertSelect()
                        .forEntity(Document.class)
                        .forRelation(Document.class, "people")
                        .fetching(Person.class)
                        .and();
            }
        } else {
            builder.assertSelect()
                    .fetching(Document.class, "people")
                    .fetching(Person.class)
                    .and();
        }

        // If possible, the deletion of document.people returns document.people.friend
        if (!isQueryStrategy() || !dbmsDialect.supportsReturningColumns()) {
            builder.select(Person.class);
        }

        builder.delete(Person.class)
                // document.people.friend
                .delete(Person.class)
                // document.versions
                .delete(Version.class)
                .delete(Document.class)
                .validate();

        restartTransactionAndReload();
        assertNull(doc1);
        assertNull(p1);
        assertNull(p3);
    }

}
