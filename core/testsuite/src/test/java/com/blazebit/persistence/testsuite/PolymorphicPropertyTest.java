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
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;
import com.googlecode.catchexception.CatchException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Negative tests to assert implicit downcast is not supported!
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PolymorphicPropertyTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            PolymorphicPropertyBase.class,
            PolymorphicPropertySub1.class,
            PolymorphicPropertySub2.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class
        };
    }
    
    @Test
    public void testSelectSubProperty() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("propBase.base.relation1");
        cb.where("TYPE(base)").eq(PolymorphicSub1.class);

        CatchException.verifyException(cb, IllegalArgumentException.class).getQueryString();
        String message = CatchException.caughtException().getMessage();
        assertTrue(message.contains("base"));
        assertTrue(message.contains(PolymorphicPropertyBase.class.getSimpleName()));
    }

    @Test
    public void testSelectSubPropertyWithTreat() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("relation1");
        CatchException.verifyException(cb, IllegalArgumentException.class).leftJoin("TREAT(propBase.base AS PolymorphicSub1)", "base1");
        String message = CatchException.caughtException().getMessage();
        assertTrue(message.contains("base"));
        assertTrue(message.contains(PolymorphicPropertyBase.class.getSimpleName()));
    }
}
