/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.cascade.simple;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitiveFamily;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.entity.PrimitiveVersion;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.PostRemoveListener;
import com.blazebit.persistence.view.PreRemoveListener;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.simple.model.FamilyIdView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.simple.model.PersonIdView;
import com.blazebit.persistence.view.testsuite.update.remove.cascade.simple.model.PersonNameView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewRemoveCascadeOneToManyTest extends AbstractEntityViewUpdateTest<PersonIdView> {

    private PrimitiveFamily family;
    private PrimitivePerson person;

    public EntityViewRemoveCascadeOneToManyTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, PersonIdView.class);
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(FamilyIdView.class);
        cfg.addEntityView(PersonNameView.class);
        cfg.addEntityViewListener(PersonNameView.class, PrimitivePerson.class, PrimitivePersonPostRemoveListener.class);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                PrimitivePerson.class,
                PrimitiveDocument.class,
                PrimitiveVersion.class,
                PrimitiveFamily.class
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

    // Test for issue #1456
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
            // Select for the entity view used in the PrimitivePersonPostRemoveListener
            builder.select(PrimitivePerson.class);
            builder.select(PrimitiveDocument.class)
                .select(PrimitiveVersion.class);
        } else {
            // todo: the following select only happens because we don't convert from the entity to the entity view
            builder.select(PrimitivePerson.class);
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

    // Test for issue #1520
    @Test
    public void testRemoveFamilyById() {
        // Given
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                family = new PrimitiveFamily("fam");
                family.setPerson(em.getReference(PrimitivePerson.class, person.getId()));
                em.persist(family);
            }
        });
        clearQueries();

        // When
        remove(FamilyIdView.class, family.getId());

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // In the query strategy, we query by owner id
        if (isQueryStrategy()) {
            if (!dbmsDialect.supportsReturningColumns()) {
                builder.select(PrimitiveFamily.class);
            }
            // Select for the entity view used in the PrimitivePersonPostRemoveListener
            builder.select(PrimitivePerson.class);
            builder.select(PrimitiveDocument.class)
                .select(PrimitiveVersion.class);
        } else {
            builder.select(PrimitiveFamily.class)
                .select(PrimitivePerson.class)
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
            .delete(PrimitiveFamily.class)
            .validate();

        clearPersistenceContextAndReload();
        Assert.assertNull(person);
        Assert.assertNull(family);
    }

    // Test for issue #1520
    @Test
    public void testRemoveFamilyByIdCascadeNull() {
        // Given
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                family = new PrimitiveFamily("fam");
                em.persist(family);
            }
        });
        clearQueries();

        // When
        remove(FamilyIdView.class, family.getId());

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        // In the query strategy, we query by owner id
        if (isQueryStrategy()) {
            if (!dbmsDialect.supportsReturningColumns()) {
                builder.select(PrimitiveFamily.class);
            }
        } else {
            builder.select(PrimitiveFamily.class);
        }

        builder.delete(PrimitiveFamily.class)
            .validate();

        clearPersistenceContextAndReload();
        Assert.assertNull(family);
    }

    // Test for issue #1520
    @Test
    public void testRemoveFamilyByIdPreRemove() {
        // Given
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                family = new PrimitiveFamily("fam");
                family.setPerson(em.getReference(PrimitivePerson.class, person.getId()));
                em.persist(family);
            }
        });
        clearQueries();

        // When
        try {
            transactional(new TxVoidWork() {
                @Override
                public void work(EntityManager em) {
                    evm.removeWith(em, FamilyIdView.class, family.getId())
                        .onPreRemove(FamilyIdView.class, new PreRemoveListener<FamilyIdView>() {
                            @Override
                            public boolean preRemove(EntityViewManager entityViewManager, EntityManager entityManager, FamilyIdView view) {
                                entityViewManager.remove(entityManager, PersonIdView.class, person.getId());
                                return true;
                            }
                        })
                        .flush();
                    em.flush();
                }
            });
        } catch (OptimisticLockException e) {
            if (isQueryStrategy()) {
                // We expect this to happen with the query flush strategy, because the preRemove listener doesn't propagate the context
                return;
            }
            throw e;
        }

        // Then
        clearPersistenceContextAndReload();
        Assert.assertNull(family);
    }

    @Override
    protected void reload() {
        person = em.find(PrimitivePerson.class, person.getId());
        family = family == null ? null : em.find(PrimitiveFamily.class, family.getId());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder;
    }

    public static class PrimitivePersonPostRemoveListener implements PostRemoveListener<PersonNameView> {
        @Override
        public void postRemove(EntityViewManager entityViewManager, EntityManager entityManager, PersonNameView view) {

        }
    }
}
