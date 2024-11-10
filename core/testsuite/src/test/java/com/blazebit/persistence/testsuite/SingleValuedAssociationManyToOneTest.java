/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Tuple;

import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingleValuedAssociationManyToOneTest extends AbstractCoreTest {

    @Test
    public void manyToOneSingleValuedAssociationIsNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").isNull();

        assertEquals("SELECT d FROM Document d WHERE d.owner IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void manyToOneSingleValuedAssociationIsNullAndSelectCopyResolved() {
        CriteriaBuilder<String> criteria = cbf.create(em, String.class)
                .from(Document.class, "d")
                .select("d.owner.name");
        criteria.where("d.owner").isNull();

        assertEquals("SELECT owner_1.name FROM Document d JOIN d.owner owner_1 WHERE owner_1 IS NULL", criteria.getQueryString());
        criteria.getResultList();
        criteria = criteria.copy(String.class).select("d.name");
        assertEquals("SELECT d.name FROM Document d WHERE d.owner IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void manyToOneSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("owner.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.owner.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " FROM Document d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.owner.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.owner.id", "o") + " FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("o.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT o.id FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("o.id")
                .leftJoin("owner", "o");
        String expectedQuery = "SELECT o.id FROM Document d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
}
