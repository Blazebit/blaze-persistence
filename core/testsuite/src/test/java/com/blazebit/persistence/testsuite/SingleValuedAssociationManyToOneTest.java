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

import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

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
