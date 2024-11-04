/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate52;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate53;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test that asserts Hibernate versions which can't handle entity comparisons do id rewrites.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class EntityEqualityRewriteToIdTest extends AbstractCoreTest {

    @Test
    // This actually only makes sense for Hibernate 5.1
    public void rewriteEntityAssociationEqualsEntityInOnToIdEquals() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 1);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument").eqExpression("d")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument.id = d.id"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Pre Hibernate 5.1 we could do this broken thing to make some stuff work
    public void rewriteEntityAssociationEqualsEntityInOnToIdEqualsBroken() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() < 5 || getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 0);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p.partnerDocument").eqExpression("d")
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument.id = d"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Pre Hibernate 5.1 we could do this broken thing to make some stuff work
    public void rewriteEntityAssociationEqualsParameterInOnToIdEquals() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() < 5 || getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() <= 1);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p.partnerDocument").eq(new Document(1L))
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.partnerDocument.id = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // This actually only makes sense for Hibernate 5.1
    public void rewriteEntityEqualsEntityAssociationInOnToIdEquals() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 1);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
            .on("p").eqExpression("d.owner")
        .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.id = d.owner.id"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Pre Hibernate 5.1 we could do this broken thing to make some stuff work
    public void rewriteEntityEqualsEntityAssociationInOnToIdEqualsBroken() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() < 5 || getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 0);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p").eqExpression("d.owner")
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = d.owner.id"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // This actually only makes sense for Hibernate 5.1
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate52.class, NoHibernate53.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void rewriteEntityEqualsTransientEntityParameterInOn() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 1);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p").eq(new Person(1L))
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p.id = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Pre Hibernate 5.1 we could do this broken thing to make some stuff work
    public void rewriteEntityEqualsTransientEntityParameterInOnBroken() {
        Assume.assumeTrue(getJpaProviderFamily() == JpaProviderFamily.HIBERNATE && getJpaProviderMajorVersion() < 5 || getJpaProviderMajorVersion() == 5 && getJpaProviderMinorVersion() == 0);
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.innerJoinOn("partners","p")
                .on("p").eq(new Person(1L))
                .end();

        assertEquals("SELECT d FROM Document d JOIN d.partners p"
                + onClause("p = :param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

}
