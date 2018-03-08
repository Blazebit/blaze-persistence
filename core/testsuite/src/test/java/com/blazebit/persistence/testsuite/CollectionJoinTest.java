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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionJoinTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Root.class,
            IndexedNode.class,
            KeyedNode.class,
            KeyedEmbeddable.class,
            IndexedEmbeddable.class
        };
    }

    @Test
    public void testOneToManyJoinTable() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodes[0]");
        criteria.select("r.keyedNodes['default']");

        assertEquals("SELECT indexedNodes_0_1, " + joinAliasValue("keyedNodes_default_1") + " FROM Root r"
                + " LEFT JOIN r.indexedNodes indexedNodes_0_1"
                + onClause("INDEX(indexedNodes_0_1) = 0")
                + " LEFT JOIN r.keyedNodes keyedNodes_default_1"
                + onClause("KEY(keyedNodes_default_1) = 'default'"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testManyToManyJoinTable() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesMany[0]");
        criteria.select("r.keyedNodesMany['default']");

        assertEquals("SELECT indexedNodesMany_0_1, " + joinAliasValue("keyedNodesMany_default_1") + " FROM Root r"
                + " LEFT JOIN r.indexedNodesMany indexedNodesMany_0_1"
                + onClause("INDEX(indexedNodesMany_0_1) = 0")
                + " LEFT JOIN r.keyedNodesMany keyedNodesMany_default_1"
                + onClause("KEY(keyedNodesMany_default_1) = 'default'"), criteria.getQueryString());
        criteria.getResultList();
    }

    // Normally we would have duplicate names, but since we are re-aliasing the parent id and collection key names, this must pass
    @Test
    public void testManyToManyJoinTableDuplicateName() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesManyDuplicate[0]");
        criteria.select("r.keyedNodesManyDuplicate['default']");

        assertEquals("SELECT indexedNodesManyDuplicate_0_1, " + joinAliasValue("keyedNodesManyDuplicate_default_1") + " FROM Root r"
                + " LEFT JOIN r.indexedNodesManyDuplicate indexedNodesManyDuplicate_0_1"
                + onClause("INDEX(indexedNodesManyDuplicate_0_1) = 0")
                + " LEFT JOIN r.keyedNodesManyDuplicate keyedNodesManyDuplicate_default_1"
                + onClause("KEY(keyedNodesManyDuplicate_default_1) = 'default'"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: http://hibernate.atlassian.net/browse/HHH-10229
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void testElementCollection() {
        CriteriaBuilder<Object[]> criteria = cbf.create(em, Object[].class);
        criteria.from(Root.class, "r");
        criteria.select("r.indexedNodesElementCollection[0]");
        criteria.select("r.keyedNodesElementCollection['default']");

        assertEquals("SELECT indexedNodesElementCollection_0_1, " + joinAliasValue("keyedNodesElementCollection_default_1") + " FROM Root r"
                + " LEFT JOIN r.indexedNodesElementCollection indexedNodesElementCollection_0_1"
                + onClause("INDEX(indexedNodesElementCollection_0_1) = 0")
                + " LEFT JOIN r.keyedNodesElementCollection keyedNodesElementCollection_default_1"
                + onClause("KEY(keyedNodesElementCollection_default_1) = 'default'"), criteria.getQueryString());
        criteria.getResultList();
    }
}
