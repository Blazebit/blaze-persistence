/*
 * Copyright 2014 - 2022 Blazebit.
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

import org.hibernate.annotations.Where;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class Issue1167Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ BasicEntity.class };
    }

    @Entity(name = "BasicEntity")
    @Where(clause = "deleted_char <> 't'")
    public static class BasicEntity {
        @Id
        Long id;
        @Column(name = "deleted_char", length = 1, nullable = false)
        char deleted = 'f';
        @ManyToOne
        BasicEntity parent;
    }

    @Test
    public void test1() {
        cbf.create(em, BasicEntity.class).getResultList();
    }

}
