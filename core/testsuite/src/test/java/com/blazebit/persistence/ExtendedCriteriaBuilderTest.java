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
import com.blazebit.persistence.spi.Criteria;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class ExtendedCriteriaBuilderTest extends AbstractPersistenceTest {
    
    final String defaultDocumentAlias = "document";
    
    @Test
    public void testDefaultAlias(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class);
        assertEquals("FROM Document " + defaultDocumentAlias, criteria.getQueryString());
    }
    
    @Test
    public void testRightJoinFetch(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.rightJoinFetch("owner", "o");
        criteria.rightJoinFetch("versions", "v");
        
        assertEquals("FROM Document d RIGHT JOIN FETCH d.owner o RIGHT JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testRightJoin(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.rightJoin("owner", "o");
        criteria.rightJoin("versions", "v");
        
        assertEquals("FROM Document d RIGHT JOIN d.owner o RIGHT JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testLeftJoinFetch(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.leftJoinFetch("owner", "o");
        criteria.leftJoinFetch("versions", "v");
        
        assertEquals("FROM Document d LEFT JOIN FETCH d.owner o LEFT JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testLeftJoin(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.leftJoin("owner", "o");
        criteria.leftJoin("versions", "v");
        
        assertEquals("FROM Document d LEFT JOIN d.owner o LEFT JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testInnerJoinFetch(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.innerJoinFetch("owner", "o");
        criteria.innerJoinFetch("versions", "v");
        
        assertEquals("FROM Document d JOIN FETCH d.owner o JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testInnerJoin(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.innerJoin("owner", "o");
        criteria.innerJoin("versions", "v");
        
        assertEquals("FROM Document d JOIN d.owner o JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testOuterJoinFetch(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.outerJoinFetch("owner", "o");
        criteria.outerJoinFetch("versions", "v");
        
        assertEquals("FROM Document d OUTER JOIN FETCH d.owner o OUTER JOIN FETCH d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testOuterJoin(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.outerJoin("owner", "o");
        criteria.outerJoin("versions", "v");
        
        assertEquals("FROM Document d OUTER JOIN d.owner o OUTER JOIN d.versions v", criteria.getQueryString());
    }
    
    @Test
    public void testJoinMethodEquivalences(){
        final String qInnerJoin = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.INNER, false).getQueryString();
        final String qInnerJoinFetch = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.INNER, true).getQueryString();
        final String qLeftJoin = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.LEFT, false).getQueryString();
        final String qLeftJoinFetch = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.LEFT, true).getQueryString();
        final String qRightJoin = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, false).getQueryString();
        final String qRightJoinFetch = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.RIGHT, true).getQueryString();
        final String qOuterJoin = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.OUTER, false).getQueryString();
        final String qOuterJoinFetch = Criteria.from(em, Document.class, "d").join("owner", "o", JoinType.OUTER, true).getQueryString();
        
        assertEquals(Criteria.from(em, Document.class, "d").innerJoin("owner", "o").getQueryString(),
                qInnerJoin);
        assertEquals(Criteria.from(em, Document.class, "d").innerJoinFetch("owner", "o").getQueryString(),
                qInnerJoinFetch);
        assertEquals(Criteria.from(em, Document.class, "d").rightJoin("owner", "o").getQueryString(),
                qRightJoin);
        assertEquals(Criteria.from(em, Document.class, "d").rightJoinFetch("owner", "o").getQueryString(),
                qRightJoinFetch);
        assertEquals(Criteria.from(em, Document.class, "d").leftJoin("owner", "o").getQueryString(),
                qLeftJoin);
        assertEquals(Criteria.from(em, Document.class, "d").leftJoinFetch("owner", "o").getQueryString(),
                qLeftJoinFetch);
        assertEquals(Criteria.from(em, Document.class, "d").outerJoin("owner", "o").getQueryString(),
                qOuterJoin);
        assertEquals(Criteria.from(em, Document.class, "d").outerJoinFetch("owner", "o").getQueryString(),
                qOuterJoinFetch);
    }
    
    @Test
    public void testNestedJoin(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.join("owner.ownedDocuments.partners", "cont", JoinType.LEFT, false);
        criteria.join("owner.ownedDocuments.partners.name", "contName", JoinType.RIGHT, true);
        criteria.join("versions", "v", JoinType.INNER, true);
        
        String q = criteria.getQueryString();
        assertEquals("FROM Document d LEFT JOIN FETCH d.owner owner LEFT JOIN FETCH owner.ownedDocuments ownedDocuments LEFT JOIN FETCH ownedDocuments.partners cont JOIN FETCH d.versions v", q);
    }
    
    @Test(expected = NullPointerException.class)
    public void testConstructorAliasNull(){
        Criteria.from(em, Document.class, null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullPath(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class);
        criteria.join(null, "o", JoinType.LEFT, true);
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullAlias(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class);
        criteria.join("owner", null, JoinType.LEFT, true);
    }
    
    @Test(expected = NullPointerException.class)
    public void testJoinNullJoinType(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class);
        criteria.join("owner", "o", null, true);
    }
    
//    @Test(expected = InvalidAliasException.class)
//    public void testJoinInvalidAlias1(){
//        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
//        criteria.join("owner", "d.owner", JoinType.LEFT, true);
//    }
//    
//    @Test(expected = InvalidAliasException.class)
//    public void testJoinInvalidAlias2(){
//        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
//        criteria.join("owner", ".", JoinType.LEFT, true);
//    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testJoinEmptyAlias(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class);
        criteria.join("owner", "", JoinType.LEFT, true);
    }

    
    
    @Test
    public void testWhereProperty(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age").ge(25);

        assertEquals("FROM Document d WHERE d.age >= :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testWhereExpression(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age + 1").ge(25);

        assertEquals("FROM Document d WHERE d.age+1 >= :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testWherePath(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.partners.ownedDocuments.age + 1").ge(25);

        assertEquals("FROM Document d LEFT JOIN d.partners partners LEFT JOIN partners.ownedDocuments ownedDocuments WHERE ownedDocuments.age+1 >= :param_0", criteria.getQueryString());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testWhereNotClosed(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("d.age");
        criteria.where("d.owner");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWhereEmptyExpression(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where("");
    
    }
    
    @Test(expected = NullPointerException.class)
    public void testWhereNullExpression(){
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.where(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testEntityManagerNull(){
        Criteria.from(null, Document.class, "d");
    }
}
