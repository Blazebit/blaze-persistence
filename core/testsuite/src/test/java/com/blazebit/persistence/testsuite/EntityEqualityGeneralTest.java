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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityEqualityGeneralTest extends AbstractCoreTest {

    @Test
    public void neverRewriteEntityAssociationEqualsEntityInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").eqExpression("d.parent.owner");

        assertEquals("SELECT d FROM Document d LEFT JOIN d.parent parent_1 WHERE d.owner = parent_1.owner", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support transient objects as parameters
    @Category({ NoDatanucleus.class })
    public void neverRewriteEntityAssociationEqualsTransientEntityParameterInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").eq(new Person(1L));

        assertEquals("SELECT d FROM Document d WHERE d.owner = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Eclipselink loads entities when doing getReference()
    @Category({ NoEclipselink.class })
    public void neverRewriteEntityAssociationEqualsManagedEntityParameterInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").eq(em.getReference(Person.class, 1L));

        assertEquals("SELECT d FROM Document d WHERE d.owner = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

}
