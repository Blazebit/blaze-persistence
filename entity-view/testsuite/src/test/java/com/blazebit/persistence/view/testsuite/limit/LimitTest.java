/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.limit;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate62;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.limit.model.DocumentLimitView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitJoinExpressionView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitJoinView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitMultisetView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitSelectView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitSubselectView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitView;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class LimitTest extends AbstractEntityViewTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("pers1");
                Document doc1 = new Document("doc1", o1);
                doc1.setAge(10);
                Document doc2 = new Document("doc2", o1);
                doc2.setAge(5);
                Document doc3 = new Document("doc3", o1);
                doc3.setAge(10);

                em.persist(o1);
                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Test
    // EclipseLink, Datanucleus and OpenJPA don't support extended SQL
    // NOTE: Hibernate 6.3 bug: https://hibernate.atlassian.net/browse/HHH-17386
    @Category({ NoEclipselink.class, NoHibernate62.class })
    public void testLimitMultiset() {
        test(PersonLimitMultisetView.class, null);
    }

    @Test
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    @Category({ NoEclipselink.class })
    public void testLimitJoin() {
        test(PersonLimitJoinView.class, null);
    }

    @Test
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // NOTE: Hibernate 6.3 bug: https://hibernate.atlassian.net/browse/HHH-17386
    @Category({ NoEclipselink.class, NoHibernate62.class })
    public void testLimitJoinExpression() {
        test(PersonLimitJoinExpressionView.class, null);
    }

    @Test
    @Category({ NoEclipselink.class })
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitSelectBatch1() {
        test(PersonLimitSelectView.class, 1);
    }

    @Test
    @Category({ NoEclipselink.class })
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitSelectBatch2() {
        test(PersonLimitSelectView.class, 2);
    }

    @Test
    @Category({ NoEclipselink.class })
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitSubselect() {
        test(PersonLimitSubselectView.class, null);
    }

    private void test(Class<? extends PersonLimitView> clazz, Integer batchSize) {
        EntityViewManager evm = build(DocumentLimitView.class, clazz);
        if (batchSize == null) {
            batchSize = 1;
        }
        String prop = ConfigurationProperties.DEFAULT_BATCH_SIZE + ".ownedDocuments";
        List<? extends PersonLimitView> list = evm.applySetting(EntityViewSetting.create(clazz).withProperty(prop, batchSize), cbf.create(em, Person.class, "p")).getResultList();
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getOwnedDocuments().size());
        assertEquals("doc2", list.get(0).getOwnedDocuments().get(0).getName());
    }
}
