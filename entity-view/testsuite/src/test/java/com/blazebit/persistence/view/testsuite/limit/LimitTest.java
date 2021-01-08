/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.limit;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.limit.model.DocumentLimitView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitJoinExpressionView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitJoinView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitMultisetView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitSelectView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitSubselectView;
import com.blazebit.persistence.view.testsuite.limit.model.PersonLimitView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

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
    @Category({ NoH2.class, NoMySQLOld.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // H2 and the MySQL before 8 didn't support lateral joins which are required here
    // EclipseLink, Datanucleus and OpenJPA don't support extended SQL
    public void testLimitMultiset() {
        test(PersonLimitMultisetView.class, null);
    }

    @Test
    @Category({ NoMySQLOld.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // We need a left entity join for this so Hibernate < 5.1 can't be used
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitJoin() {
        test(PersonLimitJoinView.class, null);
    }

    @Test
    @Category({ NoMySQLOld.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // We need a left entity join for this so Hibernate < 5.1 can't be used
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitJoinExpression() {
        test(PersonLimitJoinExpressionView.class, null);
    }

    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitSelectBatch1() {
        test(PersonLimitSelectView.class, 1);
    }

    @Test
    @Category({ NoMySQLOld.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // MySQL before 8 didn't support lateral and also don't support correlated LIMIT subqueries in quantified predicates
    // EclipseLink doesn't support subqueries in functions which is required for LIMIT
    // Datanucleus fails because of a NPE?
    // OpenJPA has no function support
    public void testLimitSelectBatch2() {
        test(PersonLimitSelectView.class, 2);
    }

    @Test
    @Category({ NoMySQLOld.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
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
