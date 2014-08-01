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
import com.blazebit.persistence.entity.Person;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class SubqueryTest extends AbstractPersistenceTest {

    @Test
    public void testRootAliasInSubquery() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("id").in().from(Person.class).select("id").where("ownedDocuments").eqExpression("d").end().getQueryString();
        String expected = "FROM Document d WHERE d.id IN (SELECT person.id FROM Person person LEFT JOIN person.ownedDocuments ownedDocuments WHERE ownedDocuments = d)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testAmbiguousSelectAliases() {
        // we decided that this should not throw an exception
        // - we first check for existing aliases and if none exist we check if an implicit root alias is possible
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "name").where("id").in().from(Person.class).select("id").where("name").eqExpression("name").end().getQueryString();
        String expected = "SELECT d.name AS name FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE d.name = d.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testSelectAliases() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "n").where("id")
                .in().from(Person.class).select("id").where("d.name").eqExpression("name")
                .end()
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE d.name = person.name)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithSelectAliases() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "n")
                .where("id")
                .in().from(Person.class).select("id").where("name").eqExpression("d.name").end()
                .where("id")
                .notIn().from(Person.class).select("id").where("d.name").likeExpression("name").end()
                .orderByAsc("n")
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d WHERE d.id IN "
                + "(SELECT person.id FROM Person person WHERE person.name = d.name) AND NOT d.id IN "
                + "(SELECT person.id FROM Person person WHERE d.name LIKE person.name) "
                + "ORDER BY n ASC NULLS LAST";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithJoinAliases() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "n")
                .leftJoin("versions", "v")
                .where("id")
                    .in().from(Person.class, "p").select("id").where("SIZE(d.versions)").eqExpression("SIZE(p.ownedDocuments)").end()
                .where("id")
                    .notIn().from(Person.class).select("id").where("SIZE(d.versions)").ltExpression("SIZE(partnerDocument.versions)").end()
                .getQueryString();
        
        String expected = "SELECT d.name AS n FROM Document d LEFT JOIN d.versions v WHERE d.id IN "
                + "(SELECT p.id FROM Person p LEFT JOIN p.ownedDocuments ownedDocuments WHERE SIZE(d.versions) = SIZE(p.ownedDocuments)) AND NOT d.id IN "
                + "(SELECT person.id FROM Person person LEFT JOIN person.partnerDocument partnerDocument LEFT JOIN partnerDocument.versions versions WHERE SIZE(d.versions) < SIZE(partnerDocument.versions))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testImplicitAliasPostfixing() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "n")
                .where("SIZE(d.versions)").lt(5)
                .where("id")
                    .in().from(Document.class, "document").select("id").where("SIZE(document.versions)").eqExpression("SIZE(document.partners)").end()
                .getQueryString();
        
        String expected = "SELECT d.name AS n FROM Document d LEFT JOIN d.versions versions WHERE SIZE(d.versions) < :param_0 AND d.id IN "
                + "(SELECT document.id FROM Document document LEFT JOIN document.partners partners LEFT JOIN document.versions versions_1 WHERE SIZE(document.versions) = SIZE(document.partners))";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testImplicitRootAliasPostfixing() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "document");
        crit.select("name", "n")
                .leftJoin("versions", "v")
                .where("SIZE(document.versions)").lt(5)
                .where("id")
                    .in().from(Document.class).select("id").where("name").eq("name1").end()
                .getQueryString();
        
        String expected = "SELECT document.name AS n FROM Document document LEFT JOIN document.versions v WHERE SIZE(document.versions) < :param_0 AND document.id IN "
                + "(SELECT document_1.id FROM Document document_1 WHERE document_1.name = :param_1)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSubqueryWithoutSelect() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.where("id").in().from(Person.class).where("ownedDocuments").eqExpression("d").end().getQueryString();
        String expected = "FROM Document d WHERE d.id IN (SELECT * FROM Person person LEFT JOIN person.ownedDocuments ownedDocuments WHERE ownedDocuments = d)";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testMultipleSubqueriesWithParameters() {
        CriteriaBuilder<Document> crit = cbf.from(em, Document.class, "d");
        crit.select("name", "n")
                .where("id")
                .in().from(Person.class).select("id").where("d.name").eq("name").end()
                .where("id")
                .notIn().from(Person.class).select("id").where("d.name").like("test").end()
                .where("SIZE(d.versions)").lt(5)
                .getQueryString();
        String expected = "SELECT d.name AS n FROM Document d LEFT JOIN d.versions versions WHERE d.id IN (SELECT person.id FROM Person person WHERE d.name = :param_0) AND NOT d.id IN (SELECT person.id FROM Person person WHERE d.name LIKE :param_1) AND SIZE(d.versions) < :param_2";

        assertEquals(expected, crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void workingJPQLQueries(){
//        em.createQuery("SELECT SIZE(owner.ownedDocuments) AS ownedSize FROM Document d LEFT JOIN d.versions versions LEFT JOIN d.owner owner WHERE d.id IN "
//                + "(SELECT p.id FROM Person p WHERE ownedSize = p.name)").getResultList();
        em.createQuery("FROM Document d LEFT JOIN d.versions versions LEFT JOIN d.owner owner WHERE d.id IN "
                + "(SELECT p.id FROM Person p WHERE SIZE(d.versions) = p.name)").getResultList();
        em.createQuery("FROM Document d LEFT JOIN d.versions versions LEFT JOIN versions.document document LEFT JOIN document.partners partners LEFT JOIN d.owner owner WHERE d.id IN "
                + "(SELECT p.id FROM Person p WHERE SIZE(document.partners) = p.name)").getResultList();
        em.createQuery("FROM Person p LEFT JOIN p.partnerDocument partnerDocument LEFT JOIN partnerDocument.versions versions WHERE SIZE(partnerDocument.versions) > 0").getResultList();
//        em.createQuery("SELECT d.name AS n FROM Document d WHERE d.id IN "
//                + "(SELECT p.id FROM Person p WHERE p.name = n)").getResultList();
        
        em.createQuery("SELECT d.name AS n FROM Document d WHERE d.id > 0 ORDER BY n").getResultList();
        em.createQuery("SELECT d.name FROM Document d WHERE UPPER(name) = LOWER(name)").getResultList();
        em.createQuery("SELECT name, UPPER(d.name) FROM Document d").getResultList();
        
        em.createQuery("SELECT name, UPPER(d.name) FROM Document d WHERE d.name = (SELECT AVG(d.age) FROM Document d2 WHERE d.id IN (SELECT p.id FROM Person p))").getResultList();
        
//        em.createQuery("SELECT d.name AS n FROM Document d WHERE d.id IN "
//                + "(SELECT p.id FROM Person p WHERE p.name = 'test' ORDER BY n)").getResultList();
    }
}
