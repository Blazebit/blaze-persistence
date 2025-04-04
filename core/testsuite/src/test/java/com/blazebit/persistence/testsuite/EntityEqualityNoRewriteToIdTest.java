/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
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
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus4.class })
    public void neverRewriteEntityAssociationEqualsEntityInOnIfSupported1() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p");
        criteria.innerJoinOn(Document.class,"correlated_ownedDocuments")
                .on("correlated_ownedDocuments.owner").eqExpression("p")
                .end();
        criteria.innerJoinDefault("correlated_ownedDocuments.owner", "o1");

        assertEquals("SELECT p FROM Person p JOIN Document correlated_ownedDocuments"
                + onClause("correlated_ownedDocuments.owner = p") + " JOIN correlated_ownedDocuments.owner o1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus4.class })
    public void neverRewriteEntityAssociationEqualsEntityInOnIfSupported2() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p");
        criteria.select("correlated_ownedDocuments.owner.name");
        criteria.innerJoinOn(Document.class,"correlated_ownedDocuments")
                .on("correlated_ownedDocuments.owner").eqExpression("p")
                .end();
        criteria.innerJoinDefault("correlated_ownedDocuments.owner", "o1");

        assertEquals("SELECT o1.name FROM Person p JOIN Document correlated_ownedDocuments"
                + onClause("correlated_ownedDocuments.owner = p") + " JOIN correlated_ownedDocuments.owner o1", criteria.getQueryString());
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
