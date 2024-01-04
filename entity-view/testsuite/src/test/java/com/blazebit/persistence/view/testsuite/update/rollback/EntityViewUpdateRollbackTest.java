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

package com.blazebit.persistence.view.testsuite.update.rollback;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.rollback.model.PersonNameView;
import com.blazebit.persistence.view.testsuite.update.rollback.model.UpdatableDocumentRollbackView;
import com.blazebit.persistence.view.testsuite.update.rollback.model.CreatePersonRollbackView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateRollbackTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentRollbackView> {

    public EntityViewUpdateRollbackTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentRollbackView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonNameView.class);
        cfg.addEntityView(CreatePersonRollbackView.class);
        cfg.addEntityView(DocumentRollbackInfoView.class);
    }

    @EntityView(Document.class)
    public static interface DocumentRollbackInfoView {

        @IdMapping
        public Long getId();

        public Long getAge();
    }

    @Test
    public void testUpdateRollbacked() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();
        
        // When 1
        docView.setName("newDoc");
        AtomicInteger updateCommitCount = new AtomicInteger();
        saveWithRollbackWith(docView, flushOperationBuilder -> {
            flushOperationBuilder.onPostRollbackUpdate(DocumentRollbackInfoView.class, (entityViewManager, entityManager, view, transition) -> {
                assertEquals(10L, view.getAge().longValue());
                updateCommitCount.incrementAndGet();
            });
        });

        // Then 1
        clearPersistenceContextAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", doc1.getName());
        assertEquals(1, updateCommitCount.get());

        // When 2
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testModifyAndUpdateRollbacked() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();
        
        // When
        docView.setName("newDoc");
        updateWithRollback(docView);

        // Then 1
        clearPersistenceContextAndReload();
        assertEquals("newDoc", docView.getName());
        assertEquals("doc1", doc1.getName());

        // When 2
        docView.setName("newDoc1");
        // Remove milliseconds because MySQL doesn't use that precision by default
        Date date = new Date();
        date.setTime(1000 * (date.getTime() / 1000));
        docView.setLastModified(date);
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
        assertEquals("newDoc1", docView.getName());
        assertEquals(date.getTime(), docView.getLastModified().getTime());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals(doc1.getLastModified().getTime(), docView.getLastModified().getTime());
    }

    @Test
    public void testMultiUpdateRollback() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();

        // When
        docView.setName("newDoc");
        assertEquals("doc1", evm.getChangeModel(docView).get("name").getInitialState());
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EntityTransaction tx = em.getTransaction();
                evm.save(em, docView);
                assertEquals("newDoc", evm.getChangeModel(docView).get("name").getInitialState());
                em.flush();
                docView.setName("newNewDoc");
                assertEquals("newDoc", evm.getChangeModel(docView).get("name").getInitialState());
                evm.save(em, docView);
                assertEquals("newNewDoc", evm.getChangeModel(docView).get("name").getInitialState());
                em.flush();
                tx.setRollbackOnly();
            }
        });
        assertEquals("doc1", evm.getChangeModel(docView).get("name").getInitialState());
        assertEquals("newNewDoc", evm.getChangeModel(docView).get("name").getCurrentState());
        assertEquals("newNewDoc", docView.getName());
    }

    @Test
    public void testRollbackPersisted() {
        // Given
        final UpdatableDocumentRollbackView docView = getDoc1View();
        CreatePersonRollbackView personView = evm.create(CreatePersonRollbackView.class);
        personView.setName("test");

        // When 1
        docView.setOwner(personView);
        assertTrue(((DirtyTracker) personView).$$_hasParent());
        updateWithRollback(docView);

        // Then 1
        clearPersistenceContextAndReload();
        assertTrue(docView.getOwner() == personView);
        assertTrue(((DirtyTracker) personView).$$_hasParent());

        // When 2
        update(docView);

        // Then 2
        assertNoUpdateAndReload(docView);
        assertFalse(docView.getOwner() == personView);
        assertFalse(((DirtyTracker) personView).$$_hasParent());
        assertEquals(doc1.getOwner().getId(), personView.getId());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
