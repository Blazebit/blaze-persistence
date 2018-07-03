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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import org.junit.Test;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class NonPrimaryKeyJoinColumnTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                BookEntity.class,
                BookISBNReferenceEntity.class,
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class
        };
    }

    @Test
    public void testExcplicitMultipleJoins() {
        CriteriaBuilder<BookEntity> cb = cbf.create(em, BookEntity.class).from(BookEntity.class);
        cb.getResultList();
    }

}
