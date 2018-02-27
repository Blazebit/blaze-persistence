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
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityNullnessGeneralTest extends AbstractCoreTest {

    @Test
    public void neverRewriteEntityAssociationNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").isNull();

        assertEquals("SELECT d FROM Document d WHERE d.owner IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void alwaysRewriteEntityAssociationIdNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner.id").isNull();

        assertEquals("SELECT d FROM Document d WHERE " + singleValuedAssociationIdNullnessPath("d.owner", "id") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void alwaysRewriteEntityIdNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.id").isNull();

        assertEquals("SELECT d FROM Document d WHERE d IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

}
