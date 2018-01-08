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
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionJoinTestHibernate extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Root2.class,
            IndexedNode2.class,
            KeyedNode2.class,
            KeyedEmbeddable.class,
            IndexedEmbeddable.class
        };
    }

    @Test
    public void testOneToManyMappedBy() {
        CriteriaBuilder<Root2> criteria = cbf.create(em, Root2.class, "r");
        criteria.select("r.indexedNodesMappedBy[0]");
        criteria.select("r.keyedNodesMappedBy['default']");

        assertEquals("SELECT indexedNodesMappedBy_0_1, " + joinAliasValue("keyedNodesMappedBy_default_1") + " FROM Root2 r"
                + " LEFT JOIN r.indexedNodesMappedBy indexedNodesMappedBy_0_1"
                + onClause("INDEX(indexedNodesMappedBy_0_1) = 0")
                + " LEFT JOIN r.keyedNodesMappedBy keyedNodesMappedBy_default_1"
                + onClause("KEY(keyedNodesMappedBy_default_1) = 'default'"), criteria.getQueryString());
        criteria.getResultList();
    }

}
