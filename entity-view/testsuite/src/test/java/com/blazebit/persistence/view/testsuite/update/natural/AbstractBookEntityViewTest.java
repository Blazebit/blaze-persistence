/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.natural;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractBookEntityViewTest<T> extends AbstractEntityViewUpdateTest<T> {

    protected NaturalIdJoinTableEntity e1;
    protected BookISBNReferenceEntity e2;

    public AbstractBookEntityViewTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    public AbstractBookEntityViewTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType, Class<?>... views) {
        super(mode, strategy, version, viewType, views);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                BookEntity.class,
                BookISBNReferenceEntity.class,
                NaturalIdJoinTableEntity.class,
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class
        };
    }

    protected String[] getFetchedCollections() {
        return new String[] { };
    }

    @Override
    protected void prepareData(EntityManager em) {
        Person p1 = new Person("pers1");
        BookEntity b1 = new BookEntity();
        b1.setOwner(p1);
        b1.setIsbn("123");
        BookEntity b2 = new BookEntity();
        b2.setOwner(p1);
        b2.setIsbn("456");
        BookEntity b3 = new BookEntity();
        b3.setOwner(p1);
        b3.setIsbn("789");
        e1 = new NaturalIdJoinTableEntity();
        e1.setVersion(1L);
        e1.setOwner(p1);
        e1.setIsbn("123");
        e1.getManyToManyBook().put("b1", b1);
        e1.getManyToManyBook().put("b2", b2);
        e1.getOneToManyBook().add(b1);
        e1.getOneToManyBook().add(b2);
        e2 = new BookISBNReferenceEntity();
        e2.setVersion(1L);
        e2.setBook(b1);
        e2.setBookNormal(b2);

        em.persist(p1);
        em.persist(b1);
        em.persist(b2);
        em.persist(b3);
        em.persist(e1);
        em.persist(e2);
    }

    @Override
    protected void reload() {
        // Load into PC, then access via find
        cbf.create(em, NaturalIdJoinTableEntity.class)
                .fetch(getFetchedCollections())
                .getResultList();
        cbf.create(em, BookISBNReferenceEntity.class)
                .fetch("book", "bookNormal")
                .getResultList();
        e1 = em.find(NaturalIdJoinTableEntity.class, e1.getId());
        e2 = em.find(BookISBNReferenceEntity.class, e2.getId());
    }

    <X> X book(Class<X> type, String isbn) {
        return evm.applySetting(EntityViewSetting.create(type), cbf.create(em, BookEntity.class).where("isbn").eq(isbn)).getSingleResult();
    }

    <X> X naturalJoinTableEntity(Class<X> type, String isbn) {
        return evm.applySetting(EntityViewSetting.create(type), cbf.create(em, NaturalIdJoinTableEntity.class).where("isbn").eq(isbn)).getSingleResult();
    }

}
