/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.multisetbig;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.fetch.multisetbig.model.DocumentMultisetFetchView;
import com.blazebit.persistence.view.testsuite.fetch.multisetbig.model.DocumentTemporalsView;
import com.blazebit.persistence.view.testsuite.fetch.multisetbig.model.PersonMultisetFetchView;
import com.blazebit.persistence.view.testsuite.fetch.multisetbig.model.SimplePersonMultisetFetchView;
import com.blazebit.persistence.view.testsuite.timeentity.DocumentForMultisetFetch;
import com.blazebit.persistence.view.testsuite.timeentity.PersonForMultisetFetch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
public class BigMultisetTest extends AbstractEntityViewTest {

    private PersonForMultisetFetch pers1;
    private PersonForMultisetFetch pers2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                DocumentForMultisetFetch.class,
                PersonForMultisetFetch.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentForMultisetFetch doc1 = new DocumentForMultisetFetch( "doc1");
                DocumentForMultisetFetch doc2 = new DocumentForMultisetFetch( "doc2");
                DocumentForMultisetFetch doc3 = new DocumentForMultisetFetch( "doc3");
                DocumentForMultisetFetch doc4 = new DocumentForMultisetFetch( "doc4");

                pers1 = new PersonForMultisetFetch( "pers1");
                pers2 = new PersonForMultisetFetch( "pers2");

                doc1.setOwner(pers1);
                doc2.setOwner(pers1);
                doc3.setOwner(pers1);
                doc4.setOwner(pers1);

                doc1.getPersonList().add(pers1);
                doc1.getPersonList().add(pers2);

                doc1.getContacts().put(1, pers1);
                doc1.getContacts().put(2, pers2);

                doc1.setTheLocalDate( LocalDate.of(2012, 1, 12 ));
                doc1.setTheLocalDateTime( LocalDateTime.of( 2012, 1, 12, 13, 43, 38, 123 ));
                doc1.setTheLocalTime( LocalTime.of( 13, 43, 38, 123 ));
                doc1.setTheOffsetDateTime( LocalDateTime.of( 2012, 1, 12, 13, 43, 38, 123 ).atOffset( ZoneOffset.UTC ));
                doc1.setTheOffsetTime( LocalTime.of( 13, 43, 38, 123 ).atOffset( ZoneOffset.UTC ));
                doc1.setTheZonedDateTime( LocalDateTime.of( 2012, 1, 12, 13, 43, 38, 123 ).atZone( ZoneOffset.UTC ));
                doc1.setTheInstant( doc1.getTheOffsetDateTime().toInstant() );
                doc1.setTheDate( Date.valueOf(doc1.getTheLocalDate()) );
                doc1.setTheTime( Time.valueOf(doc1.getTheLocalTime()));
                doc1.setTheTimestamp( Timestamp.valueOf( doc1.getTheLocalDateTime()));

                em.persist(pers1);
                em.persist(pers2);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);

                pers1.setPartnerDocument(doc1);
                pers2.setPartnerDocument(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        pers1 = cbf.create(em, PersonForMultisetFetch.class).where( "name").eq( "pers1").getSingleResult();
        pers2 = cbf.create(em, PersonForMultisetFetch.class).where( "name").eq( "pers2").getSingleResult();
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    // NOTE: Java 8 time types are only supported as of Hibernate 5.2
    @Test
    @Category({ NoDB2.class, NoDatanucleus.class, NoEclipselink.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class })
    public void testCollections() {
        EntityViewManager evm = build(
                SimplePersonMultisetFetchView.class,
                PersonMultisetFetchView.class,
                DocumentMultisetFetchView.class,
                DocumentTemporalsView.class
        );

        CriteriaBuilder<PersonForMultisetFetch> criteria = cbf.create( em, PersonForMultisetFetch.class, "p")
                .where("id").in(pers1.getId(), pers2.getId())
                .orderByAsc("id");
        CriteriaBuilder<PersonMultisetFetchView> cb = evm.applySetting( EntityViewSetting.create(
				PersonMultisetFetchView.class), criteria);
        List<PersonMultisetFetchView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Pers1
        assertEquals(pers1.getName(), results.get(0).getName());
        assertSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getOwnedDocuments());

        // Pers2
        assertEquals(pers2.getName(), results.get(1).getName());
        assertSubviewCollectionEquals(pers2.getOwnedDocuments(), results.get(1).getOwnedDocuments());
    }


    private void assertSubviewCollectionEquals(Set<DocumentForMultisetFetch> ownedDocuments, Set<? extends DocumentMultisetFetchView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        for ( DocumentForMultisetFetch doc : ownedDocuments) {
            boolean found = false;
            for ( DocumentMultisetFetchView docSub : ownedSubviewDocuments) {
                if (doc.getName().equals(docSub.getName())) {
                    found = true;

                    assertTemporalsViewEquals( doc, docSub.getTemporals1() );
                    assertTemporalsViewEquals( doc, docSub.getTemporals2() );
                    assertTemporalsViewEquals( doc, docSub.getTemporals3() );
                    assertTemporalsViewEquals( doc, docSub.getTemporals4() );
                    assertTemporalsViewEquals( doc, docSub.getTemporals5() );
                    break;
                }
            }

            if (!found) {
                Assert.fail( "Could not find a DocumentMultisetFetchView with the name: " + doc.getName());
            }
        }
    }

    private void assertTemporalsViewEquals(DocumentForMultisetFetch doc, DocumentTemporalsView docSub) {
        assertEquals( doc.getTheInstant(), docSub.getTheInstant() );
        assertEquals( doc.getTheLocalDate(), docSub.getTheLocalDate() );
        assertEquals( doc.getTheLocalDateTime(), docSub.getTheLocalDateTime() );
        assertEquals( doc.getTheLocalTime(), docSub.getTheLocalTime() );
        if ( doc.getTheOffsetDateTime() == null ) {
            assertNull( docSub.getTheOffsetDateTime() );
        } else {
            assertEquals(
                    doc.getTheOffsetDateTime().toInstant(),
                    docSub.getTheOffsetDateTime().toInstant()
            );
        }
        assertEquals( doc.getTheOffsetTime(), docSub.getTheOffsetTime() );
        if ( doc.getTheZonedDateTime() == null ) {
            assertNull( docSub.getTheZonedDateTime() );
        } else {
            assertEquals( doc.getTheZonedDateTime().toInstant(), docSub.getTheZonedDateTime().toInstant() );
        }
        assertEquals( doc.getTheDate(), docSub.getTheDate() );
        assertEquals( doc.getTheTime(), docSub.getTheTime() );
        assertEquals( doc.getTheTimestamp(), docSub.getTheTimestamp() );
    }
}
