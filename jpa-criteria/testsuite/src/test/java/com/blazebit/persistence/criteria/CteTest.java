/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity_;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1_;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.entity.TestCTEEmbeddable_;
import com.blazebit.persistence.testsuite.entity.TestCTE_;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class CteTest extends AbstractCoreTest {

    private CriteriaBuilderFactory cbfUnoptimized;

    @Before
    public void initNonOptimized() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config.getProperties().setProperty(ConfigurationProperties.EXPRESSION_OPTIMIZATION, "false");
        cbfUnoptimized = config.createCriteriaBuilderFactory(emf);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                RecursiveEntity.class,
                TestCTE.class,
                TestAdvancedCTE1.class,
                TestAdvancedCTE2.class
        };
    }


    @Test
    @Category({  NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testBindEmbeddable() {
        BlazeCriteriaQuery<TestAdvancedCTE1> cq = BlazeCriteria.get(cbf, TestAdvancedCTE1.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();

        BlazeFullSelectCTECriteria<TestAdvancedCTE1> documentCte = cq.with(TestAdvancedCTE1.class);
        BlazeRoot<RecursiveEntity> recursiveEntity = documentCte.from(RecursiveEntity.class);
        documentCte.bind(TestAdvancedCTE1_.id, recursiveEntity.get(RecursiveEntity_.id));
        documentCte.bind(documentCte.get(TestAdvancedCTE1_.embeddable).get(TestCTEEmbeddable_.name), recursiveEntity.get(RecursiveEntity_.name));
        documentCte.bind(documentCte.get(TestAdvancedCTE1_.embeddable).get(TestCTEEmbeddable_.description), "desc");
        documentCte.bind(documentCte.get(TestAdvancedCTE1_.embeddable).get(TestCTEEmbeddable_.recursiveEntity), recursiveEntity);
        documentCte.bind(TestAdvancedCTE1_.level, cb.literal(0));
        documentCte.bind(TestAdvancedCTE1_.parent, recursiveEntity.get(RecursiveEntity_.parent));

        cq.from(TestAdvancedCTE1.class);

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String queryString = criteriaBuilder.getQueryString();
        Assert.assertNotNull(queryString);
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoOracle.class })
    public void testRecursiveCTE() {
        BlazeCriteriaQuery<TestCTE> cq = BlazeCriteria.get(cbf, TestCTE.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();

        BlazeSelectRecursiveCTECriteria<TestCTE> testCTE = cq.withRecursive(TestCTE.class);
        {
            BlazeRoot<RecursiveEntity> recursiveEntity = testCTE.from(RecursiveEntity.class, "e");
            testCTE.bind(TestCTE_.id, recursiveEntity.get(RecursiveEntity_.id));
            testCTE.bind(TestCTE_.name, recursiveEntity.get(RecursiveEntity_.name));
            testCTE.bind(TestCTE_.level, 0);
            testCTE.where(recursiveEntity.get(RecursiveEntity_.parent).isNull());
        }

        {
            BlazeSelectCTECriteria<TestCTE> recursivePart = testCTE.unionAll();
            BlazeRoot<TestCTE> testCteRoot = recursivePart.from(TestCTE.class, "t");
            BlazeJoin<TestCTE, RecursiveEntity> recursiveEntity = testCteRoot.join(RecursiveEntity.class, "e");
            recursiveEntity.on(cb.equal(testCteRoot.get(TestCTE_.id), recursiveEntity.get(RecursiveEntity_.parent).get(RecursiveEntity_.id)));
            recursivePart.bind(TestCTE_.id, recursiveEntity.get(RecursiveEntity_.id));
            recursivePart.bind(TestCTE_.name, recursiveEntity.get(RecursiveEntity_.name));
            recursivePart.bind(TestCTE_.level, cb.sum(testCteRoot.get(TestCTE_.level), 1));
        }

        BlazeRoot<TestCTE> t = cq.from(TestCTE.class, "t");
        cq.where(cb.lessThan(t.get(TestCTE_.level), 2));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String expected = ""
                + "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";

        assertEquals(expected, criteriaBuilder.getQueryString());
    }

    private String innerJoinRecursive(String entityFragment, String onPredicate) {
        if (jpaProvider.supportsEntityJoin() && dbmsDialect.supportsJoinsInRecursiveCte()) {
            return " JOIN " + entityFragment + onClause(onPredicate);
        } else {
            return ", " + entityFragment + " WHERE " + onPredicate;
        }
    }

}
