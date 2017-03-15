/*
 * Copyright 2014 - 2017 Blazebit.
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

import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.entity.DocumentForOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfo;
import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingleValuedAssociationTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class[]{
                DocumentForOneToOne.class,
                DocumentInfo.class
        });
    }

    @Test
    public void manyToOneSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentForOneToOne> criteria = cbf.create(em, DocumentForOneToOne.class, "d");
        criteria.where("d.owner").isNull();

        assertEquals("SELECT d FROM DocumentForOneToOne d WHERE d.owner IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void oneToOneSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentForOneToOne> criteria = cbf.create(em, DocumentForOneToOne.class, "d");
        criteria.where("d.documentInfo").isNull();

        assertEquals("SELECT d FROM DocumentForOneToOne d WHERE d.documentInfo IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentForOneToOne> criteria = cbf.create(em, DocumentForOneToOne.class, "d");
        criteria.where("d.documentInfo2").isNull();

        assertEquals("SELECT d FROM DocumentForOneToOne d WHERE d.documentInfo2 IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationIsNull() {
        CriteriaBuilder<DocumentInfo> criteria = cbf.create(em, DocumentInfo.class, "d");
        criteria.where("d.document").isNull();

        assertEquals("SELECT d FROM DocumentInfo d WHERE d.document IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void manyToOneSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("owner.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " FROM DocumentForOneToOne d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("documentInfo.id");
        String expectedQuery = "SELECT documentInfo_1.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo documentInfo_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("documentInfo2.id");
        String expectedQuery = "SELECT documentInfo2_1.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo2 documentInfo2_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationRelativeIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentInfo.class, "d")
                .select("document.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.document.id", "document_1") + " FROM DocumentInfo d" + singleValuedAssociationIdJoin("d.document", "document_1", true);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.owner.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.owner.id", "owner_1") + " FROM DocumentForOneToOne d" + singleValuedAssociationIdJoin("d.owner", "owner_1", false);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.documentInfo.id");
        String expectedQuery = "SELECT documentInfo_1.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo documentInfo_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.documentInfo2.id");
        String expectedQuery = "SELECT documentInfo2_1.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo2 documentInfo2_1";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationAbsoluteIdAccess() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentInfo.class, "d")
                .select("d.document.id");
        String expectedQuery = "SELECT " + singleValuedAssociationIdPath("d.document.id", "document_1") + " FROM DocumentInfo d" + singleValuedAssociationIdJoin("d.document", "document_1", true);
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.owner.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.documentInfo.id")
                .leftJoinDefault("documentInfo", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("d.documentInfo2.id")
                .leftJoinDefault("documentInfo2", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo2 o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationIdAccessJoinOverride1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentInfo.class, "d")
                .select("d.document.id")
                .leftJoinDefault("document", "o");
        String expectedQuery = "SELECT o.id FROM DocumentInfo d LEFT JOIN d.document o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoinDefault("owner", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoinDefault("documentInfo", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoinDefault("documentInfo2", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo2 o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationIdAccessJoinOverride2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentInfo.class, "d")
                .select("o.id")
                .leftJoinDefault("document", "o");
        String expectedQuery = "SELECT o.id FROM DocumentInfo d LEFT JOIN d.document o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
    
    @Test
    public void manyToOneSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoin("owner", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.owner o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoin("documentInfo", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneJoinTableSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentForOneToOne.class, "d")
                .select("o.id")
                .leftJoin("documentInfo2", "o");
        String expectedQuery = "SELECT o.id FROM DocumentForOneToOne d LEFT JOIN d.documentInfo2 o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }

    @Test
    public void oneToOneOwnerSingleValuedAssociationIdAccessJoinOverride3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(DocumentInfo.class, "d")
                .select("o.id")
                .leftJoin("document", "o");
        String expectedQuery = "SELECT o.id FROM DocumentInfo d LEFT JOIN d.document o";
        Assert.assertEquals(expectedQuery, cb.getQueryString());
    }
}
