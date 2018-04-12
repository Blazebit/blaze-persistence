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

package com.blazebit.persistence.view.testsuite.update.remove.orphan.nested;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.AbstractEntityViewOrphanRemoveDocumentTest;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonCreateView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableResponsiblePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewOrphanRemoveNestedSubviewCollectionsTest extends AbstractEntityViewOrphanRemoveDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewOrphanRemoveNestedSubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
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
        cfg.addEntityView(FriendPersonCreateView.class);
    }

    @Test
    public void testSetNull() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getPeople().get(1).setFriend(null);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        restartTransactionAndReload();
        assertNull(p4.getFriend());
        assertNull(p8);
    }

    @Test
    public void testSetOther() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        FriendPersonView p5View = getPersonView(p5.getId(), FriendPersonView.class);
        clearQueries();

        // When
        docView.getPeople().get(1).setFriend(p5View);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        restartTransactionAndReload();
        assertEquals(p5.getId(), p4.getFriend().getId());
        assertNull(p8);
    }

    @Test
    public void testSetNew() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        FriendPersonCreateView newPersonView = evm.create(FriendPersonCreateView.class);
        newPersonView.setName("new");
        clearQueries();

        // When
        docView.getPeople().get(1).setFriend(newPersonView);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();
        builder.insert(Person.class);
        builder.validate();

        restartTransactionAndReload();
        assertEquals("new", p4.getFriend().getName());
        assertNull(p8);
    }

    @Test
    public void testRemoveCascade() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();

        // When
        docView.getPeople().remove(1);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence().unordered();

        if (!isQueryStrategy()) {
            // Hibernate loads the entities before deleting?
            builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Document.class, "people")
                        .fetching(Person.class)
                    .and()
                    .select(Person.class);
        } else if (isQueryStrategy()) {
            // This is just temporary until #507 is fixed
            builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Document.class, "people")
                        .fetching(Person.class)
                    .and();

            if (isFullMode()) {
                builder.update(Person.class);
            }
        }

        if (version) {
            builder.update(Document.class);
        }
        deletePersonOwned(builder, true);
        deletePersonOwned(builder, true);
        builder.delete(Person.class);
        builder.delete(Person.class);
        builder.assertDelete()
                .forRelation(Document.class, "people")
            .and();
        builder.validate();

        restartTransactionAndReload();
        assertNull(p4);
        assertNull(p8);
        assertEquals(1, doc1.getPeople().size());
    }

    public AssertStatementBuilder assertUpdateAndRemove() {
        AssertStatementBuilder builder = assertQuerySequence().unordered();

        if (isFullMode() || !isQueryStrategy()) {
            // Hibernate loads the entities before deleting?
            builder.assertSelect()
                        .fetching(Document.class)
                        .fetching(Document.class, "people")
                        .fetching(Person.class)
                    .and();
            if (!isQueryStrategy()) {
                builder.select(Person.class);
            }
        }

        if (isFullMode() && isQueryStrategy()) {
            builder.update(Person.class);
        }

        // Since we switch to entity flushing because of the collection, we avoid the document flush even in full mode
        // This will be fixed with #507
        if (version) { // || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }

        deletePersonOwned(builder, true);

        // document.responsiblePerson.friend
        builder.delete(Person.class);
        builder.update(Person.class);
        return builder;
    }

}
