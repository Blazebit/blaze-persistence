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
public class LikeTest extends AbstractPersistenceTest {
    @Test
    public void testLikeCaseInsensitive(){
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").like(pattern, false, null);
        
        assertEquals("FROM Document d WHERE " + getCaseInsensitiveLike("d.name", ":param_0", null), criteria.getQueryString());
    }
    
    @Test
    public void testLikeCaseSensitive(){
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").like(pattern, true, null);
        
        assertEquals("FROM Document d WHERE d.name LIKE :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testLikeEscaped(){
        final String pattern = "t\\_e%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").like(pattern, true, '\\');
        
        assertEquals("FROM Document d WHERE d.name LIKE :param_0 ESCAPE '\\'", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testLikeNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").like(null, true, null);
    }
    
    @Test
    public void testLikeExpressionCaseInsensitive(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").likeExpression("d.owner.name", false, null);
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE " + getCaseInsensitiveLike("d.name", "owner.name", null), criteria.getQueryString());
    }
    
    @Test
    public void testLikeExpressionCaseSensitive(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").likeExpression("d.owner.name", true, null);
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE d.name LIKE owner.name", criteria.getQueryString());
    }
    
    @Test
    public void testLikeExpressionEscaped(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").likeExpression("d.owner.name", true, '\\');
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE d.name LIKE owner.name ESCAPE '\\'", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testLikeExpressionNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").likeExpression(null, true, null);
    }
    
    /***** NOT LIKE *****/
    
    @Test
    public void testNotLikeCaseInsensitive() {
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLike(pattern, false, null);
        
        assertEquals("FROM Document d WHERE " + getCaseInsensitiveNotLike("d.name", ":param_0", null), criteria.getQueryString());
    }
    
    @Test
    public void testNotLikeCaseSensitive(){
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLike(pattern, true, null);
        
        assertEquals("FROM Document d WHERE NOT d.name LIKE :param_0", criteria.getQueryString());
    }
    
    @Test
    public void testNotLikeEscaped(){
        final String pattern = "t\\_e%t";
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLike(pattern, true, '\\');
        
        assertEquals("FROM Document d WHERE NOT d.name LIKE :param_0 ESCAPE '\\'", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testNotLikeNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLike(null, true, null);
    }
    
    @Test
    public void testNotLikeExpressionCaseInsensitive(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLikeExpression("d.owner.name", false, null);
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE " + getCaseInsensitiveNotLike("d.name", "owner.name", null), criteria.getQueryString());
    }
    
    @Test
    public void testLikeExpressionCaseInsensitiveEscaped(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").likeExpression("d.owner.name", false, '\\');
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE " + getCaseInsensitiveLike("d.name", "owner.name", '\\'), criteria.getQueryString());
    }
    
    @Test
    public void testNotLikeExpressionCaseInsensitiveEscaped(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLikeExpression("d.owner.name", false, '\\');
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE " + getCaseInsensitiveNotLike("d.name", "owner.name", '\\'), criteria.getQueryString());
    }
    
    @Test
    public void testNotLikeExpressionCaseSensitive(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLikeExpression("d.owner.name", true, null);
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE NOT d.name LIKE owner.name", criteria.getQueryString());
    }
    
    @Test
    public void testNotLikeExpressionEscaped(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLikeExpression("d.owner.name", true, '\\');
        
        assertEquals("FROM Document d JOIN d.owner owner WHERE NOT d.name LIKE owner.name ESCAPE '\\'", criteria.getQueryString());
    }
    
    @Test(expected = NullPointerException.class)
    public void testNotLikeExpressionNull(){
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").notLikeExpression(null, true, null);
    }
    
    private String getCaseInsensitiveLike(String property, String likeParam, Character escape){
        String res = "UPPER(" + property + ") LIKE UPPER(" + likeParam + ")";
        if(escape != null)
            res += " ESCAPE UPPER('" + escape + "')";
        return res;
    }
    
    private String getCaseInsensitiveNotLike(String property, String likeParam, Character escape){
        return "NOT " + getCaseInsensitiveLike(property, likeParam, escape);
    }
}
