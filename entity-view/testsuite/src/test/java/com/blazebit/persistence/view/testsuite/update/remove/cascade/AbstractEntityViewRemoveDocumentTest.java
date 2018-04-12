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

package com.blazebit.persistence.view.testsuite.update.remove.cascade;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.LongSequenceEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractEntityViewRemoveDocumentTest<T> extends AbstractEntityViewUpdateTest<T> {

    protected Document doc1;
    protected Document doc2;
    protected Person p1;
    protected Person p2;
    protected Person p3;
    protected Person p4;
    protected Person p5;
    protected Person p6;

    public AbstractEntityViewRemoveDocumentTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    public AbstractEntityViewRemoveDocumentTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType, Class<?>... views) {
        super(mode, strategy, version, viewType, views);
    }

    @Override
    protected void prepareData(EntityManager em) {
        doc1 = new Document("doc1", null, new Version());
        doc1.setVersion(1L);
        doc1.setLastModified(new Date(EPOCH_2K));
        doc1.getNameObject().setPrimaryName("doc1");
        doc1.getNames().add(new NameObject("doc1", "doc1"));
        doc1.getNameMap().put("doc1", new NameObject("doc1", "doc1"));
        doc1.getNameContainer().setName("doc1");
        doc1.getNameContainer().getNameObject().setPrimaryName("doc1");
        doc1.getNameContainers().add(new NameObjectContainer("doc1", new NameObject("doc1", "doc1")));
        doc1.getNameContainerMap().put("doc1", new NameObjectContainer("doc1", new NameObject("doc1", "doc1")));
        doc2 = new Document("doc2");
        doc2.setVersion(1L);
        doc2.setLastModified(new Date(EPOCH_2K));
        doc2.setNameObject(new NameObject("doc2", "doc2"));
        doc2.getNames().add(new NameObject("doc2", "doc2"));
        doc2.getNameMap().put("doc1", new NameObject("doc2", "doc2"));
        doc2.getNameContainer().setName("doc2");
        doc2.getNameContainer().getNameObject().setPrimaryName("doc2");
        doc2.getNameContainers().add(new NameObjectContainer("doc2", new NameObject("doc2", "doc2")));
        doc2.getNameContainerMap().put("doc2", new NameObjectContainer("doc2", new NameObject("doc2", "doc2")));

        p1 = new Person("pers1");
        p1.getNameObject().setPrimaryName("pers1");
        p1.getLocalized().put(1, "localized1");
        p2 = new Person("pers2");
        p2.getNameObject().setPrimaryName("pers2");
        p2.getLocalized().put(1, "localized2");

        p3 = new Person("pers3");
        p3.getNameObject().setPrimaryName("pers3");
        p3.getLocalized().put(1, "localized3");
        p4 = new Person("pers4");
        p4.getNameObject().setPrimaryName("pers4");
        p4.getLocalized().put(1, "localized4");

        p5 = new Person("pers3");
        p5.getNameObject().setPrimaryName("pers3");
        p5.getLocalized().put(1, "localized3");
        p6 = new Person("pers4");
        p6.getNameObject().setPrimaryName("pers4");
        p6.getLocalized().put(1, "localized4");

        doc1.setOwner(p5);
        doc1.setResponsiblePerson(p1);
        doc1.getPeople().add(p1);
        doc1.getContacts().put(1, p1);
        doc1.getContacts2().put(2, p1);
        doc1.getStrings().add("asd");
        doc1.getStringMap().put("doc1", "doc1");

        doc2.setOwner(p6);
        doc2.setResponsiblePerson(p2);

        em.persist(p1);
        em.persist(p2);
        em.persist(p3);
        em.persist(p4);
        em.persist(p5);
        em.persist(p6);
        em.persist(doc1);
        em.persist(doc2);

        p1.setFriend(p3);
        p2.setFriend(p4);
    }

    @Override
    protected void cleanDatabase() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                clearCollections(em, Person.class, Document.class);
                em.createQuery("DELETE FROM Version").executeUpdate();
                em.createQuery("UPDATE Person SET partnerDocument = NULL, friend = NULL").executeUpdate();
                em.createQuery("DELETE FROM Document").executeUpdate();
                em.createQuery("DELETE FROM Person").executeUpdate();
            }
        });
    }

    @Override
    protected void restartTransactionAndReload() {
        restartTransaction();
        // Load into PC, then access via find
        cbf.create(em, Person.class)
                .where("id").in(ids(p1, p2, p3, p4, p5, p6))
                .getResultList();
        cbf.create(em, Document.class)
                .where("id").in(ids(doc1, doc2))
                .getResultList();
        doc1 = load(doc1);
        doc2 = load(doc2);
        p1 = load(p1);
        p2 = load(p2);
        p3 = load(p3);
        p4 = load(p4);
        p5 = load(p5);
        p6 = load(p6);
    }

    private List<Long> ids(LongSequenceEntity... entities) {
        List<Long> ids = new ArrayList<>(entities.length);
        for (LongSequenceEntity entity : entities) {
            if (entity != null) {
                ids.add(entity.getId());
            }
        }
        return ids;
    }

    private Document load(Document d) {
        if (d == null) {
            return null;
        }
        return em.find(Document.class, d.getId());
    }

    private Person load(Person p) {
        if (p == null) {
            return null;
        }
        return em.find(Person.class, p.getId());
    }

    protected T getDoc1View() {
        return evm.find(em, viewType, doc1.getId());
    }

    protected T getDoc2View() {
        return evm.find(em, viewType, doc2.getId());
    }

    protected <P> P getP1View(Class<P> personView) {
        return evm.find(em, personView, p1.getId());
    }

    protected <P> P getP2View(Class<P> personView) {
        return evm.find(em, personView, p2.getId());
    }

    protected <D> D getDocumentView(Long id, Class<D> documentView) {
        return evm.find(em, documentView, id);
    }

    protected <P> P getPersonView(Long id, Class<P> personView) {
        return evm.find(em, personView, id);
    }

    protected AssertStatementBuilder deleteDocumentOwned(AssertStatementBuilder builder) {
        return deleteDocumentOwned(builder, true);
    }

    protected AssertStatementBuilder deleteDocumentOwned(AssertStatementBuilder builder, boolean simpleDelete) {
        if (!isQueryStrategy() || simpleDelete || !dbmsDialect.supportsModificationQueryInWithClause()) {
            builder
                    .assertDelete().forRelation(Document.class, "contacts").and()
                    .assertDelete().forRelation(Document.class, "contacts2").and()
                    .assertDelete().forRelation(Document.class, "people").and()
                    .assertDelete().forRelation(Document.class, "peopleListBag").and()
                    .assertDelete().forRelation(Document.class, "peopleCollectionBag").and();
        }
        if (supportsNestedEmbeddables()) {
            builder
                    .assertDelete().forRelation(Document.class, "nameContainerMap").and()
                    .assertDelete().forRelation(Document.class, "nameContainers").and();
        }
        return builder
                .assertDelete().forRelation(Document.class, "nameMap").and()
                .assertDelete().forRelation(Document.class, "names").and()
                .assertDelete().forRelation(Document.class, "stringMap").and()
                .assertDelete().forRelation(Document.class, "strings").and()
                ;
    }

    protected AssertStatementBuilder deletePersonOwned(AssertStatementBuilder builder) {
        return deletePersonOwned(builder, true);
    }

    protected AssertStatementBuilder deletePersonOwned(AssertStatementBuilder builder, boolean simpleDelete) {
        if (!isQueryStrategy() || simpleDelete || !dbmsDialect.supportsModificationQueryInWithClause()) {
            builder
                    .assertDelete().forRelation(Person.class, "favoriteDocuments").and()
            ;
        }

        return builder
                .assertDelete().forRelation(Person.class, "localized").and()
                ;
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return null;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return null;
    }
}
