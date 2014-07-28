/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class JoinTest extends AbstractPersistenceTest {
    
    final String defaultDocumentAlias = "document";
    
    @Test
    public void testDefaultAlias(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        assertEquals("FROM Document " + defaultDocumentAlias, criteria.getQueryString());
    }
    
    @Test
    public void testRightJoinFetch(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.rightJoinFetch("owner", "o");
        criteria.rightJoinFetch("versions", "v");
        criteria.where("o").eq(0);
        
        assertEquals("FROM Document d RIGHT JOIN FETCH d.owner o RIGHT JOIN FETCH d.versions v WHERE o = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testRightJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.rightJoin("owner", "o");
        criteria.rightJoin("versions", "v");
        
        assertEquals("FROM Document d RIGHT JOIN d.owner o RIGHT JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testLeftJoinFetch(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.leftJoinFetch("owner", "o");
        criteria.leftJoinFetch("versions", "v");
        
        assertEquals("FROM Document d LEFT JOIN FETCH d.owner o LEFT JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testLeftJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.leftJoin("owner", "o");
        criteria.leftJoin("versions", "v");
        
        assertEquals("FROM Document d LEFT JOIN d.owner o LEFT JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testInnerJoinFetch(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.innerJoinFetch("owner", "o");
        criteria.innerJoinFetch("versions", "v");
        
        assertEquals("FROM Document d JOIN FETCH d.owner o JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testInnerJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.innerJoin("owner", "o");
        criteria.innerJoin("versions", "v");
        
        assertEquals("FROM Document d JOIN d.owner o JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testOuterJoinFetch(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.outerJoinFetch("owner", "o");
        criteria.outerJoinFetch("versions", "v");
        
        assertEquals("FROM Document d OUTER JOIN FETCH d.owner o OUTER JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testOuterJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.outerJoin("owner", "o");
        criteria.outerJoin("versions", "v");
        
        assertEquals("FROM Document d OUTER JOIN d.owner o OUTER JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testJoinMethodEquivalences(){
        final String qInnerJoin = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.INNER, false).getQueryString();
        final String qInnerJoinFetch = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.INNER, true).getQueryString();
        final String qLeftJoin = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.LEFT, false).getQueryString();
        final String qLeftJoinFetch = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.LEFT, true).getQueryString();
        final String qRightJoin = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, false).getQueryString();
        final String qRightJoinFetch = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, true).getQueryString();
        final String qOuterJoin = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.OUTER, false).getQueryString();
        final String qOuterJoinFetch = cbf.from(em, Document.class, "d").join("owner", "o", JoinType.OUTER, true).getQueryString();
        
        assertEquals(cbf.from(em, Document.class, "d").innerJoin("owner", "o").getQueryString(),
                qInnerJoin);
        assertEquals(cbf.from(em, Document.class, "d").innerJoinFetch("owner", "o").getQueryString(),
                qInnerJoinFetch);
        assertEquals(cbf.from(em, Document.class, "d").rightJoin("owner", "o").getQueryString(),
                qRightJoin);
        assertEquals(cbf.from(em, Document.class, "d").rightJoinFetch("owner", "o").getQueryString(),
                qRightJoinFetch);
        assertEquals(cbf.from(em, Document.class, "d").leftJoin("owner", "o").getQueryString(),
                qLeftJoin);
        assertEquals(cbf.from(em, Document.class, "d").leftJoinFetch("owner", "o").getQueryString(),
                qLeftJoinFetch);
        assertEquals(cbf.from(em, Document.class, "d").outerJoin("owner", "o").getQueryString(),
                qOuterJoin);
        assertEquals(cbf.from(em, Document.class, "d").outerJoinFetch("owner", "o").getQueryString(),
                qOuterJoinFetch);
    }
    
    @Test
    public void testNestedLeftJoinBeforeRightJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.join("owner.ownedDocuments.versions", "cont", JoinType.LEFT, false);
        criteria.join("owner.ownedDocuments.versions.document.name", "contName", JoinType.RIGHT, true);
        criteria.join("owner", "o", JoinType.INNER, true);
        
        
        String q = criteria.getQueryString();
        assertEquals("FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments LEFT JOIN FETCH ownedDocuments.versions cont LEFT JOIN FETCH cont.document document", q);
    }
    
    @Test
    public void testNestedRightJoinBeforeLeftJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.join("owner.ownedDocuments.versions", "cont", JoinType.RIGHT, false);
        criteria.join("owner.ownedDocuments.versions.document.name", "contName", JoinType.LEFT, true);
        criteria.join("owner", "o", JoinType.INNER, true);
        
        
        String q = criteria.getQueryString();
        assertEquals("FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments RIGHT JOIN FETCH ownedDocuments.versions cont LEFT JOIN FETCH cont.document document", q);
    }
    
    @Test
    public void testNestedLeftJoinAfterRightJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.join("owner.ownedDocuments.versions.document.name", "contName", JoinType.RIGHT, true);
        criteria.join("owner.ownedDocuments.versions", "cont", JoinType.LEFT, false);
        criteria.join("owner", "o", JoinType.INNER, true);
        
        
        String q = criteria.getQueryString();
        assertEquals("FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments LEFT JOIN FETCH ownedDocuments.versions cont LEFT JOIN FETCH cont.document document", q);
    }
    
    @Test
    public void testNestedRightJoinAfterLeftJoin(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.join("owner.ownedDocuments.versions.document.name", "contName", JoinType.LEFT, true);
        criteria.join("owner.ownedDocuments.versions", "cont", JoinType.RIGHT, false);
        criteria.join("owner", "o", JoinType.INNER, true);
        
        
        String q = criteria.getQueryString();
        assertEquals("FROM Document d JOIN FETCH d.owner o LEFT JOIN FETCH o.ownedDocuments ownedDocuments RIGHT JOIN FETCH ownedDocuments.versions cont LEFT JOIN FETCH cont.document document", q);
    }
    
	@Test(expected = NullPointerException.class)
    public void testConstructorClassNull(){
        cbf.from(em, null, "d");
    }
    
    @Test(expected = NullPointerException.class)
    public void testConstructorEntityManagerNull(){
        cbf.from(null, Document.class, "d");
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullPath(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        criteria.join(null, "o", JoinType.LEFT, true);
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullAlias(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        criteria.join("owner", null, JoinType.LEFT, true);
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullJoinType(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        criteria.join("owner", "o", null, true);
    }
    
//    @Test(expected = InvalidAliasException.class)
//    public void testJoinInvalidAlias1(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.join("owner", "d.owner", JoinType.LEFT, true);
//    }
//    
//    @Test(expected = InvalidAliasException.class)
//    public void testJoinInvalidAlias2(){
//        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
//        criteria.join("owner", ".", JoinType.LEFT, true);
//    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testJoinEmptyAlias(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class);
        criteria.join("owner", "", JoinType.LEFT, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnresolvedAlias1(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        
        criteria.where("z.c.x").eq(0).leftJoin("d.partners", "p");
        
        criteria.getQueryString();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnresolvedAlias2(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "a");
        
        criteria.where("z").eq(0);
        
        criteria.getQueryString();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnresolvedAliasInOrderBy(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "a");
        
        criteria.orderByAsc("z");
        
        criteria.getQueryString();
    }
    
    @Test
    public void testImplicitRootRelativeAlias(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "a");
        
        criteria.where("versions.document.name").eq(0).leftJoin("a.partners", "p");
        
        assertEquals("FROM Document a LEFT JOIN a.partners p LEFT JOIN a.versions versions LEFT JOIN versions.document document WHERE document.name = :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testCallOrderInvariance(){
        CriteriaBuilder<Document> criteria1 = cbf.from(em, Document.class, "a");
        CriteriaBuilder<Document> criteria2 = cbf.from(em, Document.class, "a");
        
        criteria1.where("p.ownedDocuments.name").eq(0).leftJoin("a.partners", "p");
        criteria2.leftJoin("a.partners", "p").where("p.ownedDocuments.name").eq(0);
        
        assertEquals("FROM Document a LEFT JOIN a.partners p LEFT JOIN p.ownedDocuments ownedDocuments WHERE ownedDocuments.name = :param_0", criteria1.getQueryString());
        assertEquals("FROM Document a LEFT JOIN a.partners p LEFT JOIN p.ownedDocuments ownedDocuments WHERE ownedDocuments.name = :param_0", criteria2.getQueryString());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testFetchJoinCheck(){
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "a");
        crit.select("name").join("d.versions", "versions", JoinType.LEFT, true);
    }
}