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
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test that asserts JPA impls which can handle entity comparisons rewrite id access to entity access.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Category({ NoHibernate.class })
public class EntityEqualityRewriteToEntityTest extends AbstractCoreTest {

    @Test
    public void rewriteEntityAssociationEqualsEntityInOnToIdEquals() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument.id").eqExpression("d.id")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument = d"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void rewriteEntityAssociationEqualsParameterInOnToIdEquals() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument.id").eq(1L)
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void rewriteEntityEqualsEntityAssociationInOnToIdEquals() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.id").eqExpression("d.owner.id")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = d.owner"), criteria.getQueryString());
        criteria.getResultList();
    }

}
