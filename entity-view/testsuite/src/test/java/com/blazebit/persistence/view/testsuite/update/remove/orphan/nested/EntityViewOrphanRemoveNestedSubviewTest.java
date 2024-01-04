/*
 * Copyright 2014 - 2024 Blazebit.
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
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.AbstractEntityViewOrphanRemoveDocumentTest;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonCreateView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.FriendPersonView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.remove.orphan.nested.model.UpdatableResponsiblePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.atomic.AtomicInteger;

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
public class EntityViewOrphanRemoveNestedSubviewTest extends AbstractEntityViewOrphanRemoveDocumentTest<UpdatableDocumentView> {

    public EntityViewOrphanRemoveNestedSubviewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
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
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.getResponsiblePerson().setFriend(null);
        AtomicInteger removeCount = new AtomicInteger();
        AtomicInteger removeCommitCount = new AtomicInteger();
        saveWith(docView, flushOperationBuilder -> {
            flushOperationBuilder.onPreRemove(FriendPersonView.class, (entityViewManager, entityManager, view) -> {
                assertEquals(p7.getId(), view.getId());
                removeCount.incrementAndGet();
                return true;
            }).onPostCommitRemove(FriendPersonView.class, (entityViewManager, entityManager, view, transition) -> {
                assertEquals(p7.getId(), view.getId());
                removeCommitCount.incrementAndGet();
            });
        });

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        clearPersistenceContextAndReload();
        assertEquals(1, removeCount.get());
        assertEquals(1, removeCommitCount.get());
        assertNull(p2.getFriend());
        assertNull(p7);
    }

    @Test
    public void testSetOther() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        FriendPersonView p5View = getPersonView(p3.getId(), FriendPersonView.class);
        clearQueries();

        // When
        docView.getResponsiblePerson().setFriend(p5View);
        AtomicInteger removeCount = new AtomicInteger();
        AtomicInteger removeCommitCount = new AtomicInteger();
        saveWith(docView, flushOperationBuilder -> {
            flushOperationBuilder.onPreRemove(FriendPersonView.class, (entityViewManager, entityManager, view) -> {
                assertEquals(p7.getId(), view.getId());
                removeCount.incrementAndGet();
                return true;
            }).onPostCommitRemove(FriendPersonView.class, (entityViewManager, entityManager, view, transition) -> {
                assertEquals(p7.getId(), view.getId());
                removeCommitCount.incrementAndGet();
            });
        });

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();

        builder.validate();

        clearPersistenceContextAndReload();
        assertEquals(1, removeCount.get());
        assertEquals(1, removeCommitCount.get());
        assertEquals(p3.getId(), p2.getFriend().getId());
        assertNull(p7);
    }

    @Test
    public void testSetNew() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        FriendPersonCreateView newPersonView = evm.create(FriendPersonCreateView.class);
        newPersonView.setName("new");
        clearQueries();

        // When
        docView.getResponsiblePerson().setFriend(newPersonView);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUpdateAndRemove();
        builder.insert(Person.class);
        builder.validate();

        clearPersistenceContextAndReload();
        assertEquals("new", p2.getFriend().getName());
        assertNull(p7);
    }

    @Test
    public void testSetNullCascade() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        docView.setResponsiblePerson(null);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                builder.assertSelect()
                    .fetching(Document.class)
                    .fetching(Person.class)
                    .and();
                if (!supportsProxyRemoveWithoutLoading()) {
                    builder.select(Person.class);
                }
            } else {
                builder.select(Document.class);
                if (!supportsProxyRemoveWithoutLoading()) {
                    builder.select(Person.class)
                        .select(Person.class);
                }
            }
        }

        builder.update(Document.class);
        deletePersonOwned(builder, true);
        deletePersonOwned(builder, true);
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.validate();

        clearPersistenceContextAndReload();
        assertNull(p2);
        assertNull(p7);
        assertNull(doc1.getResponsiblePerson());
    }

    public AssertStatementBuilder assertUpdateAndRemove() {
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .and();
            if (!supportsProxyRemoveWithoutLoading()) {
                builder.select(Person.class);
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }

        deletePersonOwned(builder, true);

        // document.responsiblePerson.friend
        builder.delete(Person.class);
        builder.update(Document.class);
        builder.update(Person.class);
        return builder;
    }
}
