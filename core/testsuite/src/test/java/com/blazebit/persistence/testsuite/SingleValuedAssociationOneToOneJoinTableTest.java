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
import com.blazebit.persistence.testsuite.entity.DocumentForOneToOneJoinTable;
import com.blazebit.persistence.testsuite.entity.DocumentForOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfo;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Datanucleus generates wrong DDL for OneToOne JoinTable mappings: https://github.com/datanucleus/datanucleus-core/issues/196
@Category({ NoDatanucleus.class })
public class SingleValuedAssociationOneToOneJoinTableTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class[]{
                DocumentForOneToOneJoinTable.class,
                DocumentForOneToOne.class,
                DocumentInfo.class
        });
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentForOneToOneJoinTable> criteria = cbf.create(em, DocumentForOneToOneJoinTable.class, "d");
        criteria.where("d.documentInfoJoinTable").isNull();

        assertEquals("SELECT d FROM DocumentForOneToOneJoinTable d WHERE d.documentInfoJoinTable IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category({NoHibernate52.class, NoHibernate53.class})
    public void oneToOneJoinTableSingleValuedAssociationRelativeIdAccess() {
        Assume.assumeFalse(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("documentInfoJoinTable.id");
        String expectedQuery = "SELECT documentInfoJoinTable_1.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable documentInfoJoinTable_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate51.class, NoHibernate50.class, NoHibernate43.class, NoHibernate42.class})
    public void oneToOneJoinTableSingleValuedAssociationRelativeIdAccessWithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("documentInfoJoinTable.id");
        String expectedQuery = "SELECT d.documentInfoJoinTable.id FROM DocumentForOneToOneJoinTable d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();  // Execute the query to ensure the query actually executes with dereference
    }

    @Test
    @Category({NoHibernate52.class, NoHibernate53.class})
    public void oneToOneJoinTableSingleValuedAssociationAbsoluteIdAccess() {
        Assume.assumeFalse(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id");
        String expectedQuery = "SELECT documentInfoJoinTable_1.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable documentInfoJoinTable_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate51.class, NoHibernate50.class, NoHibernate43.class, NoHibernate42.class})
    public void oneToOneJoinTableSingleValuedAssociationAbsoluteIdAccessWithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id");
        String expectedQuery = "SELECT d.documentInfoJoinTable.id FROM DocumentForOneToOneJoinTable d";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList(); // Execute the query to ensure the query actually executes with dereference
    }

    @Test
    @Category({NoHibernate52.class, NoHibernate53.class})
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride1() {
        Assume.assumeFalse(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id")
                .leftJoinDefault("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate51.class, NoHibernate50.class, NoHibernate43.class, NoHibernate42.class})
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride1WithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("d.documentInfoJoinTable.id")
                .leftJoinDefault("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT d.documentInfoJoinTable.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList(); // Execute the query to ensure the query actually executes with dereference
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("o.id")
                .leftJoinDefault("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOneJoinTable.class, "d")
                .select("o.id")
                .leftJoin("documentInfoJoinTable", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOneJoinTable d LEFT JOIN d.documentInfoJoinTable o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class})
    public void leftJoinDereferenedForeignJoinTableWithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder criteriaBuilder = cbf.create(em, DocumentForOneToOneJoinTable.class, "d")
                .leftJoinOn(DocumentForOneToOne.class, "e").on("d.documentInfoJoinTable.id").eqExpression("e.id").end();

        assertEquals("SELECT d FROM DocumentForOneToOneJoinTable d LEFT JOIN DocumentForOneToOne e ON (d.documentInfoJoinTable.id = e.id)",
                criteriaBuilder.getQueryString());
        criteriaBuilder.getResultList();
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class})
    public void leftJoinDereferenedForeignJoinTableInverseWithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder criteriaBuilder = cbf.create(em, DocumentForOneToOne.class, "e")
                .leftJoinOn(DocumentForOneToOneJoinTable.class, "d").on("d.documentInfoJoinTable.id").eqExpression("e.id").end();

        assertEquals("SELECT e FROM DocumentForOneToOne e LEFT JOIN DocumentForOneToOneJoinTable d ON (d.documentInfoJoinTable.id = e.id)",
                criteriaBuilder.getQueryString());
        criteriaBuilder.getResultList();
    }

    @Test
    @Category({NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class})
    public void leftJoinDereferenedForeignJoinTable() {
        Assume.assumeFalse(supportsTableGroupJoins());
        Assume.assumeTrue(jpaProvider.supportsEntityJoin());

        CriteriaBuilder criteriaBuilder = cbf.create(em, DocumentForOneToOneJoinTable.class, "d")
                .leftJoinOn(DocumentForOneToOne.class, "e").on("d.documentInfoJoinTable.id").eqExpression("e.id").end();

        assertEquals("SELECT d FROM DocumentForOneToOneJoinTable d LEFT JOIN DocumentForOneToOne e ON (EXISTS (SELECT 1 FROM d.documentInfoJoinTable _synth_subquery_0 WHERE _synth_subquery_0.id = e.id))",
                criteriaBuilder.getQueryString());
        criteriaBuilder.getResultList();
    }

}
