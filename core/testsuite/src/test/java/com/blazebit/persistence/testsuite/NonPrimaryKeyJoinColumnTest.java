/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate52;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate53;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
public class NonPrimaryKeyJoinColumnTest extends AbstractCoreTest {

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

    @Test
    // NOTE: Only Hibernate supports the single values association id access optimization, but doesn't do it for natural ids yet
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class})
//    @Category({ NoDatanucleus.class, NoOpenJPA.class })
    public void testNonPrimaryKeySingleValuedAssociationId() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class, "ref")
                .select("ref.id", "refId")
                .select("ref.book.isbn", "isbn");
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assertFalse(cb.getQueryString().toUpperCase().contains("JOIN"));
        cb.getResultList();
    }

    @Test
    // NOTE: Only Hibernate supports the single values association id access optimization, but doesn't do it for natural ids yet
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class})
//    @Category({ NoDatanucleus.class, NoOpenJPA.class })
    public void testNonPrimaryKeySingleValuedAssociationId2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class)
                .select("id", "refId")
                .select("book.isbn", "isbn");
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assertFalse(cb.getQueryString().toUpperCase().contains("JOIN"));
        cb.getResultList();
    }

    @Test
    @Category({ NoDatanucleus.class, NoOpenJPA.class })
    public void testNonPrimaryKeySingleValuedAssociationId3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class)
                .innerJoin("book", "b")
                .select("id", "refId")
                .select("b.isbn", "isbn");
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assertTrue(cb.getQueryString().contains("b.isbn"));
        cb.getResultList();
    }

    @Test
    @Category({ NoDatanucleus.class, NoOpenJPA.class })
    public void testNonPrimaryKeySingleValuedAssociationId4() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class, "r")
                .innerJoinOn("book", "b")
                    .on("b").eqExpression("r.bookNormal")
                .end()
                .select("id", "refId")
                .select("b.isbn", "isbn");
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assertTrue(cb.getQueryString().contains("b.isbn"));
        cb.getResultList();
    }

    // NOTE: No JPA provider supports the optimized natural id access and only EclipseLink and Hibernate seem to support natural ids
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class})
    public void testNonPrimaryKeySingleValuedAssociationId5() {
        CriteriaBuilder<Tuple> cb1 = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class, "r")
                .innerJoinOn("bookNormal", "b")
                    .on("b").eqExpression("r.book")
                .end()
                .select("id", "refId")
                .select("b.isbn", "isbn");
        CriteriaBuilder<Tuple> cb2 = cbf.create(em, Tuple.class).from(BookISBNReferenceEntity.class, "r")
                .innerJoinOn("bookNormal", "b")
                    .on("b.isbn").eqExpression("r.book.isbn")
                .end()
                .select("id", "refId")
                .select("b.isbn", "isbn");
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assertEquals(cb1.getQueryString(), cb2.getQueryString());
        cb1.getResultList();
        cb2.getResultList();
    }

    // NOTE: No JPA provider supports the optimized natural id access and only EclipseLink and Hibernate seem to support natural ids
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class})
    public void testNonPrimaryKeySingleValuedAssociationIdInUpdate() {
        Person owner = new Person("p1");
        BookEntity bookEntity = new BookEntity();
        bookEntity.setOwner(owner);
        bookEntity.setIsbn("123");
        em.persist(owner);
        em.persist(bookEntity);
        UpdateCriteriaBuilder<BookISBNReferenceEntity> cb1 = cbf.update(em, BookISBNReferenceEntity.class, "r")
                .setExpression("version", "r.version + 1")
                .where("r.book.isbn").eq(bookEntity.getIsbn());
        UpdateCriteriaBuilder<BookISBNReferenceEntity> cb2 = cbf.update(em, BookISBNReferenceEntity.class, "r")
                .setExpression("version", "r.version + 1")
                .where("r.book").eq(bookEntity);
        assumeTrue(jpaProvider.supportsSingleValuedAssociationIdExpressions());
        assumeTrue(jpaProvider.supportsSingleValuedAssociationNaturalIdExpressions());
        assertEquals("UPDATE BookISBNReferenceEntity r SET r.version = r.version + 1 WHERE r.book.isbn = :param_0", cb1.getQueryString());
        assertEquals("UPDATE BookISBNReferenceEntity r SET r.version = r.version + 1 WHERE r.book = :param_0", cb2.getQueryString());
        cb1.executeUpdate();
        cb2.executeUpdate();
    }

}
