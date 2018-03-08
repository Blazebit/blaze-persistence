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
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test that asserts Hibernate versions which can handle entity comparisons don't do id rewrites.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
public class EntityEqualityNoRewriteToIdTest extends AbstractCoreTest {

    @Test
    public void neverRewriteEntityAssociationEqualsEntityInOnIfSupported() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument").eqExpression("d")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument = d"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support transient objects as parameters
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class })
    public void neverRewriteEntityAssociationEqualsTransientEntityParameterInOnIfSupported() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument").eq(new Document(1L))
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Eclipselink loads entities when doing getReference()
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class })
    public void neverRewriteEntityAssociationEqualsManagedEntityParameterInOnIfSupported() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument").eq(em.getReference(Document.class, 1L))
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void neverRewriteEntityEqualsEntityAssociationInOnIfSupported() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p").eqExpression("d.owner")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = d.owner"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support transient objects as parameters
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class })
    public void neverRewriteEntityEqualsTransientEntityParameterInOn() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p").eq(new Person(1L))
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Eclipselink loads entities when doing getReference()
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class })
    public void neverRewriteEntityEqualsManagedEntityParameterInOn() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p").eq(em.getReference(Person.class, 1L))
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

}
