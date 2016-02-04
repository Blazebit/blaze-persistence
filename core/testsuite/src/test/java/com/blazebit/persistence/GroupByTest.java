/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blazebit.persistence.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class GroupByTest extends AbstractCoreTest {

    @Test
    public void testGroupByEntitySelect() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d");
        criteria.groupBy("d.owner");
        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 GROUP BY owner_1, d.age, d.archived, d.creationDate, d.creationDate2, d.documentType, d.id, d.idx, d.intIdEntity, d.lastModified, d.lastModified2, d.name, d.nonJoinable, d.owner", criteria.getQueryString());
        criteria.getResultList();
    }
}
