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

package com.blazebit.persistence.criteria;

import static org.junit.Assert.assertEquals;

import javax.persistence.criteria.JoinType;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.googlecode.catchexception.CatchException;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Person_;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JoinTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }

    @Test
    public void joinTypesSingular() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeJoin<Document, Person> p1 = root.join(Document_.owner, "owner1", JoinType.INNER);
        BlazeJoin<Document, Person> p2 = root.join(Document_.owner, "owner2", JoinType.LEFT);
        // Right join is not supported
        CatchException.verifyException(root, UnsupportedOperationException.class).join(Document_.owner, "owner3", JoinType.RIGHT);

        BlazeJoin<Person, Document> p1_1 = p1.join(Person_.partnerDocument, "partnerDoc", JoinType.INNER);
        
        cq.select(root.get(Document_.id));
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document JOIN document.owner owner1 JOIN owner1.partnerDocument partnerDoc LEFT JOIN document.owner owner2", criteriaBuilder.getQueryString());
    }

    @Test
    public void joinSet() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeJoin<Document, Person> partners = root.join(Document_.partners, "partner");
        BlazeJoin<Person, Document> ownerDocuments = partners.join(Person_.ownedDocuments, "doc");

        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document JOIN document.partners partner JOIN partner.ownedDocuments doc", criteriaBuilder.getQueryString());
    }

    @Test
    public void fetchSet() {
        BlazeCriteriaQuery<Document> cq = BlazeCriteria.get(cbf, Document.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeJoin<Document, Person> partners = root.fetch(Document_.partners, "partner");
        BlazeJoin<Person, Document> ownerDocuments = partners.join(Person_.ownedDocuments, "doc");
        ownerDocuments.fetch();

        cq.select(root);

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document FROM Document document JOIN FETCH document.partners partner JOIN FETCH partner.ownedDocuments doc", criteriaBuilder.getQueryString());
    }

    @Test
    public void listMapJoinWithFunctions() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeListJoin<Document, Person> people = root.join(Document_.people, "person");
        BlazeMapJoin<Document, Integer, Person> contacts = root.join(Document_.contacts, "contact");
        BlazeMapJoin<Person, Integer, String> localized = people.join(Person_.localized, "localized");
        BlazeJoin<Person, Document> partnerDocument = contacts.join(Person_.partnerDocument, "partnerDoc");

        cq.multiselect(
                people.index(),
                contacts.key(),
                contacts.value(),
                contacts.entry()
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT INDEX(person), KEY(contact), " + joinAliasValue("contact") + ", ENTRY(contact) " +
                "FROM Document document JOIN document.contacts contact JOIN contact.partnerDocument partnerDoc JOIN document.people person JOIN person.localized localized" +
                "", criteriaBuilder.getQueryString());
    }

    @Test
    // TODO: Report that SingularAttributeImpl#getBindableType() returns ENTITY_TYPE instead of SINGULAR_ATTRIBUTE
    @Category(NoDatanucleus.class)
    public void listStringMapEmbeddableJoinWithFunctions() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeListJoin<Document, Person> people = (BlazeListJoin<Document, Person>) (BlazeListJoin<?, ?>) root.join("people", "person");
        BlazeMapJoin<Document, Integer, Person> contacts = (BlazeMapJoin<Document, Integer, Person>) (BlazeMapJoin<?, ?, ?>) root.join("contacts", "contact");
        BlazeMapJoin<Person, Integer, String> localized = (BlazeMapJoin<Person, Integer, String>) (BlazeMapJoin<?, ?, ?>) people.join("localized", "localized");
        BlazeJoin<Person, Document> partnerDocument = contacts.join("partnerDocument", "partnerDoc");
        root.join("nameObject").join("intIdEntity", "intEntity");

        cq.multiselect(
                people.index(),
                contacts.key(),
                contacts.value(),
                contacts.entry()
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT INDEX(person), KEY(contact), " + joinAliasValue("contact") + ", ENTRY(contact) " +
                "FROM Document document JOIN document.contacts contact " +
                "JOIN contact.partnerDocument partnerDoc " +
                "JOIN document.nameObject.intIdEntity intEntity " +
                "JOIN document.people person " +
                "JOIN person.localized localized" +
                "", criteriaBuilder.getQueryString());
    }

    @Test
    public void joinsWithOnClause() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeJoin<Document, Person> partnerDocument = root.join(Document_.owner, "owner");
        BlazeListJoin<Document, Person> people = root.join(Document_.people, "person");
        BlazeMapJoin<Document, Integer, Person> contacts = root.join(Document_.contacts, "contact");

        partnerDocument.on(partnerDocument.get(Person_.age).isNotNull());
        people.on(people.index().isNotNull());
        contacts.on(contacts.key().isNotNull());

        cq.select(root.get(Document_.id));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document" +
                " JOIN document.contacts contact"
                + onClause("KEY(contact) IS NOT NULL") +
                " JOIN document.owner owner"
                + onClause("owner.age IS NOT NULL") +
                " JOIN document.people person"
                + onClause("INDEX(person) IS NOT NULL"), criteriaBuilder.getQueryString());
    }

}
