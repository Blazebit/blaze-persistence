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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

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
