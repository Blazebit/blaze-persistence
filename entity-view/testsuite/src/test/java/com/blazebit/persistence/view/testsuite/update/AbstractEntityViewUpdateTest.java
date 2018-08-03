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

package com.blazebit.persistence.view.testsuite.update;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractEntityViewUpdateTest<T> extends AbstractEntityViewTest {

    protected static final long EPOCH_2K = 946684800000L;
    protected static final Object[][] MODE_STRATEGY_VERSION_COMBINATIONS = {
            { FlushMode.LAZY, FlushStrategy.ENTITY, true },
            { FlushMode.LAZY, FlushStrategy.QUERY, true },
            { FlushMode.LAZY, FlushStrategy.ENTITY, false },
            { FlushMode.LAZY, FlushStrategy.QUERY, false },

            { FlushMode.PARTIAL, FlushStrategy.ENTITY, true },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, true },
            { FlushMode.PARTIAL, FlushStrategy.ENTITY, false },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, false },

            { FlushMode.FULL, FlushStrategy.ENTITY, true },
            { FlushMode.FULL, FlushStrategy.QUERY, true },
            { FlushMode.FULL, FlushStrategy.ENTITY, false },
            { FlushMode.FULL, FlushStrategy.QUERY, false },
    };
    protected static final Object[][] MODE_STRATEGY_VERSION_TYPE_COMBINATIONS = {
            { FlushMode.LAZY, FlushStrategy.ENTITY, true, true },
            { FlushMode.LAZY, FlushStrategy.QUERY, true, true },
            { FlushMode.LAZY, FlushStrategy.ENTITY, true, false },
            { FlushMode.LAZY, FlushStrategy.QUERY, true, false },
            { FlushMode.LAZY, FlushStrategy.ENTITY, false, true },
            { FlushMode.LAZY, FlushStrategy.QUERY, false, true },
            { FlushMode.LAZY, FlushStrategy.ENTITY, false, false },
            { FlushMode.LAZY, FlushStrategy.QUERY, false, false },

            { FlushMode.PARTIAL, FlushStrategy.ENTITY, true, true },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, true, true },
            { FlushMode.PARTIAL, FlushStrategy.ENTITY, true, false },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, true, false },
            { FlushMode.PARTIAL, FlushStrategy.ENTITY, false, true },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, false, true },
            { FlushMode.PARTIAL, FlushStrategy.ENTITY, false, false },
            { FlushMode.PARTIAL, FlushStrategy.QUERY, false, false },

            { FlushMode.FULL, FlushStrategy.ENTITY, true, true },
            { FlushMode.FULL, FlushStrategy.QUERY, true, true },
            { FlushMode.FULL, FlushStrategy.ENTITY, true, false },
            { FlushMode.FULL, FlushStrategy.QUERY, true, false },
            { FlushMode.FULL, FlushStrategy.ENTITY, false, true },
            { FlushMode.FULL, FlushStrategy.QUERY, false, true },
            { FlushMode.FULL, FlushStrategy.ENTITY, false, false },
            { FlushMode.FULL, FlushStrategy.QUERY, false, false },
    };

    protected FlushMode mode;
    protected FlushStrategy strategy;
    protected boolean version;
    protected Class<T> viewType;
    protected Class<?>[] views;

    public AbstractEntityViewUpdateTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        this(mode, strategy, version, viewType, new Class[0]);
    }

    public AbstractEntityViewUpdateTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType, Class<?>... views) {
        this.mode = mode;
        this.strategy = strategy;
        this.version = version;
        this.viewType = viewType;
        this.views = views;
    }

    protected void prepareData(EntityManager em) {
    }

    @Before
    public final void setUp() {
        cleanDatabase();

        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                prepareData(em);
                em.flush();
            }
        });

        restartTransactionAndReload();

        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(viewType);
        for (Class<?> view : views) {
            cfg.addEntityView(view);
        }

        registerViewTypes(cfg);

        for (EntityViewMapping mapping : cfg.getEntityViewMappings()) {
            if (version) {
                if (mapping.getVersionAttribute() == null) {
                    mapping.setVersionAttribute(mapping.getAttributes().get("version"));
                }
            } else {
                mapping.setVersionAttribute(null);
            }
            mapping.setFlushMode(mode);
            mapping.setFlushStrategy(strategy);
        }

        evm = cfg.createEntityViewManager(cbf);
        enableQueryCollecting();
    }

    @After
    public final void tearDown() {
        disableQueryCollecting();
    }

    protected void assertNullCollection(Collection<?> collection) {
        // Currently, setting a collection to null will result in an empty recording collection after update
//        assertNull(collection);
        assertTrue(collection instanceof RecordingCollection);
        assertEquals(0, collection.size());
    }

    protected void assertNullMap(Map<?, ?> map) {
        // Currently, setting a collection to null will result in an empty recording collection after update
//        assertNull(map);
        assertTrue(map instanceof RecordingMap);
        assertEquals(0, map.size());
    }

    protected void registerViewTypes(EntityViewConfiguration cfg) {
    }

    protected boolean supportsNullCollectionElements() {
        // Hibernate apparently does not insert NULL values
        return false;
    }

    protected boolean preferLoadingAndDiffingOverRecreate() {
        // CollectionAttributeFlusher#getDirtyFlusher currently favors load and diff over recreate
        return true;
    }

    protected void restartTransaction() {
        em.clear();
        em.getTransaction().rollback();
        em.getTransaction().begin();
    }

    protected abstract void restartTransactionAndReload();
    
    protected void assertNoUpdateAndReload(T docView) {
        restartTransactionAndReload();
        clearQueries();
        update(docView);
        AssertStatementBuilder afterBuilder = assertUnorderedQuerySequence();
        if (isFullMode()) {
            if (isQueryStrategy()) {
                fullUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
                if (version) {
                    versionUpdate(afterBuilder);
                }
            }
        }
        afterBuilder.validate();
        restartTransactionAndReload();
    }

    protected void assertNoUpdateFullFetchAndReload(T docView) {
        restartTransactionAndReload();
        clearQueries();
        update(docView);
        fullFetch(assertQuerySequence()).validate();
    }

    protected AssertStatementBuilder assertQueriesAfterUpdate(T docView) {
        restartTransactionAndReload();
        clearQueries();
        update(docView);
        return assertUnorderedQuerySequence();
    }

    protected void assertVersionDiff(long oldVersion, long currentVersion, long diff, long fullDiff) {
        if (version) {
            if (isFullMode()) {
                assertEquals(oldVersion + fullDiff, currentVersion);
            } else {
                assertEquals(oldVersion + diff, currentVersion);
            }
        } else {
            assertEquals(oldVersion, currentVersion);
        }
    }

    protected void validateNoChange(T docView) {
        AssertStatementBuilder builder = assertQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullUpdate(builder);
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

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullUpdate(afterBuilder);
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

    protected abstract AssertStatementBuilder fullFetch(AssertStatementBuilder builder);

    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder;
    }

    protected abstract AssertStatementBuilder versionUpdate(AssertStatementBuilder builder);

    protected void update(final Object docView) {
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                evm.update(em, docView);
                em.flush();
            }
        });
    }

    protected void remove(final Class<?> docView, final Object id) {
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                evm.remove(em, docView, id);
                em.flush();
            }
        });
    }

    protected void remove(final T docView) {
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                evm.remove(em, docView);
                em.flush();
            }
        });
    }

    protected void updateWithRollback(final T docView) {
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                EntityTransaction tx = em.getTransaction();
                evm.update(em, docView);
                em.flush();
                tx.setRollbackOnly();
            }
        });
    }

    protected void updateAndAssertChangesFlushed(T docView) {
        update(docView);
        assertEmptyChangeModel(docView);
    }

    protected void assertEmptyChangeModel(T docView) {
        if (!isFullMode()) {
            SingularChangeModel<T> changeModel = evm.getChangeModel(docView);
            Assert.assertFalse(changeModel.isDirty());
            Assert.assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());
            Assert.assertTrue(changeModel.getDirtyChanges().isEmpty());
        }
    }

    protected boolean isFullMode() {
        return mode == FlushMode.FULL;
    }

    protected boolean isQueryStrategy() {
        return strategy != FlushStrategy.ENTITY;
    }

}
