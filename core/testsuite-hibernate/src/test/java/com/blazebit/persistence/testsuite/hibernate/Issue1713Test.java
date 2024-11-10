/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import org.junit.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;

/**
 * @author Christian Beikov
 * @since 1.6.6
 */
public class Issue1713Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ Person.class, PersonToOneCTE.class, IntIdEntity.class, NameObject.class, Document.class, Version.class };
    }

    @Test
    public void testBind() {
        String queryString = cbf.create(em, Integer.class)
                .with(PersonToOneCTE.class)
                .from(Person.class, "p")
                .bind("id").select("p.id")
                .end()
                .fromEntitySubquery(PersonToOneCTE.class, "a").end()
                .select("a.id")
                .getQueryString();

        System.out.println(queryString);
    }

    @CTE
    @Entity
    public static class PersonToOneCTE {

        @Id
        Long id;

        @OneToOne
        @PrimaryKeyJoinColumn
        Person person;

    }

}
